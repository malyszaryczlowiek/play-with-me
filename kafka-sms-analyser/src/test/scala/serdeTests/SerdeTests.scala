package io.github.malyszaryczlowiek
package serdeTests

import org.scalatest.funsuite.AnyFunSuite
import org.apache.kafka.streams.scala.serialization.Serdes.stringSerde
import serdes.CustomSerdes._

import io.github.malyszaryczlowiek.model.ConfidenceLevel.CONFIDENCE_LEVEL_UNSPECIFIED
import io.github.malyszaryczlowiek.model.{ConfidenceAndUriList, UriList}

class SerdeTests extends AnyFunSuite {


  test("testing serialization and deserialization of null") {
    val s: String = null
    val serialized = stringSerde.serializer().serialize("", s)
    val deserialized = stringSerde.deserializer().deserialize("", serialized)
    assert(s == deserialized && deserialized == null, "no null equality")
  }




  test("testing Confidence and UriList serde ") {
    val serde = confidenceAndUriListSerde

    val data = ConfidenceAndUriList("conf", UriList(List("A","B")))
    val ser   = serde.serializer().serialize("", data)
    val deser = serde.deserializer().deserialize("", ser)

    assert(deser == data, "nie jest równy")
  }


  test("test if throw NullPointerException or check second condition.") {
    val confidenceAndUriList: ConfidenceAndUriList = ConfidenceAndUriList(null, UriList(List.empty[String]))
    assert( confidenceAndUriList.confidence == null || confidenceAndUriList.confidence == CONFIDENCE_LEVEL_UNSPECIFIED )
    assert( confidenceAndUriList.confidence == CONFIDENCE_LEVEL_UNSPECIFIED || confidenceAndUriList.confidence == null )
  }

}
