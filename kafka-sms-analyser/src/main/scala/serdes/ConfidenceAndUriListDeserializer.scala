package io.github.malyszaryczlowiek
package serdes

import model.{ConfidenceAndUriList, UriList}

import org.apache.kafka.common.serialization.Deserializer
import upickle.default._

class ConfidenceAndUriListDeserializer extends Deserializer[ConfidenceAndUriList] {

  override def deserialize(topic: String, data: Array[Byte]): ConfidenceAndUriList = {
    val json = new String(data)
    implicit val uriListReadWriter: ReadWriter[UriList] = macroRW[UriList]
    implicit val confidenceAndUriListReadWriter: ReadWriter[ConfidenceAndUriList] = macroRW[ConfidenceAndUriList]
    read[ConfidenceAndUriList](json)
  }
}
