package io.github.malyszaryczlowiek
package util

import model.{ConfidenceLevel, PhishingRequestBody, PhishingResponseBody, Score, ThreatType}
import config.AppConfig.PhishingService

import sttp.client3._
import sttp.model.StatusCode
import upickle.default._


class PhishingApiCaller extends Serializable {


  /**
   * In this method we call external Service to check
   * phishing is social engineering technique so we focus
   * only on this type of thread.
   *
   * @param uri uri which security concerns we want check.
   * @return Method returns confidence as string. If service is inactive or we cannot get result we return null
   *         which indicates that that site is still not checked. next time when we will
   *         process data with this uri it will be checked once again.
   */
  def check(uriToCheck: String, phishingService: PhishingService): String = {
//    val forbiden = List("https://forbidden.com", "https://foo.org")
//    if (forbiden.contains(uriToCheck)) ConfidenceLevel.HIGH
//    else ConfidenceLevel.LOW
    val backend = HttpClientSyncBackend()
    implicit val requestReadWriter: ReadWriter[PhishingRequestBody] = macroRW[PhishingRequestBody]
    val requestBody = PhishingRequestBody(uriToCheck, List(ThreatType.SOCIAL_ENGINEERING), true)
    val requestBodyJson = write(requestBody)
    val request = basicRequest
      .body(requestBodyJson)
    if (phishingService.headers.nonEmpty) request.headers( phishingService.headers )
    val response = request.post(uri"${phishingService.server}").send( backend )
    val responseBody = response.body
    val responseCode = response.code
    if (responseCode == StatusCode.Ok) {
      responseBody match {
        case Left( errorString ) => null
        case Right( bodyJson ) =>
          implicit val scoreReadWriter: ReadWriter[Score] = macroRW[Score]
          implicit val readWriter: ReadWriter[PhishingResponseBody] = macroRW[PhishingResponseBody]
          val rb: PhishingResponseBody = read[PhishingResponseBody]( bodyJson )
          rb.scores.find(_.threatType == ThreatType.SOCIAL_ENGINEERING) match {
            case Some( score ) => score.confidenceLevel
            case None => null
          }
      }
    } else {
      null
    }
  }

}

/*
{"sender":"234100200300","recipient":"48700800999","message":"coś https://forbiddennn.com innego https://foo.org"}
{"sender":"234100200300","recipient":"48700800999","message":"coś https://foo.org innego"}

{"sender":"234100200300","recipient":"48700800999","message":"coś https://forbiddennn.com innego https://fooo.org"}
 */