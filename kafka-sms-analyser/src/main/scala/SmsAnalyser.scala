package io.github.malyszaryczlowiek

import org.apache.kafka.common.config.TopicConfig
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig, Topology}
import org.apache.kafka.streams.scala.StreamsBuilder
import org.apache.kafka.streams.scala.kstream.{Branched, BranchedKStream, Consumed, KStream, Materialized, Produced}
import org.apache.kafka.streams.scala.serialization.Serdes.stringSerde
import org.apache.kafka.streams.kstream.{GlobalKTable, Named}

import java.util.Properties
import config.AppConfig._
import model.Sms
import serdes.CustomSerdes._

import io.github.malyszaryczlowiek.kessengerlibrary.kafka.{Done, Error, TopicCreator, TopicSetup}
import io.github.malyszaryczlowiek.util.UriSearcher

//import play.api.Configuration.logger.logger

import sttp.client3._
import sttp.model.StatusCode
import upickle.default._



class SmsAnalyser {



  def main(args: Array[String]): Unit = {


    // define types for easier managing streams
    type Uri              = String
    type UriList          = List[Uri]
    type Nulll            = String    // this type means string is null for sure
    type UserNum          = String
    type NullOrUserNum    = String
    type UserStatusOrNull = String
    type Confidence       = String
    type ConfidenceOrNull = String    // may be confidence or null string


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



    /*
    Building topology
     */



    // pool stream with sms. key is null
    val smsStream: KStream[Nulll, Sms] = builder.stream( smsInputTopicName )(Consumed `with` (stringSerde, smsSerde))




    // wczytuję informację z użytkownikami i czy mają aktywną usługę
    // zapisuję to w globalną tabelę tak aby było dostępne pomiędzy wszytkie instancje aplikacji
    // w tej tabeli klucz to user number a wartość to boolean zapisany jako string z info czy ma aktywną usługę
    val userTable: GlobalKTable[UserNum, UserStatusOrNull] = builder.globalTable(
      userStatusTopicName,
      Materialized.as("user_table")(stringSerde,stringSerde)
    )(Consumed `with`(stringSerde, stringSerde))




    // wczytuję informacje o stronach i ich confidence level
    // to też jest globalna tabela
    // tutaj kluczem jest uri a wartością jest confidence level
    val uriTable: GlobalKTable[Uri, Confidence] =
      builder.globalTable(
        uriConfidenceTopicName,
        Materialized.as("uri_table")(stringSerde,stringSerde)
      )(Consumed `with`(stringSerde, stringSerde))


    val uriToCheckStream: KStream[Uri, Uri] = builder.stream( uriToCheckTopicName )(Consumed `with` (stringSerde, stringSerde))


    val uriSearcher = new UriSearcher


    val splitSmsWithUriOrNot = smsStream.split( Named.as("split_sms_uri_or_not") )
      .branch((nulll, sms) => {
        val uriList = uriSearcher.search(sms.message)
        uriList.nonEmpty
      }, Branched.as("sms_with_uri"))
      .branch((nulll, sms) => {
        val uriList = uriSearcher.search(sms.message)
        uriList.isEmpty
      }, Branched.as("sms_without_uri"))
      .noDefaultBranch()



    val smsWithUri: KStream[Nulll, Sms] = splitSmsWithUriOrNot.apply("sms_with_uri")


    val smsWithoutUri: KStream[Nulll, Sms] = splitSmsWithUriOrNot.apply("sms_without_uri")


    val splitPhishingService = smsWithoutUri.split(Named.as( "split_phishing_service_turn_on_off_or_do_nothing"))
      .branch((nulll, sms) => {
        // recipient must be different from defined in conf
        sms.recipient != serviceNumber
      }, Branched.as("service_no_change"))
      .branch((nulll, sms) => {
        // recipient number must be equal to service turn on/off number
        // and message must be stop or start
        sms.recipient == serviceNumber && (sms.message == "STOP" || sms.message == "START")
      }, Branched.as("change_service"))
      .noDefaultBranch()


    val serviceNoChange: KStream[Nulll, Sms] = splitPhishingService.apply("service_no_change")


    val changeService: KStream[Nulll, Sms] = splitPhishingService.apply("change_service")


    // we map our sms to user number who want to change service status.
    changeService.map( (nulll,sms) => {
      // null string in value means we activate service and null value delete user from GlobalKTable
      val status: String = if (sms.message == "STOP") "false" else null
      (sms.sender, status)
    }, Named.as("user_status_changes"))
      // and we save changes to user_status topic
      .to( userStatusTopicName )(Produced `with` (stringSerde, stringSerde))



    // back to sms with uri
    val smsUserNum: KStream[Sms, UserNum] = smsWithUri.map( (nulll,sms) => (sms, sms.sender))



    val checkingUserService: KStream[Sms, NullOrUserNum] = smsUserNum.leftJoin( userTable )(
      // join using userNum
      (sms, userNum) => userNum,
      // if userStatus is null this means there is no user in table
      // and user has active protection, if user status is "false",
      // this means user has protection turned off
      (userNum, userStatus) => userStatus
    )



    val splitSmsProtectedOrNot = checkingUserService.split(Named.as( "split_sms_protected_or_not"))
      .branch((sms, userStatus) => {
        // if userStatus is null this means user is protected
        userStatus == null
      }, Branched.as("protected"))
      .branch((sms, userStatus) => {
        // if userNum is not null and is false then user is not protected
        // remember we only put "false" or null as value to user_status topic
        userStatus != null && userStatus == "false"
      }, Branched.as("not_protected"))
      .noDefaultBranch()



    val smsWithUriProtected: KStream[Sms, Nulll] = splitSmsProtectedOrNot.apply("protected")


    val smsWithUriNotProtected1: KStream[Sms, UserNum] = splitSmsProtectedOrNot.apply("not_protected")


    val smsWithUriNotProtected2: KStream[Nulll, Sms] = smsWithUriNotProtected1.map((sms, userNum) => (null, sms))


    val smsNotProtectedAndNoUri: KStream[Nulll, Sms] = smsWithUriNotProtected2.merge( serviceNoChange )


    val uriListInSms: KStream[Sms, UriList] = smsWithUriProtected.map((sms,nulll) => {
      val uri: List[String] = uriSearcher.search( sms.message )
      (sms,uri)
    })


    val uriStream: KStream[Uri, Uri] = smsUserNum.flatMap((sms, userNumm) => uriSearcher.search(sms.message).map(uri => (uri,uri)) )


    val mergedUriStream: KStream[Uri, Uri] = uriToCheckStream.merge( uriStream )


    val uriWithConfidenceOrNull: KStream[Uri, ConfidenceOrNull]  = mergedUriStream.leftJoin( uriTable )(
      (uri,urii) => uri,
      (uri, confidenceOrNulll) => confidenceOrNulll
    )  // todo przy filtrowaniu muszę sprawdzić czy gdzieś jeszcze nie ma Named.as do wstawienia
      // we filter only uri which are not contained in our uriTable (GlobalKTable)
      .filter((uri, confidenceOrNull) => confidenceOrNull == null)
      // most blocking operation. We call phishingApi for Confidence level of uri

    // todo tuaj muszę zimplementować odpytywanie PhishingApi
//      .map((uri, nulll) => {
//
//
//
//
//        (uri, confidenceOrNull)
//      })



















    // TODO tutaj jest jeszcze null
    val smsWithUriWithoutProtection: KStream[String, Sms] = null


    // todo to czeka na jeszcze jeden merge a potem zapisywane do topica sms_output
    serviceNoChange.merge(smsWithUriWithoutProtection)





















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
