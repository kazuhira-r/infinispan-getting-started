package org.littlewings.infinispan.interceptors

import org.infinispan.Cache
import org.infinispan.interceptors.distribution._
import org.infinispan.interceptors.locking.{NonTransactionalLockingInterceptor, OptimisticLockingInterceptor}
import org.infinispan.interceptors._
import org.infinispan.manager.DefaultCacheManager
import org.infinispan.statetransfer.{StateTransferInterceptor, TransactionSynchronizerInterceptor}
import org.scalatest.{FunSpec, Matchers}

import scala.collection.JavaConverters._

class PrintInterceptorsSpec extends FunSpec with Matchers {
  describe("Print Interceptors Spec") {
    it("local-cache") {
      withCache[String, String]("localCache") { cache =>
        cache.getAdvancedCache.getInterceptorChain.asScala.map(_.getClass) should contain theSameElementsInOrderAs (
          Seq(
            classOf[InvocationContextInterceptor],
            classOf[CacheMgmtInterceptor],
            classOf[NonTransactionalLockingInterceptor],
            classOf[EntryWrappingInterceptor],
            classOf[CallInterceptor]
          )
          )
      }
    }

    it("replicated-cache") {
      withCache[String, String]("replicatedCache") { cache =>
        cache.getAdvancedCache.getInterceptorChain.asScala.map(_.getClass) should contain theSameElementsInOrderAs (
          Seq(
            classOf[DistributionBulkInterceptor[_, _]],
            classOf[InvocationContextInterceptor],
            classOf[CacheMgmtInterceptor],
            classOf[StateTransferInterceptor],
            classOf[NonTransactionalLockingInterceptor],
            classOf[EntryWrappingInterceptor],
            classOf[NonTxDistributionInterceptor],
            classOf[CallInterceptor]
          )
          )
      }
    }

    it("distributed-cache") {
      withCache[String, String]("distributedCache") { cache =>
        cache.getAdvancedCache.getInterceptorChain.asScala.map(_.getClass) should contain theSameElementsInOrderAs (
          Seq(
            classOf[DistributionBulkInterceptor[_, _]],
            classOf[InvocationContextInterceptor],
            classOf[CacheMgmtInterceptor],
            classOf[StateTransferInterceptor],
            classOf[NonTransactionalLockingInterceptor],
            classOf[EntryWrappingInterceptor],
            classOf[NonTxDistributionInterceptor],
            classOf[CallInterceptor]
          )
          )
      }
    }

    it("invalidation-cache") {
      withCache[String, String]("invalidationCache") { cache =>
        cache.getAdvancedCache.getInterceptorChain.asScala.map(_.getClass) should contain theSameElementsInOrderAs (
          Seq(
            classOf[InvocationContextInterceptor],
            classOf[CacheMgmtInterceptor],
            classOf[NonTransactionalLockingInterceptor],
            classOf[EntryWrappingInterceptor],
            classOf[InvalidationInterceptor],
            classOf[CallInterceptor]
          )
          )
      }
    }

    it("transactional local-cache") {
      withCache[String, String]("localTxCache") { cache =>
        cache.getAdvancedCache.getInterceptorChain.asScala.map(_.getClass) should contain theSameElementsInOrderAs (
          Seq(
            classOf[InvocationContextInterceptor],
            classOf[CacheMgmtInterceptor],
            classOf[TxInterceptor[_, _]],
            classOf[OptimisticLockingInterceptor],
            classOf[NotificationInterceptor],
            classOf[EntryWrappingInterceptor],
            classOf[CallInterceptor]
          )
          )
      }
    }

    it("transactional replicated-cache") {
      withCache[String, String]("replicatedTxCache") { cache =>
        cache.getAdvancedCache.getInterceptorChain.asScala.map(_.getClass) should contain theSameElementsInOrderAs (
          Seq(
            classOf[DistributionBulkInterceptor[_, _]],
            classOf[InvocationContextInterceptor],
            classOf[CacheMgmtInterceptor],
            classOf[StateTransferInterceptor],
            classOf[TransactionSynchronizerInterceptor],
            classOf[TxInterceptor[_, _]],
            classOf[OptimisticLockingInterceptor],
            classOf[NotificationInterceptor],
            classOf[EntryWrappingInterceptor],
            classOf[TxDistributionInterceptor],
            classOf[CallInterceptor]
          )
          )
      }
    }

    it("transactional distributed-cache") {
      withCache[String, String]("distributedTxCache") { cache =>
        cache.getAdvancedCache.getInterceptorChain.asScala.map(_.getClass) should contain theSameElementsInOrderAs (
          Seq(
            classOf[DistributionBulkInterceptor[_, _]],
            classOf[InvocationContextInterceptor],
            classOf[CacheMgmtInterceptor],
            classOf[StateTransferInterceptor],
            classOf[TransactionSynchronizerInterceptor],
            classOf[TxInterceptor[_, _]],
            classOf[OptimisticLockingInterceptor],
            classOf[NotificationInterceptor],
            classOf[EntryWrappingInterceptor],
            classOf[TxDistributionInterceptor],
            classOf[CallInterceptor]
          )
          )
      }
    }

    it("transactional invalidation-cache") {
      withCache[String, String]("invalidationTxCache") { cache =>
        cache.getAdvancedCache.getInterceptorChain.asScala.map(_.getClass) should contain theSameElementsInOrderAs (
          Seq(
            classOf[InvocationContextInterceptor],
            classOf[CacheMgmtInterceptor],
            classOf[TxInterceptor[_, _]],
            classOf[OptimisticLockingInterceptor],
            classOf[NotificationInterceptor],
            classOf[EntryWrappingInterceptor],
            classOf[InvalidationInterceptor],
            classOf[CallInterceptor]
          )
          )
      }
    }

    it("l1 enabled, distributed-cache") {
      withCache[String, String]("distributedL1Cache") { cache =>
        cache.getAdvancedCache.getInterceptorChain.asScala.map(_.getClass) should contain theSameElementsInOrderAs (
          Seq(
            classOf[DistributionBulkInterceptor[_, _]],
            classOf[InvocationContextInterceptor],
            classOf[CacheMgmtInterceptor],
            classOf[StateTransferInterceptor],
            classOf[NonTransactionalLockingInterceptor],
            classOf[L1LastChanceInterceptor],
            classOf[EntryWrappingInterceptor],
            classOf[L1NonTxInterceptor],
            classOf[NonTxDistributionInterceptor],
            classOf[CallInterceptor]
          )
          )
      }
    }

    it("l1 enabled, transactional distributed-cache") {
      withCache[String, String]("distributedL1TxCache") { cache =>
        cache.getAdvancedCache.getInterceptorChain.asScala.map(_.getClass) should contain theSameElementsInOrderAs (
          Seq(
            classOf[DistributionBulkInterceptor[_, _]],
            classOf[InvocationContextInterceptor],
            classOf[CacheMgmtInterceptor],
            classOf[StateTransferInterceptor],
            classOf[TransactionSynchronizerInterceptor],
            classOf[TxInterceptor[_, _]],
            classOf[OptimisticLockingInterceptor],
            classOf[NotificationInterceptor],
            classOf[L1LastChanceInterceptor],
            classOf[EntryWrappingInterceptor],
            classOf[L1TxInterceptor],
            classOf[TxDistributionInterceptor],
            classOf[CallInterceptor]
          )
          )
      }
    }

    it("distributed-cache, put/get") {
      withCache[String, String]("distributedCache") { cache =>
        cache.put("key1", "value1")
        cache.get("key1") should be("value1")
      }
    }
  }

  protected def withCache[K, V](cacheName: String, numInstances: Int = 1)(fun: Cache[K, V] => Unit): Unit = {
    val managers = (1 to numInstances).map(_ => new DefaultCacheManager("infinispan.xml"))
    val cache = managers(0).getCache[K, V](cacheName)

    try {
      fun(cache)
    } finally {
      managers.foreach(_.getCache[K, V](cacheName).stop())
      managers.foreach(_.stop())
    }
  }
}
