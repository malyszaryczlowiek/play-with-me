package io.github.malyszaryczlowiek

import org.apache.kafka.common.config.TopicConfig
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig, Topology}
import org.apache.kafka.streams.scala.StreamsBuilder
import org.apache.kafka.streams.scala.kstream.{Consumed, KStream, Materialized}
import org.apache.kafka.streams.scala.serialization.Serdes.{intSerde, longSerde, shortSerde, stringSerde}
import org.apache.kafka.streams.kstream.{GlobalKTable, Named}

import java.util.Properties
import config.AppConfig._
import model.Sms

import io.github.malyszaryczlowiek.kessengerlibrary.kafka.{Done, Error, TopicCreator, TopicSetup}

//import play.api.Configuration.logger.logger




class SmsAnalyser {


  def main(args: Array[String]): Unit = {


    // Define properties for KafkaStreams object
    val properties: Properties = new Properties()
    properties.put(StreamsConfig.APPLICATION_ID_CONFIG,    appId)
    properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBroker.servers)
    properties.put(StreamsConfig.STATE_DIR_CONFIG,         kafkaBroker.fileStore)


    /*
    Create required topics to save analyses.
     */
    createKafkaTopic()


    // define builder
    val builder: StreamsBuilder = new StreamsBuilder()

    // wczytuję strumień z sms'ami
    val smsStream: KStream[String, Sms] = builder.stream( smsInputTopicName )(Consumed `with`(stringSerde, stringSerde))
      // wykonuję mapowanie gdzie klucz i tak jest null a sms jest w value
      .map( (k,v) => (k, mappers.Mappers.mapStringToSms(v)) , Named.as("sms_stream") )




    // wczytuję informację z użytkownikami i czy mają aktywną usługę
    // zapisuję to w globalną tabelę tak aby było dostępne pomiędzy wszytkie instancje aplikacji
    // w tej tabeli klucz to user number a wartość to boolean zapisany jako string z info czy ma aktywną usługę
    val userGlobalTable: GlobalKTable[String, String] = builder.globalTable(
      userStatusTopicName,
      Materialized.as("user_table")(stringSerde,stringSerde)
    )(Consumed `with`(stringSerde, stringSerde))




    // wczytuję informacje o stronach i ich confidence level
    // to też jest globalna tabela
    // tutaj kluczem jest uri a wartością jest confidence level
    val uriConfidenceLevelGlobalTable: GlobalKTable[String, String] =
      builder.globalTable(
        uriConfidenceTopicName,
        Materialized.as("uri_table")(stringSerde,stringSerde)
      )(Consumed `with`(stringSerde, stringSerde))




    // todo napisać join smsStream z groupedUriTable joinedSmsUriStream

    // sprawdzić jescze cogroup
    // stream.peek() do wywołania zapytania do google API ???

    // todo filtrować po rekordach z nullowym uri z  joinedSmsUriStream i te które będą null trzeba przebadać za pomocą google Api

    // todo groupuje userStream po user i powstaje userStreamGrouped

    // todo ponownie bierzemy joinedSmsUriStream i robimy join z userStreamGrouped powstaje joinedSmsUriUserStream

    // todo biorę joinedSmsUriUserStream i filtruję
    //  jeśli uri jest null to przechodzi
    //  jeśli uri nie jest null a użytkownik nie ma aktywnej usługi to przechodzi
    //  jeśli uri nie jest null a użytkownik ma aktywną usługę i level jest poniżej dopuszczalnego to przechodzi
    //  jeśli uri nie jest null a użytkownik ma aktywną usługę i level jest powyżej dopuszczalnego to NIE przechodzi
    //  to co przeszło dajemy na output na exxternal broker


    // todo zrobić join smsStream z userStream

    // todo filtrowanie jeśli nr recivera jest zgodny z numerem włączania i wyłączania usługi a message jest START albo STOP to
    //  przekierowujemy ten stream do zapisania w userActiveService
    //


    // we build topology of the streams and tables
    val topology: Topology = builder.build()


    /*
      Main loop of program
    */
    var continue = true

    while (continue) {

      // create KafkaStreams object
      val streams: KafkaStreams = new KafkaStreams(topology, properties)


      // we initialize shutdownhook only once.
      // if initializeShutDownHook then
      Runtime.getRuntime.addShutdownHook(new Thread("closing_stream_thread") {
        override
        def run(): Unit =
          streams.close()
        // logger.warn(s"Streams closed from ShutdownHook.")
      })


      // we starting streams
      try {
        streams.start()
        // logger.trace(s"Streams started.")
      } catch {
        case e: Throwable =>
          // logger.error(s"Error during Streams starting: ${e.toString}.")
          continue = false
          System.exit(1)
      }
      // logger.trace(s"SmsAnalyser started.")
      Thread.sleep(120_000) // two minutes sleep.
      streams.close()
      // logger.trace(s"Streams stopped.")
    }

  }





  /**
   * na internal kafka broker tworzę dwa wymienione wyżej topici
   * używam do tego API
   */
  private def createKafkaTopic(): Unit = {

    val topicConfig = Map(
      TopicConfig.CLEANUP_POLICY_CONFIG -> TopicConfig.CLEANUP_POLICY_DELETE,
      TopicConfig.RETENTION_MS_CONFIG -> "-1" // keep all logs forever
    )

    val uriConfidenceTopic =
      TopicSetup( uriConfidenceTopicName , kafkaBroker.servers, kafkaBroker.partitionNum, kafkaBroker.replicationFactor, topicConfig)

    val userActiveServiceTopic =
      TopicSetup( userStatusTopicName , kafkaBroker.servers, kafkaBroker.partitionNum, kafkaBroker.replicationFactor, topicConfig)

    /*
    W tym miejscu tworzymy topici na internal kafka broker.
    W przypadku ponownego uruchomienia aplikacji nie ma problemu,
    że topici istnieją po prostu zostanie zwrócony kafka.Error
    i zostanie to zapsane do logów
     */
    TopicCreator.createTopic(uriConfidenceTopic) match {
      case Done =>
        // logger.info(s"Topic '$uriConfidenceTopicName' created")
      case Error(error) =>
        // logger.error(s"Creation topic '$uriConfidenceTopicName' failed with error: $error")
    }

    TopicCreator.createTopic(userActiveServiceTopic) match {
      case Done =>
        // logger.info(s"Topic '$userStatusTopicName' created")
      case Error(error) =>
        // logger.error(s"Creation topic '$userStatusTopicName' failed with error: $error")
    }
  }

}
