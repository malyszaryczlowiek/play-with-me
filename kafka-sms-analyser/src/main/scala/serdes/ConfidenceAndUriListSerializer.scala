package io.github.malyszaryczlowiek
package serdes

import model.{ConfidenceAndUriList, UriList}

import org.apache.kafka.common.serialization.Serializer
import upickle.default._


class ConfidenceAndUriListSerializer extends Serializer[ConfidenceAndUriList]{

  override def serialize(topic: String, data: ConfidenceAndUriList): Array[Byte] = {
    implicit val uriListReadWriter: ReadWriter[UriList] = macroRW[UriList]
    implicit val readWriter: ReadWriter[ConfidenceAndUriList] = macroRW[ConfidenceAndUriList]
    val parsed = write(data)
    parsed.getBytes
  }

}
