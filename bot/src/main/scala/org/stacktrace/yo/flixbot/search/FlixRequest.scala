package org.stacktrace.yo.flixbot.search

import java.util.regex.Pattern

import org.stacktrace.yo.flixbot.IMDBSearch

case class FlixRequest(posTerms: Array[String], posMovie: Array[String], negTerms: Array[String], negMovie: Array[String]){
  lazy val totalPosTerms: Int = posTerms.length + posMovie.length
}

object FlixRequest {
  private val IMDB_URL = Pattern.compile("http[s]*://(?:.*\\.|.*)imdb.com/[tT]itle[?/](..\\d+)")


  def apply(raw: String): FlixRequest = {
    val (neg, pos) = raw.toLowerCase.split(" ").distinct.partition(negativeTerm)
    val (negMovie, negTerms) = neg.partition(looksLikeIMDB)
    val (posMovie, posTerms) = pos.partition(looksLikeIMDB)
    FlixRequest(posTerms, posMovie.map(asDocTag), negTerms.map(removeNegIndicator), negMovie.map(negativeMovie))

  }

  private def negativeTerm(term: String): Boolean = term.startsWith("!")

  private def removeNegIndicator(term: String): String = term.replace('!', ' ').trim

  private def negativeMovie(term: String): String = asDocTag(removeNegIndicator(term))

  private def asDocTag(query: String) = {
    val matcher = IMDB_URL.matcher(query)
    if (matcher.find()) {
      "*dt_" + matcher.group(1)
    } else {
      query
    }
  }

  private def looksLikeIMDB(query: String) = {
    IMDB_URL.matcher(query).find()
  }


}
