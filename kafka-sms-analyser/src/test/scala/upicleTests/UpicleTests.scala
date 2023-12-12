package io.github.malyszaryczlowiek
package upicleTests

import model.PhishingRequestBody
import org.scalatest.funsuite.AnyFunSuite
import upickle.default._



class UpicleTests extends AnyFunSuite {


  test("json to object") {
    implicit val readWriter: ReadWriter[PhishingRequestBody] = macroRW[PhishingRequestBody]
    val json = """{"uri":"testUri","threatTypes":["SOCIAL_ENGINEERING"],"allowScan":false}"""
    val phishing = read[PhishingRequestBody](json)
    println(s"$phishing")
    val testWith = PhishingRequestBody("testUri", List("SOCIAL_ENGINEERING"), false)
    assert(phishing == testWith)
  }


  test("object to json") {
    implicit val readWriter: ReadWriter[PhishingRequestBody] = macroRW[PhishingRequestBody]
    val parsed = write(PhishingRequestBody("testUri", List("SOCIAL_ENGINEERING"), false))
    val json = """{"uri":"testUri","threatTypes":["SOCIAL_ENGINEERING"],"allowScan":false}"""
    println(s"$parsed")
    assert(parsed == json)
  }


  test("test with null") {
    implicit val readWriter: ReadWriter[PhishingRequestBody] = macroRW[PhishingRequestBody]
    val json = """{"uri":null,"threatTypes":["SOCIAL_ENGINEERING"],"allowScan":false}"""
    val phishing = read[PhishingRequestBody](json)
    println(s"$phishing")
    val testWith = PhishingRequestBody(null, List("SOCIAL_ENGINEERING"), false)
    assert(phishing == testWith)
  }



  test("object with null to json") {
    implicit val readWriter: ReadWriter[PhishingRequestBody] = macroRW[PhishingRequestBody]
    val parsed = write(PhishingRequestBody(null, List("SOCIAL_ENGINEERING"), false))
    val json = """{"uri":null,"threatTypes":["SOCIAL_ENGINEERING"],"allowScan":false}"""
    println(s"$parsed")
    assert(parsed == json)
  }


  test("testing seq of strings") {
    // commented line causes exception in seq case
    // implicit val readWriter: ReadWriter[Seq[String]] = macroRW[Seq[String]]
    val li = List("")
    val parsed = write(li)
    val json = """[""]"""
    println(s"$parsed")
    assert(parsed == json)
  }


  test("testing seq of strings with null") {
    // import upickle.default._ required
    val li = List("A", null, "C")
    val parsed = write(li)
    val json = """["A",null,"C"]"""
    println(s"$parsed")
    assert(parsed == json)
  }


  test("testing null") {
    // import upickle.default._ required
    val s: String = null
    val parsed = write(s)
    val json = """null"""
    println(s"$parsed")
    assert(parsed == json)
  }



  test("list of Strings to object") {
    // commented line throws exception!!!! and it was a problem
    // implicit val readWriter: ReadWriter[Seq[String]] = macroRW[Seq[String]]
    val json = """["A","B"]"""
    val list = read[Seq[String]](json)
    println(s"$list")
    val testWith = Seq("A","B")
    assert(list == testWith)
  }

}



/*
ZłE CUDZYSłowy !!! i deserializacja nie działa !!!
{“sender”:“234100200300”,“recipient”:“48700800999”,“message”:“Dzień dobry. W związku z audytem nadzór finansowy w naszym banku proszą o potwierdzanie danych pod adresem: https://www.m-bonk.pl.ng/personal-data”}

DOBRY
{"sender":"234100200300","recipient":"48700800999","message":"Dzień dobry. W związku z audytem nadzór finansowy w naszym banku proszą o potwierdzanie danych pod adresem: https://www.m-bonk.pl.ng/personal-data"}


{"sender":"234100200300","recipient":"48700800999","message":"coś innego"}
{"sender":"234100200300","recipient":"48700800999","message":"coś https://forbidden.com innego"}

// wyłączanie usługi
{"sender":"234100200300","recipient":"123456789","message":"STOP"}
// włączanie usługi
{"sender":"234100200300","recipient":"123456789","message":"START"}
 */
