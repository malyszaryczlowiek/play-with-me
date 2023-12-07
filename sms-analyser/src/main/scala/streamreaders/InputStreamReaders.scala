package io.github.malyszaryczlowiek
package streamreaders

import io.github.malyszaryczlowiek.config.AppConfig.{externalKafkaServers, internalKafkaBroker, smsInputTopicName, userStatusTopicName}
import io.github.malyszaryczlowiek.model.{Sms, UriWithConfidenceLevel, UserActiveService}
import io.github.malyszaryczlowiek.parsers.Parsers.{kafkaSmsInputRowParser, kafkaUriWithConfidenceStatusRowParser, kafkaUserActiveServiceRowParser}
import org.apache.spark.sql.{Dataset, SparkSession}

object InputStreamReaders {


  /**
   * metoda wczytuje dane z kafki (z external broker) i mapuje je do typu Dataseet[Sms]
   * dzięki czemu będziemy mogli używać nazw atrybutów Sms jako nazw kolumn
   * np $sender
   *
   * @param sparkSession
   * @return
   */
  def readSmsStream(sparkSession: SparkSession): Dataset[Sms] = {

    val df = sparkSession
      .readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", externalKafkaServers)
      .option("subscribe", smsInputTopicName)
      //.option("startingOffsets", "earliest")
      //.option("endingOffsets",   "latest")
      .load()

    // dodane dla implicit Encoding
    import sparkSession.implicits._

    // tutaj zmapowany input stream ma typ Sms
    // dzięki czemu
    val inputStream: Dataset[Sms] = df.map(kafkaSmsInputRowParser)

    println(s"\n### SMS INITIAL SCHEMA: ###\n")
    inputStream.printSchema()
    // nazwa tego temp view służy jako nazwa tabeli dla dalszych zapytań sql
    inputStream.createOrReplaceTempView(s"sms_input_table")
    inputStream
  }




  /**
   * W tym strumieniu wczytujemy dane z internal kafka broker
   * z topica user_status w któym przechowywane są dane czy dany użytkownik
   * ma aktywną usługę.
   *
   * @param sparkSession
   * @return
   */
  def readUserActiveServiceStream(sparkSession: SparkSession): Dataset[UserActiveService] = {
    val df = sparkSession
      .readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", internalKafkaBroker.servers)
      .option("subscribe", userStatusTopicName)
      //.option("startingOffsets", "earliest")
      //.option("endingOffsets",   "latest")
      .load()

    // dodane dla implicit Encoding
    import sparkSession.implicits._

    // tutaj zmapowany input stream ma typ Sms
    // dzięki czemu
    val inputStream: Dataset[UserActiveService] = df.map( kafkaUserActiveServiceRowParser )

    println(s"\n### UserActiveService INITIAL SCHEMA: ###\n")
    inputStream.printSchema()
    // nazwa tego temp view służy jako nazwa tabeli dla dalszych zapytań sql
    inputStream.createOrReplaceTempView(s"user_status_input_table")
    inputStream
  }





  def readUriAndConfidenceStatusStream(sparkSession: SparkSession): Dataset[UriWithConfidenceLevel] = {
    val df = sparkSession
      .readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", internalKafkaBroker.servers)
      .option("subscribe", userStatusTopicName)
      //.option("startingOffsets", "earliest")
      //.option("endingOffsets",   "latest")
      .load()

    // dodane dla implicit Encoding
    import sparkSession.implicits._

    // tutaj zmapowany input stream ma typ Sms
    // dzięki czemu
    val inputStream: Dataset[UriWithConfidenceLevel] = df.map( kafkaUriWithConfidenceStatusRowParser )

    println(s"\n### UriWithConfidenceLevel INITIAL SCHEMA: ###\n")
    inputStream.printSchema()
    // nazwa tego temp view służy jako nazwa tabeli dla dalszych zapytań sql
    inputStream.createOrReplaceTempView(s"uri_input_table")
    inputStream
  }



}
