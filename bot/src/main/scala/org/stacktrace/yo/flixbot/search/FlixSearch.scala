package org.stacktrace.yo.flixbot.search

import org.stacktrace.yo.flixbot.scala.vector.search.PartitionedSearcher
import org.stacktrace.yo.flixbot.vector.io.KeyedVectorData
import org.stacktrace.yo.flixbot.vector.keyed.{AllInOneKeyedVectors, KeyedVectors, ShardedKeyVectors}
import org.stacktrace.yo.flixbot.vector.scoring.CosineScorer
import org.stacktrace.yo.flixbot.vector.search.KeyedSimilaritySearch

import scala.concurrent.ExecutionContext

class FlixSearch(docVectors: KeyedVectors, wordVectors: KeyedVectors)(implicit ec: ExecutionContext) {

  import org.stacktrace.yo.flixbot.util.Ternary._

  import scala.collection.JavaConverters._

  private lazy val cosineScorer = new CosineScorer

  def findSimilar(query: String): Search.Result = {
    val flixRequest = FlixRequest(query)
    val (posMovie, negMovie) = (flixRequest.posMovie, flixRequest.negMovie)
    val (posWords, negWords) = (flixRequest.posTerms, flixRequest.negTerms)

    val wordVec = (posWords.nonEmpty || negWords.nonEmpty) ? wv(posWords, negWords) | null
    val docVec = (posMovie.nonEmpty || negMovie.nonEmpty) ? dv(posMovie, negMovie) | null
    val allMean = docVectors.mean(Array(wordVec, docVec).filter(_ != null): _*)

    val docSimilarity = new KeyedSimilaritySearch(
      docVectors,
      new PartitionedSearcher(
        docVectors,
        cosineScorer,
        10 + flixRequest.totalPosTerms,
        4
      )
    )

    docSimilarity.mostSimilar(allMean)

  }


  private def wv(pos: Array[String], neg: Array[String]): Array[Double] = {
    wordVectors.mean(pos.toList.asJava, neg.toList.asJava)
  }

  private def dv(pos: Array[String], neg: Array[String]): Array[Double] = docVectors.mean(pos.toList.asJava, neg.toList.asJava)


}

object FlixSearch {

  def apply(path: String, sharding: Int)(implicit ec: ExecutionContext): FlixSearch = {
    new FlixSearch(
      new AllInOneKeyedVectors(KeyedVectorData.vectors(s"${path}/docs")),
      new ShardedKeyVectors(KeyedVectorData.vectors(s"${path}/words"), sharding)
    )
  }

}
