package org.littlewings.infinispan.lucene

import scala.collection.JavaConverters._
import scala.util.{ Failure, Success, Try }
import scala.io.StdIn

import org.apache.lucene.analysis.ja.JapaneseAnalyzer
import org.apache.lucene.document.{ Document, Field, TextField }
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search._
import org.apache.lucene.store.Directory
import org.infinispan.Cache
import org.infinispan.lucene.directory.DirectoryBuilder
import org.infinispan.manager.DefaultCacheManager

import org.littlewings.infinispan.lucene.ResourceWrappers._

object Searcher {
  def main(args: Array[String]): Unit =
    new Searcher(args(0)).start()
}

class Searcher(val nodeName: String) extends ConsoleLogger {
  def start(): Unit = {
    log("Start Searcher.")

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
    } queryWhile(directory)

    log("Stop Searcher.")
  }

  private def queryWhile(directory: Directory): Unit = {
    log("query while. [exit] teminate, this prompt.")

    val analyzer = new JapaneseAnalyzer
    val searcherManager = new SearcherManager(directory, new SearcherFactory)

    val queryParser = new QueryParser("content", analyzer)
    val limit = 1000

    def parser = parseQuery(queryParser) _

    Iterator
      .continually(StdIn.readLine(s"$nodeName:Query> "))
      .withFilter(line => line != null && !line.isEmpty)
      .takeWhile(_ != "exit")
      .map(parser)
      .withFilter(_.isSuccess)
      .foreach { query =>
        searcherManager.maybeRefreshBlocking()
        val searcher = searcherManager.acquire

        search(searcher, query.get, limit) match {
          case (totalHits, hits) if totalHits > 0 =>
            show(s"  ${totalHits}件ヒットしました")

            hits.foreach { h =>
              val hitDoc = searcher.doc(h.doc)

              show(s"     ScoreDoc[score/id] => [${h.score}/${h.doc}]: Doc => " +
                hitDoc
                .getFields
                .asScala
                .map(_.stringValue)
                .mkString("|"))
            }
          case _ =>
            println("  ヒット件数は0です")
        }

        searcherManager.release(searcher)
      }
  }

  private def parseQuery(queryParser: QueryParser)(queryString: String): Try[Query] =
    Try(queryParser.parse(queryString)).recoverWith {
      case e =>
        show(s"Invalid Query[$queryString], Reason: $e.")
        Failure(e)
    }

  private def search(searcher: IndexSearcher, query: Query, limit: Int): (Int, Array[ScoreDoc]) = {
    show(s"  Input Query => [$query].")

    val totalHitCountCollector = new TotalHitCountCollector
    searcher.search(query, totalHitCountCollector)
    val totalHits = totalHitCountCollector.getTotalHits

    val docCollector = TopFieldCollector.create(Sort.RELEVANCE,
      limit,
      true,  // fillFields
      true,  // trackDocScores
      false,  // traxMaxScore
      false)  // docScoreInOrder

    searcher.search(query, docCollector)
    val topDocs = docCollector.topDocs
    val hits = topDocs.scoreDocs

    (totalHits, hits)
  }
}
