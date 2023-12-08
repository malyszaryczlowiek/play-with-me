package io.github.malyszaryczlowiek
package util

import org.scalatest.funsuite.AnyFunSuite


class UriSearcherTests extends AnyFunSuite {

  val searcher = new UriSearcher()


  test("regex test for whitespaces spliting") {
    val u0 = s"coś innego niż nic"
    val r = "\\s".r
    assert( r.split(u0).toList.length == 4, "długość się nie zgadza" )

  }


  test("UriSearcher splits text, finds uri and removes    ") {

    val u0 = s"coś innego niż nic"
    val u1 = s"cośtam http://foo"
    val u2 = s"cośtam http://foo yyy"
    val u3 = s"cośtam http://foo yyy https://bar"
    val u4 = s"cośtam (http://foo) yyy"

    val s0 = searcher.search( u0 )
    val s1 = searcher.search( u1 )
    val s2 = searcher.search( u2 )
    val s3 = searcher.search( u3 )
    val s4 = searcher.search( u4 )

    assert(s0 == List.empty)
    assert(s1 == List("http://foo"))
    assert(s2 == List("http://foo"))
    assert(s3 == List("http://foo", "https://bar" ))
    assert(s4 == List("http://foo)" )) //  nie ucina ostatniego ')' bo w ścieżce mogą być różne inne znaki
    // w kolejnych implementacjach można dopracować
  }




}
