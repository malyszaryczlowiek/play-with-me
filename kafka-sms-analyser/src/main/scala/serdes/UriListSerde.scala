package io.github.malyszaryczlowiek
package serdes

import model.{Sms, UriList}

import org.apache.kafka.common.serialization.{Deserializer, Serde, Serializer}


class UriListSerde extends Serde[UriList]{

  override def serializer(): Serializer[UriList] =
    new UriListSerializer

  override def deserializer(): Deserializer[UriList] =
    new UriListDeserializer

}
