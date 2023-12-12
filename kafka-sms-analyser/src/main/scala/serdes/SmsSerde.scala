package io.github.malyszaryczlowiek
package serdes

import model.Sms
import org.apache.kafka.common.serialization.{Deserializer, Serde, Serializer}



class SmsSerde extends Serde[Sms] {

  override def serializer(): Serializer[Sms] =
    new SmsSerializer

  override def deserializer(): Deserializer[Sms] =
    new SmsDeserializer

}
