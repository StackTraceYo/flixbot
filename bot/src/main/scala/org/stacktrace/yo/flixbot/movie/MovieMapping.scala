package org.stacktrace.yo.flixbot.movie

import scala.io.Source


case class MovieMapping(mapping: Map[String, (String, String)])

object MovieMapping {

  def apply(path: String): MovieMapping = {
    val source = Source.fromFile(path)
    val mapping: Map[String, (String, String)] = source.getLines().map(line => {
      val split = line.split(",")
      val title = split.head
      val id = split.last
      s"*dt_${id}" -> (title, id)
    }).toMap
    source.close()
    MovieMapping(mapping)
  }

}
