package org.stacktrace.yo.flixbot.search

import org.scalatest.FunSuite

class FlixRequestTest extends FunSuite {


  test("parses terms correctly") {
    val in = "!action racing !romance thriller THRILLER racing !https://www.imdb.com/title/tt10003008/ https://www.imdb.com/title/tt0468569/?ref_=nv_sr_srsg_0"
    val req = FlixRequest(in)
    assert(req.negTerms sameElements Seq("action", "romance"))
    assert(req.negMovie sameElements Seq("tt10003008"))
    assert(req.posTerms sameElements Seq("racing", "thriller"))
    assert(req.posMovie sameElements Seq("tt0468569"))
  }

}
