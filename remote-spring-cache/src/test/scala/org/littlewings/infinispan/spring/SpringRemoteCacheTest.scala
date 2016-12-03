package org.littlewings.infinispan.spring

import java.util.concurrent.TimeUnit

import org.infinispan.commons.equivalence.AnyServerEquivalence
import org.infinispan.configuration.cache.ConfigurationBuilder
import org.infinispan.manager.DefaultCacheManager
import org.infinispan.server.hotrod.HotRodServer
import org.infinispan.server.hotrod.configuration.HotRodServerConfigurationBuilder
import org.junit.runner.RunWith
import org.junit.{AfterClass, Before, BeforeClass, Test}
import org.scalatest.Matchers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cache.CacheManager
import org.springframework.test.context.junit4.SpringRunner

object SpringRemoteCacheTest {
  var hotRodServer: HotRodServer = _

  @BeforeClass
  def setUpClass(): Unit = {
    val embeddedCacheManager = new DefaultCacheManager
    embeddedCacheManager
      .defineConfiguration(
        "calcCache",
        new ConfigurationBuilder()
          .dataContainer()
          .keyEquivalence(new AnyServerEquivalence)
          .valueEquivalence(new AnyServerEquivalence)
          .expiration
          .lifespan(5L, TimeUnit.SECONDS)
          .build
      )

    val hotRodServerHost = "localhost"
    val hotRodServerPort = 11222
    hotRodServer = new HotRodServer
    hotRodServer.start(
      new HotRodServerConfigurationBuilder()
        .host(hotRodServerHost)
        .port(hotRodServerPort)
        .build,
      embeddedCacheManager
    )
  }

  @AfterClass
  def tearDownClass(): Unit = hotRodServer.stop
}

@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[App]))
class SpringRemoteCacheTest extends Matchers {
  @Autowired
  var calcService: CalcService = _

  @Autowired
  var cacheManager: CacheManager = _

  @Before
  def setUp(): Unit =
    cacheManager.getCache("calcCache").clear()

  protected def sw(fun: => Unit): Long = {
    val startTime = System.nanoTime
    fun
    TimeUnit.SECONDS.convert(System.nanoTime - startTime, TimeUnit.NANOSECONDS)
  }

  @Test
  def remoteCacheSimpleTest(): Unit = {
    sw {
      calcService.add(1, 3) should be(4)
    } should be >= 3L
    sw {
      calcService.add(1, 3) should be(4)
    } should be < 1L

    TimeUnit.SECONDS.sleep(5L)

    sw {
      calcService.add(1, 3) should be(4)
    } should be >= 3L
  }

  @Test
  def remoteCacheEvictTest(): Unit = {
    sw {
      calcService.add(1, 3) should be(4)
    } should be >= 3L

    calcService.evict(1, 3)

    sw {
      calcService.add(1, 3) should be(4)
    } should be >= 3L
  }
}
