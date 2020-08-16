package org.stacktrace.yo.flixbot.search

import org.scalatest.FunSuite
import org.stacktrace.yo.flixbot.movie.MovieMapping

import scala.concurrent.ExecutionContextExecutor
import scala.collection.JavaConverters._

class FlixSearchTest extends FunSuite {

  implicit val ec: ExecutionContextExecutor = scala.concurrent.ExecutionContext.global
  private val fs = FlixSearch("/Users/ahmad/projects/flixbot/search/", 4)
  val mm: MovieMapping = MovieMapping("mapping.txt")

  test("can search"){
    fs.findSimilar("https://www.imdb.com/title/tt0117500/ https://www.imdb.com/title/tt0473705/ thriller")
      .answers.asScala
      .foreach(a => {
        println(a.name)
        println(mm.mapping(a.name) + " - "  + a.score)
      })
  }

}
