package io.github.malyszaryczlowiek
package serdes


import org.apache.kafka.common.serialization.Deserializer
import model.Sms
import mappers.Mappers.mapStringToSms



class SmsDeserializer extends Deserializer[Sms] {

  override def deserialize(topic: String, data: Array[Byte]): Sms = {
    val json = new String(data)
    mapStringToSms(json)
  }

}
