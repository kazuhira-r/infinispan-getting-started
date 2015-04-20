package org.littlewings.infinispan.lucene

import scala.io.StdIn

import org.apache.lucene.analysis.ja.JapaneseAnalyzer
import org.apache.lucene.document.{ Document, Field, TextField }
import org.apache.lucene.index.{ IndexWriter, IndexWriterConfig }
import org.apache.lucene.store.Directory
import org.apache.lucene.util.Version
import org.infinispan.Cache
import org.infinispan.lucene.directory.DirectoryBuilder
import org.infinispan.manager.DefaultCacheManager

import org.littlewings.infinispan.lucene.ResourceWrappers._

object Indexer {
  def main(args: Array[String]): Unit = {
    new Indexer("Indexer").start()
  }
}

class Indexer(val nodeName: String) extends ConsoleLogger {
  def start(): Unit = {
    log("Start Indexer.")

    for {
      manager <- new DefaultCacheManager("infinispan.xml")
      metadataCache <- manager.getCache[Any, Any]("metadataCache")
      dataChunksCache <- manager.getCache[Any, Any]("dataChunksCache")
      locksCache <- manager.getCache[Any, Any]("locksCache")
      directory <- DirectoryBuilder.newDirectoryInstance(metadataCache,
                                                         dataChunksCache,
                                                         locksCache,
                                                         "myIndex")
                                   .chunkSize(30 * 1024 * 1024)  // とりあえず30k
                                   .create
    } indexingWhile(directory)

    log("Stop Indexer.")
  }

  private def indexingWhile(directory: Directory): Unit = {
    log("add Document. [exit] teminate, this prompt.")

    val analyzer = new JapaneseAnalyzer

    for (writer <- new IndexWriter(directory, new IndexWriterConfig(Version.LUCENE_4_10_3, analyzer))) {
      // なにか作っていないと、クラスタ参加メンバーがDirectoryのopen時に
      // org.apache.lucene.index.IndexNotFoundException: no segments* file found in InfinispanDirectory
      // となってしまうので、空コミット
      writer.commit()

      Iterator
        .continually(StdIn.readLine("addDocument> "))
        .withFilter(line => line != null && !line.isEmpty)
        .takeWhile(_ != "exit")
        .foreach { content =>
          val document = new Document
          document.add(new TextField("content", content, Field.Store.YES))
          writer.addDocument(document)
          writer.commit()
          show(s"[$content] added.")
      }
    }
  }
}

