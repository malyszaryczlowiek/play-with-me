package io.github.malyszaryczlowiek
package serdes


import org.apache.kafka.common.serialization.Deserializer
import model.Sms
import upickle.default._


class SmsDeserializer extends Deserializer[Sms] {

  override def deserialize(topic: String, data: Array[Byte]): Sms = {
    val json = new String(data)
    implicit val smsReadWriter: ReadWriter[Sms] = macroRW[Sms]
    read[Sms](json) // Returns Sms
  }

}
