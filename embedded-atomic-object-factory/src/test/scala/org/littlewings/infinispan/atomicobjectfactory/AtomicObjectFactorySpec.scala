package org.littlewings.infinispan.atomicobjectfactory

import org.infinispan.Cache
import org.infinispan.atomic.AtomicObjectFactory
import org.infinispan.manager.DefaultCacheManager
import org.scalatest.FunSpec
import org.scalatest.Matchers._

class AtomicObjectFactorySpec extends FunSpec {
  describe("AtomicObjectFactory Spec") {
    it("using HashSet") {
      withCache[AnyRef, AnyRef]("atomicFactoryCache", 2) { cache =>
        val factory = new AtomicObjectFactory(cache)
        val set = factory.getInstanceOf(classOf[java.util.HashSet[String]], "set")

        set.add("Java")
        set.add("Scala")
        set.add("Groovy")
        set.add("Clojure")

        factory.disposeInstanceOf(classOf[java.util.HashSet[String]], "set", true)

        withCache[AnyRef, AnyRef]("atomicFactoryCache", 1) { nextCache =>
          val nextFactory = new AtomicObjectFactory(nextCache)
          val setAsSaved =
            nextFactory.getInstanceOf(classOf[java.util.HashSet[String]], "set", false, null, false)

          setAsSaved should have size (4)
          setAsSaved should contain only("Java", "Scala", "Groovy", "Clojure")

          nextCache should have size (1)
          nextCache.containsKey("HashSet#set") should be(true)
          nextCache.get("HashSet#set").isInstanceOf[Array[Byte]] should be(true)
        }
      }
    }

    it("using HashSet, AtomicObjectFactory#forCache") {
      withCache[String, Array[Byte]]("atomicFactoryCache", 2) { cache =>
        val factory = AtomicObjectFactory.forCache(cache)
        val set =
          factory.getInstanceOf(
            classOf[java.util.HashSet[String]],
            "set", // key
            false, // withReadOptimization
            null, // equalsMethod
            true) // forceNew

        set.add("Java")
        set.add("Scala")
        set.add("Groovy")
        set.add("Clojure")

        factory.disposeInstanceOf(classOf[java.util.HashSet[String]], "set", true)

        withCache[String, Array[Byte]]("atomicFactoryCache", 1) { nextCache =>
          val nextFactory = AtomicObjectFactory.forCache(nextCache)
          val setAsSaved =
            nextFactory.getInstanceOf(classOf[java.util.HashSet[String]], "set", false, null, false)

          setAsSaved should have size (4)
          setAsSaved should contain only("Java", "Scala", "Groovy", "Clojure")

          nextCache should have size (1)
          nextCache.containsKey("HashSet#set") should be(true)
          nextCache.get("HashSet#set").isInstanceOf[Array[Byte]] should be(true)
        }
      }
    }

    it("using ArrayList") {
      withCache[String, Array[Byte]]("atomicFactoryCache", 2) { cache =>
        val factory = AtomicObjectFactory.forCache(cache)
        val list = factory.getInstanceOf(classOf[java.util.ArrayList[String]], "list")

        list.add("Java")
        list.add("Scala")
        list.add("Groovy")
        list.add("Clojure")

        factory.disposeInstanceOf(classOf[java.util.ArrayList[String]], "list", true)

        withCache[String, Array[Byte]]("atomicFactoryCache", 1) { nextCache =>
          val nextFactory = AtomicObjectFactory.forCache(cache)
          val listAsSaved =
            nextFactory.getInstanceOf(classOf[java.util.ArrayList[String]], "list", false, null, false)

          listAsSaved should have size (4)
          listAsSaved should contain only("Java", "Scala", "Groovy", "Clojure")

          nextCache should have size (1)
          nextCache.containsKey("ArrayList#list") should be(true)
          nextCache.get("ArrayList#list").isInstanceOf[Array[Byte]] should be(true)
        }
      }
    }

    it("using HashSet, no stored, missing disposeInstanceOf") {
      withCache[String, Array[Byte]]("atomicFactoryCache", 2) { cache =>
        val factory = AtomicObjectFactory.forCache(cache)
        val set = factory.getInstanceOf(classOf[java.util.HashSet[String]], "set")

        set.add("Java")
        set.add("Scala")
        set.add("Groovy")
        set.add("Clojure")

        // factory.disposeInstanceOf(classOf[java.util.HashSet[String]], "set", true)

        withCache[String, Array[Byte]]("atomicFactoryCache", 1) { nextCache =>
          val nextFactory = AtomicObjectFactory.forCache(nextCache)
          val setAsSaved =
            nextFactory.getInstanceOf(classOf[java.util.HashSet[String]], "set", false, null, false)

          setAsSaved should have size (0)
          setAsSaved should be(empty)

          nextCache should have size (1)
          nextCache.containsKey("HashSet#set") should be(true)
        }
      }
    }

    it("using HashSet, no stored, disposeInstanceOf false") {
      withCache[String, Array[Byte]]("atomicFactoryCache", 2) { cache =>
        val factory = AtomicObjectFactory.forCache(cache)
        val set = factory.getInstanceOf(classOf[java.util.HashSet[String]], "set")

        set.add("Java")
        set.add("Scala")
        set.add("Groovy")
        set.add("Clojure")

        factory.disposeInstanceOf(classOf[java.util.HashSet[String]], "set", false)

        withCache[String, Array[Byte]]("atomicFactoryCache", 1) { nextCache =>
          val nextFactory = AtomicObjectFactory.forCache(nextCache)
          val setAsSaved =
            nextFactory.getInstanceOf(classOf[java.util.HashSet[String]], "set", false, null, false)

          setAsSaved should have size (0)
          setAsSaved should be(empty)

          nextCache should have size (1)
          nextCache.containsKey("HashSet#set") should be(true)
        }
      }
    }

    ignore("using ArrayList, no stored") {
      withCache[String, Array[Byte]]("atomicFactoryCache", 2) { cache =>
        val factory = AtomicObjectFactory.forCache(cache)
        val list = factory.getInstanceOf(classOf[java.util.ArrayList[String]], "list")

        list.add("Java")
        list.add("Scala")
        list.add("Groovy")
        list.add("Clojure")

        // factory.disposeInstanceOf(classOf[java.util.ArrayList[String]], "list", true)

        withCache[String, Array[Byte]]("atomicFactoryCache", 1) { nextCache =>
          val nextFactory = AtomicObjectFactory.forCache(cache)
          val listAsSaved =
            nextFactory.getInstanceOf(classOf[java.util.ArrayList[String]], "list", false, null, false)

          listAsSaved should have size (0)
          listAsSaved should be(empty)

          nextCache should have size (1)
          nextCache.containsKey("ArrayList#list") should be(true)
        }
      }
    }

    it("using HashSet, ignored?") {
      withCache[String, Array[Byte]]("atomicFactoryCache", 2) { cache =>
        val factory = AtomicObjectFactory.forCache(cache)
        val set = factory.getInstanceOf(classOf[java.util.HashSet[String]], "set")

        set.add("Java")
        set.add("Scala")

        factory.disposeInstanceOf(classOf[java.util.HashSet[String]], "set", true)

        set.add("Groovy")

        withCache[String, Array[Byte]]("atomicFactoryCache", 1) { nextCache =>
          val nextFactory = AtomicObjectFactory.forCache(nextCache)
          val setAsSaved =
            nextFactory.getInstanceOf(classOf[java.util.HashSet[String]], "set", false, null, false)

          setAsSaved should have size (0)
          setAsSaved should be(empty)

          nextCache should have size (1)
          nextCache.containsKey("HashSet#set") should be(true)
        }
      }
    }

    it("using HashSet, direct") {
      withCache[AnyRef, AnyRef]("atomicFactoryCache", 2) { cache =>
        val set = new java.util.HashSet[String]
        set.add("Java")
        set.add("Scala")
        set.add("Groovy")
        set.add("Clojure")

        cache.put("set", set)

        withCache[AnyRef, AnyRef]("atomicFactoryCache", 1) { nextCache =>
          val setAsSaved = nextCache.get("set").asInstanceOf[java.util.HashSet[String]]
          setAsSaved should have size (4)
          setAsSaved should contain only("Java", "Scala", "Groovy", "Clojure")

          nextCache should have size (1)
          nextCache.containsKey("set") should be(true)
        }
      }
    }

    it("using HashSet, compare AtomicObjectFactory and direct") {
      withCache[AnyRef, AnyRef]("atomicFactoryCache", 2) { cache =>
        val factory = AtomicObjectFactory.forCache(cache)

        // AtomicObjectFactoryから作成
        val setFromFactory = factory.getInstanceOf(classOf[java.util.HashSet[String]], "setFromFactory")
        setFromFactory.add("Java")
        setFromFactory.add("Scala")
        setFromFactory.add("Groovy")
        setFromFactory.add("Clojure")

        factory.disposeInstanceOf(classOf[java.util.HashSet[String]], "setFromFactory", true)

        val setFromFactoryOuter = factory.getInstanceOf(classOf[java.util.HashSet[String]], "setFromFactory", false, null, false)

        // 直接インスタンスを作成
        val setDirect = new java.util.HashSet[String]
        setDirect.add("Java")
        setDirect.add("Scala")
        setDirect.add("Groovy")
        setDirect.add("Clojure")

        cache.put("setDirect", setDirect)

        val setDirectOuter = cache.get("setDirect").asInstanceOf[java.util.HashSet[String]]

        withCache[AnyRef, AnyRef]("atomicFactoryCache", 1) { nextCache =>
          val nextFactory = AtomicObjectFactory.forCache(nextCache)
          val setFromFactoryAsSaved =
            nextFactory.getInstanceOf(classOf[java.util.HashSet[String]], "setFromFactory", false, null, false)
          setFromFactoryAsSaved should have size (4)
          setFromFactoryAsSaved should contain only("Java", "Scala", "Groovy", "Clojure")

          val setFromDirectAsSaved = nextCache.get("setDirect").asInstanceOf[java.util.HashSet[String]]
          setFromFactoryAsSaved should have size (4)
          setFromFactoryAsSaved should contain only("Java", "Scala", "Groovy", "Clojure")

          // 外側にいたインスタンスに、メンバー追加
          setFromFactoryOuter should be(empty)
          setFromFactoryOuter.add("Kotlin")
          setDirectOuter.add("Kotlin")

          // AtomicObjectFactoryで管理している方には反映される
          setFromFactoryAsSaved should have size (5)
          setFromFactoryAsSaved should contain only("Java", "Scala", "Groovy", "Clojure", "Kotlin")

          // 直接インスタンスを扱っている方には、反映されない
          setFromDirectAsSaved should have size (4)
          setFromDirectAsSaved should contain only("Java", "Scala", "Groovy", "Clojure")
        }
      }
    }

    it("using HashSet, step AtomicObjectFactory") {
      withCache[String, Array[Byte]]("atomicFactoryCache", 2) { cache =>
        val factory = AtomicObjectFactory.forCache(cache)

        // AtomicObjectFactoryから作成
        val set = factory.getInstanceOf(classOf[java.util.HashSet[String]], "set")
        set.add("Java")
        set.add("Scala")
        set.add("Groovy")
        set.add("Clojure")

        factory.disposeInstanceOf(classOf[java.util.HashSet[String]], "set", true)

        withCache[String, Array[Byte]]("atomicFactoryCache", 1) { nextCache =>
          val nextFactory = AtomicObjectFactory.forCache(nextCache)
          val setAsSaved =
            nextFactory.getInstanceOf(classOf[java.util.HashSet[String]], "set", false, null, false)
          setAsSaved.add("Kotlin")
          nextFactory.disposeInstanceOf(classOf[java.util.HashSet[String]], "set", true)
        }

        withCache[String, Array[Byte]]("atomicFactoryCache", 1) { nextCache =>
          val nextFactory = AtomicObjectFactory.forCache(nextCache)
          val setAsSaved =
            nextFactory.getInstanceOf(classOf[java.util.HashSet[String]], "set", false, null, false)
          setAsSaved should have size (5)
          setAsSaved should contain only("Java", "Scala", "Groovy", "Clojure", "Kotlin")
          nextFactory.disposeInstanceOf(classOf[java.util.HashSet[String]], "set", true)
        }

        val set2 =
          factory.getInstanceOf(classOf[java.util.HashSet[String]], "set", false, null, false)
        set2 should have size (5)
        set2 should contain only("Java", "Scala", "Groovy", "Clojure", "Kotlin")
      }
    }

    it("using My Updatable") {
      withCache[String, Array[Byte]]("atomicFactoryCache", 2) { cache =>
        val factory = AtomicObjectFactory.forCache(cache)
        val person =
          factory
            .getInstanceOf(classOf[Person],
              "person", // key
              false, // withReadOptimization
              null, // equalsMethod
              true, // forceNew
              "カツオ", "磯野", Integer.valueOf(11)) // args

        person.getFirstName should be("カツオ")
        person.getLastName should be("磯野")
        person.getAge should be(11)

        person.setFirstName("ワカメ")
        person.setAge(9)

        factory.disposeInstanceOf(classOf[Person], "person", true)

        withCache[String, Array[Byte]]("atomicFactoryCache", 1) { nextCache =>
          val nextFactory = AtomicObjectFactory.forCache(nextCache)
          val personAsSaved =
            nextFactory.getInstanceOf(classOf[Person], "person", false, null, false)

          personAsSaved.getFirstName should be("ワカメ")
          personAsSaved.getLastName should be("磯野")
          personAsSaved.getAge should be(9)
        }
      }
    }
  }

  protected def withCache[K, V](cacheName: String, numInstances: Int = 1)(f: Cache[K, V] => Unit): Unit = {
    val managers = (1 to numInstances).map(_ => new DefaultCacheManager("infinispan.xml"))

    try {
      val cache = managers.map(_.getCache[K, V](cacheName)).head

      f(cache)

      cache.stop()
    } finally {
      managers.foreach(_.stop())
    }
  }
}
