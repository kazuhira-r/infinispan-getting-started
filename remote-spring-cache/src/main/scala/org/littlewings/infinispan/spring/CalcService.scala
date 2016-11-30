package org.littlewings.infinispan.spring

import java.util.concurrent.TimeUnit

import org.springframework.cache.annotation.{CacheConfig, CacheEvict, Cacheable}
import org.springframework.stereotype.Service

@Service
@CacheConfig(cacheNames = Array("calcCache"))
class CalcService {
  @Cacheable
  def add(a: Int, b: Int): Int = {
    TimeUnit.SECONDS.sleep(3L)

    a + b
  }

  @CacheEvict
  def evict(a: Int, b: Int): Unit = ()
}
