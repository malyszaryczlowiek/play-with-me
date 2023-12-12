package io.github.malyszaryczlowiek
package serdes

import model.ConfidenceAndUriList

import org.apache.kafka.common.serialization.{Deserializer, Serde, Serializer}


class ConfidenceAndUriListSerde extends Serde[ConfidenceAndUriList]{

  override def serializer(): Serializer[ConfidenceAndUriList] =
    new ConfidenceAndUriListSerializer

  override def deserializer(): Deserializer[ConfidenceAndUriList] =
    new ConfidenceAndUriListDeserializer

}
