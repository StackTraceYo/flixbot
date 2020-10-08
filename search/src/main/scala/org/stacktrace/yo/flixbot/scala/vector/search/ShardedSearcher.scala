package org.stacktrace.yo.flixbot.scala.vector.search

import org.stacktrace.yo.flixbot.commons.AnswerQueue
import org.stacktrace.yo.flixbot.search.Search
import org.stacktrace.yo.flixbot.vector.keyed.KeyedVectors
import org.stacktrace.yo.flixbot.vector.scoring.Scorer
import org.stacktrace.yo.flixbot.vector.search.TopKSearcher

import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class ShardedSearcher(shards: Array[KeyedVectors], scorer: Scorer, k: Int)(implicit ec: ExecutionContext) extends VecSearcher {

  private lazy val pq = new AnswerQueue(k)

  override def searchVecAsync(vec: Array[Double]): Future[Seq[Search.Answer]] = {
    Future.traverse(shards.toSeq)(shard => {
      Future {
        new TopKSearcher(shard, scorer, k).search(vec).asScala
      }
    }).map(collectTop)
  }


  override def searchVec(vec: Array[Double]): Seq[Search.Answer] = Await.result(searchVecAsync(vec), Duration.Inf)

  private def collectTop(r: Seq[Seq[Search.Answer]]): Seq[Search.Answer] = {

    r.flatten.foreach(a => pq.insertWithOverflow(a))

    val top = Array.ofDim[Search.Answer](k)
    for (i <- k - 1 to 0 by -1) {
      top(i) = pq.pop()
    }
    top.toSeq

  }
}