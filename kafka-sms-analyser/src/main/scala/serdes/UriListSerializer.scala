package io.github.malyszaryczlowiek
package serdes

import io.github.malyszaryczlowiek.model.UriList
import org.apache.kafka.common.serialization.Serializer
import upickle.default._


class UriListSerializer extends Serializer[UriList]{

  override def serialize(topic: String, data: UriList): Array[Byte] = {
    implicit val readWriter: ReadWriter[UriList] = macroRW[UriList]
    val parsed = write(data)
    parsed.getBytes
  }

}
