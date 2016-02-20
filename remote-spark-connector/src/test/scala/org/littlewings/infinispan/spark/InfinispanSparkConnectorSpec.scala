package org.littlewings.infinispan.spark

import java.util.Properties
import java.util.concurrent.TimeUnit

import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.{Seconds, Duration, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder
import org.infinispan.client.hotrod.exceptions.HotRodClientException
import org.infinispan.client.hotrod.marshall.ProtoStreamMarshaller
import org.infinispan.client.hotrod.{RemoteCache, RemoteCacheManager, Search}
import org.infinispan.protostream.annotations.ProtoSchemaBuilder
import org.infinispan.query.dsl.Query
import org.infinispan.query.remote.client.ProtobufMetadataManagerConstants
import org.infinispan.spark._
import org.infinispan.spark.rdd.InfinispanRDD
import org.infinispan.spark.stream.InfinispanInputDStream
import org.scalatest.{FunSpec, Matchers}

class InfinispanSparkConnectorSpec extends FunSpec with Matchers {
  describe("Infinispan Spartk Connector Spec") {
    it("simple InfinispanRDD") {
      withRemoteCache[String, SimpleBook]("namedCache") { cache =>
        SimpleBook.sourceBooks.foreach(b => cache.put(b.isbn, b))

        withSpark { sc =>
          val properties = new Properties
          properties.setProperty("infinispan.client.hotrod.server_list", "localhost:11222")
          properties.setProperty("infinispan.rdd.cacheName", cache.getName)

          val rdd = new InfinispanRDD[String, SimpleBook](sc, properties)

          rdd.values.map(_.price).sum.toInt should be(24171)
        }
      }
    }

    it("use DStream") {
      withRemoteCache[String, SimpleBook]("namedCache") { cache =>
        withStreaming(Seconds(1)) { ssc =>
          val properties = new Properties
          properties.setProperty("infinispan.client.hotrod.server_list", "localhost:11222")
          properties.setProperty("infinispan.rdd.cacheName", cache.getName)

          val stream =
            new InfinispanInputDStream[String, SimpleBook](ssc, StorageLevel.MEMORY_ONLY, properties)

          stream.foreachRDD { rdd =>
            rdd.foreach(s => println(s"isbn: ${s._1}, title: ${s._2.title}, event: ${s._3}"))
          }

          ssc.start()

          TimeUnit.SECONDS.sleep(2)

          SimpleBook.sourceBooks.foreach(b => cache.put(b.isbn, b))

          TimeUnit.SECONDS.sleep(2)

          // ssc.awaitTermination()
        }
      }
    }

    it("Query InfinispanRDD, No ProtoBuffers") {
      withRemoteCache[String, SimpleBook]("namedCache") { cache =>
        SimpleBook.sourceBooks.foreach(b => cache.put(b.isbn, b))

        withSpark { sc =>
          val properties = new Properties
          properties.setProperty("infinispan.client.hotrod.server_list", "localhost:11222")
          properties.setProperty("infinispan.rdd.cacheName", cache.getName)

          val rdd = new InfinispanRDD[String, SimpleBook](sc, properties)

          val thrown =
            the[HotRodClientException] thrownBy
              Search
                .getQueryFactory(cache)
                .from(classOf[SimpleBook])
                .having("price")
                .gte(4000)
                .toBuilder
                .build

          thrown.getMessage should include("The cache manager must be configured with a ProtoStreamMarshaller")
        }
      }
    }

    it("Query InfinispanRDD") {
      withRemoteCache[String, Book]("indexingCache", true) { cache =>
        val manager = cache.getRemoteCacheManager

        val context = ProtoStreamMarshaller.getSerializationContext(manager)
        val protoSchemaBuilder = new ProtoSchemaBuilder
        val idl = protoSchemaBuilder
          .fileName(classOf[Book].getName)
          .addClass(classOf[Book])
          .build(context)

        val metaCache = manager.getCache[String, String](ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME)
        metaCache.put(classOf[Book].getName + ".proto", idl)

        Book.sourceBooks.foreach(b => cache.put(b.isbn, b))

        withSpark { sc =>
          val properties = new Properties
          properties.setProperty("infinispan.client.hotrod.server_list", "localhost:11222")
          properties.setProperty("infinispan.rdd.cacheName", cache.getName)

          val rdd = new InfinispanRDD[String, Book](sc, properties)

          val query: Query =
            Search
              .getQueryFactory(cache)
              .from(
                classOf[Book])
              .having("price")
              .gte(4000)
              .toBuilder
              .build

          val filteredRdd = rdd.filterByQuery[Book](query, classOf[Book])

          filteredRdd.values.map(_.price).sum.toInt should be(10711)
        }
      }
    }

    it("write to Infinispan") {
      withRemoteCache[String, SimpleBook]("namedCache") { cache =>
        withSpark { sc =>
          val properties = new Properties
          properties.setProperty("infinispan.client.hotrod.server_list", "localhost:11222")
          properties.setProperty("infinispan.rdd.cacheName", cache.getName)

          val simpleRdd = sc.parallelize(SimpleBook.sourceBooks.map(b => (b.isbn, b)))
          simpleRdd.writeToInfinispan(properties)

          cache should have size(6L)
          cache.get("978-4048662024").title should be("高速スケーラブル検索エンジン ElasticSearch Server")
        }
      }
    }
  }

  protected def withSpark(f: SparkContext => Unit): Unit = {
    val conf = new SparkConf().setMaster("local[*]").setAppName("Infinispan Spark Connector Test")
    val sc = new SparkContext(conf)

    try {
      f(sc)
    } finally {
      sc.stop()
    }
  }

  protected def withStreaming(duration: Duration)(f: StreamingContext => Unit): Unit = {
    withSpark {
      sc =>
        val ssc = new StreamingContext(sc, duration)

        try {
          f(ssc)
        } finally {
          ssc.stop(true)
        }
    }
  }

  protected def withRemoteCache[K, V](cacheName: String, useProtoStream: Boolean = false)(f: RemoteCache[K, V] => Unit): Unit = {
    val configurationBuilder = new ConfigurationBuilder
    configurationBuilder.addServers("localhost:11222")

    if (useProtoStream) {
      configurationBuilder.marshaller(new ProtoStreamMarshaller)
    }

    val manager = new RemoteCacheManager(configurationBuilder.build)

    val cache = manager.getCache[K, V](cacheName)

    try {
      f(cache)
      cache.clear()
    } finally {
      cache.stop()
      manager.stop()
    }
  }
}
