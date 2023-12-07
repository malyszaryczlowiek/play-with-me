package io.github.malyszaryczlowiek

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.kafka.common.config.TopicConfig
import org.apache.spark.sql.{Dataset, SparkSession}
import config.AppConfig._
import model.{Sms, UriWithConfidenceLevel, UserActiveService}
import streamreaders.InputStreamReaders._
import kessengerlibrary.kafka
import kessengerlibrary.kafka.{Done, TopicCreator, TopicSetup}




class SmsAnalyser
object SmsAnalyser {

  private val logger: Logger = LogManager.getLogger(classOf[SmsAnalyser])
  logger.trace(s"SmsAnalyser application starting.")

  private val topicConfig = Map(
    TopicConfig.CLEANUP_POLICY_CONFIG -> TopicConfig.CLEANUP_POLICY_DELETE,
    TopicConfig.RETENTION_MS_CONFIG -> "-1" // keep all logs forever
  )

  // dwa topici które będziemy tworzyć na internal kafka broker

  /**
   * w tym topicu przechowywane będą informacje o stopniu niebezpieczeństwa strony i tak...
   * w kluczu będzie uri strony jako string
   * a w wartości będzie confidence level też jako string przechowywany
   */
  val dangerousUri: String = uriConfidenceLevelTopicName

  /**
   * w tym topicu kluczem jest nr użytkownika
   * a wartością boolean true/false czy usługa sprawdzania phishingu jest aktywna
   */
  val userActiveService: String = userStatusTopicName




  def main(args: Array[String]): Unit = {
    createKafkaTopic()
    startStreaming()
    val sess = prepareSparkSession
  }


  /**
   * na internal kafka broker tworzę dwa wymienione wyżej topici
   * używam do tego API
   */
  private def createKafkaTopic(): Unit   = {
    val dangerousUriTopic =
      TopicSetup(dangerousUri, internalKafkaBroker.servers, internalKafkaBroker.partitionNum, internalKafkaBroker.replicationFactor, topicConfig)

    val userActiveServiceTopic =
      TopicSetup(userActiveService, internalKafkaBroker.servers, internalKafkaBroker.partitionNum, internalKafkaBroker.replicationFactor, topicConfig)

    /*
    W tym miejscu tworzymy topici na internal kafka broker.
    W przypadku ponownego uruchomienia aplikacji nie ma problemu,
    że topici istnieją po prostu zostanie zwrócony kafka.Error
    i zostanie to zapsane do logów
     */
    TopicCreator.createTopic(dangerousUriTopic) match {
      case Done =>
        logger.info(s"Topic '$dangerousUri' created")
      case kafka.Error(error) =>
        logger.error(s"Creation topic '$dangerousUri' failed with error: $error")
    }

    TopicCreator.createTopic(userActiveServiceTopic) match {
      case Done =>
        logger.info(s"Topic '$userActiveService' created")
      case kafka.Error(error) =>
        logger.error(s"Creation topic '$userActiveService' failed with error: $error")
    }
  }



  /**
   * przygotowuję obiekt SparkSession
   */
  private def prepareSparkSession: SparkSession = {
    val sparkSession = SparkSession
      .builder
      .appName(appId)
      // two config below added to solve
      // Initial job has not accepted any resources;
      // check your cluster UI to ensure that workers
      // are registered and have sufficient resources
      //      .config("spark.shuffle.service.enabled", "false")
      //      .config("spark.dynamicAllocation.enabled", "false")
      // .master("local[2]")
      .config("spark.worker.cleanup.enabled", "true")
      .master( sparkMaster ) // option for cluster  spark://spark-master:7077
      .getOrCreate()

    // we initialize shutdownhook only once.
    Runtime.getRuntime.addShutdownHook(new Thread("closing_stream_thread") {
      override
      def run(): Unit = {
        logger.warn(s"SparkSession closed from ShutdownHook.")
        sparkSession.close()
      }
    })
    sparkSession
  }



  private def startStreaming(): Unit = {
    val session = prepareSparkSession
    // wczytuję z zewnętrznej kafki stream z sms'ami
    val smsStream: Dataset[Sms] = readSmsStream(session)

    // wczytuję z wewnętrznej kafki stream z informacjami czy użytkownik ma aktywną usługę czy nie
    val userStream: Dataset[UserActiveService] = readUserActiveServiceStream(session)


    // wczytuję z wewnętrznej kafki stream z danmi, jaki jest confidence level uri,
    // które już zostały zbadane pod tym kątem.
    val uriStream: Dataset[UriWithConfidenceLevel] = readUriAndConfidenceStatusStream(session)



    // todo napisać grupowanie po uri dla uriStream groupedUriTable

    // todo napisać join smsStream z groupedUriTable joinedSmsUriStream

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




  }





}
