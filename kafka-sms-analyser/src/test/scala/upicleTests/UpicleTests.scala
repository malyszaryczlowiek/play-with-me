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
    // import upickle.default._ required
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
    // zakomentowana poniżej linijka wyżuca wyjątek !!!! I to powodowało problemy
    // implicit val readWriter: ReadWriter[Seq[String]] = macroRW[Seq[String]]
    val json = """["A","B"]"""
    val list = read[Seq[String]](json)
    println(s"$list")
    val testWith = Seq("A","B")
    assert(list == testWith)
  }






}






/*

sealed trait ThreatType
case object SOCIAL_ENGINEERING extends ThreatType


// https://com-lihaoyi.github.io/upickle/#CaseClasses
sealed trait ThreatType
object ThreatType {
  implicit val rw: ReadWriter[ThreatType] = ReadWriter.merge(SOCIAL_ENGINEERING.rw)
}
case class SOCIAL_ENGINEERING() extends ThreatType
object SOCIAL_ENGINEERING {
  implicit val rw: ReadWriter[SOCIAL_ENGINEERING] = macroRW
}


jeśli napiszę test:

implicit val readWriter: ReadWriter[ThreatPhishingRequestBody] = macroRW[ThreatPhishingRequestBody]
val parsed = write(ThreatPhishingRequestBody("testUri", List(SOCIAL_ENGINEERING()), false))
// val json = """{"uri":"testUri","threatTypes":["SOCIAL_ENGINEERING"],"allowScan":false}"""
println(s"$parsed")
// assert(parsed == json)

 dla takiej formy zwróci, a takiego obiektu nie łyknie google API

{"uri":"testUri","threatTypes":[{"$type":"io.github.malyszaryczlowiek.upicleTests.UpicleTests.SOCIAL_ENGINEERING"}],"allowScan":false}

*/