package io.github.malyszaryczlowiek
package streamTests

import org.apache.kafka.common.serialization.Serdes.StringSerde
import org.apache.kafka.streams.kstream.{Grouped, Named, TimeWindows, Windowed}
import org.apache.kafka.streams.scala.StreamsBuilder
import org.apache.kafka.streams.scala.kstream._
import org.apache.kafka.streams.scala.serialization.Serdes.{intSerde, longSerde, shortSerde, stringSerde}
import org.apache.kafka.streams.state.KeyValueStore
import org.apache.kafka.streams._
import org.apache.kafka.streams.scala.serialization.Serdes



class StreamTests extends munit.FunSuite {

  /*

  Documentation for KafkaStreams testing

  https://kafka.apache.org/documentation/streams/developer-guide/testing.html
  https://kafka.apache.org/32/javadoc/org/apache/kafka/streams/test/package-summary.html
  https://kafka.apache.org/32/javadoc/org/apache/kafka/streams/package-summary.html

  */

  // serdes
  private val stringSerdee = stringSerde


  private var topology: Topology = _
  private var inputTopic: TestInputTopic[String, String] = _

  // output topics
  private var sms_output: TestOutputTopic[String, String] = _


  private var testDriver: TopologyTestDriver = _
  private var store: KeyValueStore[String, String] = _





  override def beforeAll(): Unit = {
    super.beforeAll()
  }


  override def beforeEach(context: BeforeEach): Unit = {
    super.beforeEach(context)
  }


  override def afterEach(context: AfterEach): Unit = {
    if (testDriver != null) testDriver.close()
    super.afterEach(context)
  }






}
