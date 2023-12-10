package io.github.malyszaryczlowiek
package stringSerdeTests

import org.scalatest.funsuite.AnyFunSuite
import org.apache.kafka.streams.scala.serialization.Serdes.stringSerde

class StringSerdeTests extends AnyFunSuite {


  test("testing serialization and deserialization of null") {
    val s: String = null
    val serialized = stringSerde.serializer().serialize("", s)
    val deserialized = stringSerde.deserializer().deserialize("", serialized)
    assert(s == deserialized && deserialized == null, "no null equality")
  }


  test("testing serialization and deserialization of null") {
    val s: String = null
    val serialized = stringSerde.serializer().serialize("", s)
    val deserialized = stringSerde.deserializer().deserialize("", serialized)
    assert(s == deserialized && deserialized == null, "no null equality")
  }


}
