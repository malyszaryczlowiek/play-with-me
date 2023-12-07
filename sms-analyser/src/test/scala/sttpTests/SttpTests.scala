package io.github.malyszaryczlowiek
package sttpTests

import org.scalatest.funsuite.AnyFunSuite
import sttp.client3.HttpClientSyncBackend
import sttp.client3.quick._
import sttp.model.StatusCode
import upickle.default._

class SttpTests extends AnyFunSuite {


  val googleApi = s"https://webrisk.googleapis.com/v1eap1:evaluateUri"
  private val backend = HttpClientSyncBackend()

  case class PhishingRequestBody(uri: String, threatTypes: List[String], allowScan: Boolean)
  case class PhishingResponseBody()



  test("sending simplest post request to example backend") {
    val response = basicRequest
      .body("""{"hello":"Hello","world":"world!"}""")
      .post(uri"https://httpbin.org/post?hello=world").send(backend)
    val responseBody = response.body
    val responseCode = response.code
    println(s"code: $responseCode")
    assert(responseCode == StatusCode.Ok, s"is not OK code")
    println(responseBody)
    responseBody match {
      case Left( string ) =>
        println(s"$string")
      case Right( string ) =>
        println(s"$string")
    }
  }





  test("sending post request to Google API (without authentication)") {

    implicit val readWriter: ReadWriter[PhishingRequestBody] = macroRW[PhishingRequestBody]
    val requestBody = PhishingRequestBody("testUri", List("SOCIAL_ENGINEERING"), false)
    val requestBodyJson = write( requestBody )
    val response = basicRequest
      .body( requestBodyJson )
      .post(uri"$googleApi").send(backend)
    val responseBody = response.body
    val responseCode = response.code
    println(s"code: $responseCode")
    assert(responseCode == StatusCode.Forbidden, s"status code without authentication should be forbidden ")
    println(responseBody)
    responseBody match {
      case Left(string) =>
        println(s"$string")
      case Right(string) =>
        println(s"$string")
    }


  }

}
