package org.littlewings.infinispan.clusterexecutor

import java.util.function.Predicate

import org.infinispan.Cache
import org.infinispan.manager.{DefaultCacheManager, EmbeddedCacheManager}
import org.infinispan.remoting.transport.Address
import org.infinispan.util.{SerializableFunction, SerializableRunnable, TriConsumer}
import org.jboss.logging.Logger
import org.scalatest.{FunSpec, Matchers}

class ClusterExecutorSpec extends FunSpec with Matchers with Serializable {
  describe("Cluster Executor Spec") {
    it("local cache, execute") {
      withCache[String, String]("localCache") { cache =>
        cache.getCacheManager.executor.execute((() => {
          Logger.getLogger(classOf[ClusterExecutorSpec]).infof("Thread[%s] Execute!!", Thread.currentThread.getName)
        }): Runnable)
      }
    }

    it("replicated cache, execute") {
      withCache[String, String]("replicatedCache", 3) { cache =>
        cache.getCacheManager.executor.execute((() => {
          Logger.getLogger(classOf[ClusterExecutorSpec]).infof("Thread[%s] Execute!!", Thread.currentThread.getName)
        }): SerializableRunnable)
      }
    }

    it("distributed cache, execute") {
      withCache[String, String]("distributedCache", 3) { cache =>
        cache.getCacheManager.executor.execute((() => {
          Logger.getLogger(classOf[ClusterExecutorSpec]).infof("Thread[%s] Execute!!", Thread.currentThread.getName)
        }): SerializableRunnable)
      }
    }

    it("distributed cache, filter and execute") {
      withCache[String, String]("distributedCache", 3) { cache =>
        val self = cache.getAdvancedCache.getRpcManager.getAddress

        cache
          .getCacheManager
          .executor
          .filterTargets((address => self == address): Predicate[Address])
          .execute((() => {
            Logger.getLogger(classOf[ClusterExecutorSpec]).infof("Thread[%s] Execute!!", Thread.currentThread.getName)
          }): SerializableRunnable)
      }
    }

    it("local cache, submit") {
      withCache[String, String]("localCache") { cache =>
        val completableFuture =
          cache.getCacheManager.executor.submit((() => {
            Logger.getLogger(classOf[ClusterExecutorSpec]).infof("Thread[%s] Submit!!", Thread.currentThread.getName)
          }): Runnable)

        completableFuture.join()
      }
    }

    it("replicated cache, submit") {
      withCache[String, String]("replicatedCache", 3) { cache =>
        val completableFuture =
          cache.getCacheManager.executor.submit((() => {
            Logger.getLogger(classOf[ClusterExecutorSpec]).infof("Thread[%s] Submit!!", Thread.currentThread.getName)
          }): SerializableRunnable)

        completableFuture.join()
      }
    }

    it("distributed cache, submit") {
      withCache[String, String]("distributedCache", 3) { cache =>
        val completableFuture =
          cache.getCacheManager.executor.submit((() => {
            Logger.getLogger(classOf[ClusterExecutorSpec]).infof("Thread[%s] Submit!!", Thread.currentThread.getName)
          }): SerializableRunnable)

        completableFuture.join()
      }
    }

    it("local cache, submitConsumer") {
      withCache[String, String]("localCache") { cache =>
        val completableFuture =
          cache
            .getCacheManager
            .executor
            .submitConsumer[String](
            ((manager: EmbeddedCacheManager) => {
              Logger
                .getLogger(classOf[ClusterExecutorSpec])
                .infof("Thread[%s] callable!!", Thread.currentThread.getName)
              manager.getAddress.toString
            }): java.util.function.Function[_ >: EmbeddedCacheManager, _ <: String],
            ((address: Address, value: String, thrown: Throwable) => {
              Logger
                .getLogger(classOf[ClusterExecutorSpec])
                .infof("Thread[%s] triConsumer!!, arg Address[%s], value Address[%s]",
                  Array(Thread.currentThread.getName, address, value): _*)
            }): TriConsumer[_ >: Address, _ >: String, _ >: Throwable]
          )

        completableFuture.join()
      }
    }

    it("replicated cache, submitConsumer") {
      withCache[String, String]("replicatedCache", 3) { cache =>
        val completableFuture =
          cache
            .getCacheManager
            .executor
            .submitConsumer[String](
            ((manager: EmbeddedCacheManager) => {
              Logger
                .getLogger(classOf[ClusterExecutorSpec])
                .infof("Thread[%s] callable!!", Thread.currentThread.getName)
              manager.getAddress.toString
            }): SerializableFunction[_ >: EmbeddedCacheManager, _ <: String],
            ((address: Address, value: String, thrown: Throwable) => {
              Logger
                .getLogger(classOf[ClusterExecutorSpec])
                .infof("Thread[%s] triConsumer!!, arg Address[%s], value Address[%s]",
                  Array(Thread.currentThread.getName, address, value): _*)
            }): TriConsumer[_ >: Address, _ >: String, _ >: Throwable]
          )

        completableFuture.join()
      }
    }

    it("distributed cache, submitConsumer") {
      withCache[String, String]("distributedCache", 3) { cache =>
        val completableFuture =
          cache
            .getCacheManager
            .executor
            .submitConsumer[String](
            ((manager: EmbeddedCacheManager) => {
              Logger
                .getLogger(classOf[ClusterExecutorSpec])
                .infof("Thread[%s] callable!!", Thread.currentThread.getName)
              manager.getAddress.toString
            }): SerializableFunction[_ >: EmbeddedCacheManager, _ <: String],
            ((address: Address, value: String, thrown: Throwable) => {
              Logger
                .getLogger(classOf[ClusterExecutorSpec])
                .infof("Thread[%s] triConsumer!!, arg Address[%s], value Address[%s]",
                  Array(Thread.currentThread.getName, address, value): _*)
            }): TriConsumer[_ >: Address, _ >: String, _ >: Throwable]
          )

        completableFuture.join()
      }
    }
  }

  protected def withCache[K, V](cacheName: String, numInstances: Int = 1)(f: Cache[K, V] => Unit): Unit = {
    val managers = (1 to numInstances).map(_ => new DefaultCacheManager("infinispan.xml"))

    managers.foreach(_.getCache[K, V](cacheName))

    try {
      val cache = managers(0).getCache[K, V](cacheName)
      f(cache)
      cache.stop()
    } finally {
      managers.foreach(_.getCache[K, V](cacheName).stop())
      managers.foreach(_.stop())
    }
  }
}
