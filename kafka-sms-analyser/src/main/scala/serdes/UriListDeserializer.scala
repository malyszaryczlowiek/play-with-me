package io.github.malyszaryczlowiek
package serdes

import io.github.malyszaryczlowiek.model.UriList
import org.apache.kafka.common.serialization.Deserializer
import upickle.default._

class UriListDeserializer extends Deserializer[UriList] {

  override def deserialize(topic: String, data: Array[Byte]): UriList = {
    val json = new String(data)
    implicit val uriListReadWriter: ReadWriter[UriList] = macroRW[UriList]
    read[UriList](json)
  }
}
