package io.github.malyszaryczlowiek
package mappers

import upickle.default._
import model.{Sms, UriWithConfidenceLevel, UserActiveService}


object Mappers {


  def mapStringToSms(json: String): Sms = {
    implicit val smsReadWriter: ReadWriter[Sms] = macroRW[Sms]
    read[Sms](json) // Returns Sms
  }


  /**
   *
   * @param k key - we know that this is user number saved as string
   * @param v value - information if user has active phising status
   * @return
   */
//  def mapUserActiveService(k: String, v: String): UserActiveService = UserActiveService(k, v.toBoolean)









}
