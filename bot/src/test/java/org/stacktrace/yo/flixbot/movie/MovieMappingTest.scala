package org.stacktrace.yo.flixbot.movie

import org.scalatest.FunSuite

class MovieMappingTest extends FunSuite {


  test("mapping can be created"){
    val mm = MovieMapping("mapping.txt")
    assert(mm.mapping.size == 10000)
  }

}
