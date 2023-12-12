package io.github.malyszaryczlowiek
package serdes


import model.Sms
import org.apache.kafka.common.serialization.Serializer
import upickle.default._

class SmsSerializer extends Serializer[Sms] {

  override def serialize(topic: String, data: Sms): Array[Byte] = {
    implicit val readWriter: ReadWriter[Sms] = macroRW[Sms]
    val parsed = write( data )
    parsed.getBytes
  }

}
