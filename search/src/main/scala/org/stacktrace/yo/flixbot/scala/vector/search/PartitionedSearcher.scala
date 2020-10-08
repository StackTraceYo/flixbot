package org.stacktrace.yo.flixbot.scala.vector.search

import org.stacktrace.yo.flixbot.commons.AnswerQueue
import org.stacktrace.yo.flixbot.search.Search
import org.stacktrace.yo.flixbot.vector.keyed.KeyedVectors
import org.stacktrace.yo.flixbot.vector.scoring.Scorer

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}


class PartitionedSearcher(kv: KeyedVectors, scorer: Scorer, k: Int, partitions: Int)(implicit ec: ExecutionContext) extends VecSearcher {

  private lazy val aq = new AnswerQueue(k)

  override def searchVecAsync(vec: Array[Double]): Future[Seq[Search.Answer]] = {
    Future.traverse(partitions())(partition => Future {
      partition.search(vec)
    }).map(collectTop)
  }


  override def searchVec(vec: Array[Double]): Seq[Search.Answer] = Await.result(searchVecAsync(vec), Duration.Inf)

  private def collectTop(r: Iterator[Array[Search.Answer]]): Seq[Search.Answer] = {

    r.toSeq.flatten.foreach(a => aq.insertWithOverflow(a))

    val top = Array.ofDim[Search.Answer](k)
    for (i <- k - 1 to 0 by -1) {
      top(i) = aq.pop()
    }
    top.toSeq

  }

  def partitions(): Iterator[Partition] = kv.keys().grouped(partitions).map(p => new Partition(p))

  class Partition(val keys: Array[String]) {
    val pq = new AnswerQueue(k)

    def search(vec: Array[Double]): Array[Search.Answer] = {
      for (other <- keys) {
        val ov = kv.keyVector(other)
        val d = scorer.score(ov, vec)
        pq.insertWithOverflow(new Search.Answer(other, d))
      }
      val top = new Array[Search.Answer](k)
      for (i <- k - 1 to 0 by -1) {
        top(i) = pq.pop
      }
      top
    }
  }

}