package org.stacktrace.yo.flixbot.scala.vector.search

import java.util

import org.stacktrace.yo.flixbot.search.{Search, Searcher}

import scala.concurrent.Future

trait VecSearcher extends Searcher[Array[Double]] {

  import scala.collection.JavaConverters._

  def search(vec: Array[Double]): util.List[Search.Answer] = searchVec(vec).asJava

  def searchVec(vec: Array[Double]): Seq[Search.Answer]

  def searchVecAsync(vec: Array[Double]): Future[Seq[Search.Answer]]


}
