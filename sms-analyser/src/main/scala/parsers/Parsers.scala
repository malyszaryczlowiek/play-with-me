package io.github.malyszaryczlowiek
package parsers

import org.apache.spark.sql.{Encoder, Row}
import upickle.default._
import model.{Sms, UriWithConfidenceLevel, UserActiveService}


/**
 * Zdefiniowane tutaj parsery pomogą przetworzyć dane przechowywane w kafce
 * gdzie key i value są w postaci Array[Byte]
 * Opis z dokumentacją jakie atrybuty oprócz key i value są przechowywane w kafce
 * można znaleźć w tabelce:
 * https://spark.apache.org/docs/latest/structured-streaming-kafka-integration.html#creating-a-kafka-source-for-batch-queries
 *
 */
object Parsers {


  implicit val smsReadWriter: ReadWriter[Sms] = macroRW[Sms]



  def kafkaSmsInputRowParser: Row => Sms = (r: Row) => {
    // keys and values from kafka are always stored as Array[Byte] in broker
    // so I need to extract them to normal Sms object
    // but input key is null so we do not process it
    val mByteArray: Array[Byte] = r.getAs[Array[Byte]]("value")
    val json: String = new String(mByteArray)
    // conversion from string json to Sms object
    val sms: Sms = read[Sms](json)
    sms
  }

  // implicit val userActiveServiceReadWriter: ReadWriter[UserActiveService] = macroRW[UserActiveService]


  def kafkaUserActiveServiceRowParser: Row => UserActiveService = (r: Row) => {
    val userNumberByte = r.getAs[Array[Byte]]("key")
    val isActiveByte   = r.getAs[Array[Byte]]("value")
    val userNumber: String = new String(userNumberByte)
    val isServiceActive    = new String(isActiveByte).toBoolean
    UserActiveService(userNumber, isServiceActive)
  }


  // implicit val uriWithConfidenceStatusReadWriter: ReadWriter[UriWithConfidenceLevel] = macroRW[UriWithConfidenceLevel]


  def kafkaUriWithConfidenceStatusRowParser: Row => UriWithConfidenceLevel = (r: Row) => {
    val uriByte             = r.getAs[Array[Byte]]("key")
    val confidenceLevelByte = r.getAs[Array[Byte]]("value")
    val uriString       = new String(uriByte)
    val confidenceLevel = new String(confidenceLevelByte)
    UriWithConfidenceLevel(uriString, confidenceLevel)
  }






}
