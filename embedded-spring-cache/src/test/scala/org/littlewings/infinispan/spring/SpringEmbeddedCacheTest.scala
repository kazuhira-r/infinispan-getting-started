package org.littlewings.infinispan.spring

import java.util.concurrent.TimeUnit

import org.junit.runner.RunWith
import org.junit.{Before, Test}
import org.scalatest.Matchers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cache.CacheManager
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringBootTest(classes = Array(classOf[App]))
class SpringEmbeddedCacheTest extends Matchers {
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
  def embeddedCacheSimpleTest(): Unit = {
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
  def embeddedCacheEvictTest(): Unit = {
    sw {
      calcService.add(1, 3) should be(4)
    } should be >= 3L

    calcService.evict(1, 3)

    sw {
      calcService.add(1, 3) should be(4)
    } should be >= 3L
  }
}
