package io.github.malyszaryczlowiek
package util

import config.AppConfig.KafkaSSLConfig
import config.AppConfig.Topics.TopicSetup
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.admin.{Admin, CreateTopicsResult, NewTopic}
import org.apache.kafka.common.KafkaFuture
import org.apache.kafka.streams.StreamsConfig

import java.util.Properties
import scala.jdk.javaapi.CollectionConverters
import scala.util.{Failure, Success, Using}
import org.apache.kafka.common.KafkaException
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.errors._

object TopicCreator {

  sealed trait TopicCreationResult
  case class Done(msg: String) extends TopicCreationResult
  case class Error(msg: String) extends TopicCreationResult


  /**
   * With this method we create required topic.
   *
   * NOTE! If topic with name defined in TopicSetup argument already exists
   * then method returns Done with message 'Topic already exists'.
   *
   * @param topicName
   */
  def createTopic(setup: TopicSetup): TopicCreationResult = {

    val adminProperties: Properties = new Properties()
    adminProperties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, setup.servers)

    // security properties
    // uncomment when SSL required
//    adminProperties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, KafkaSSLConfig.SECURITY_PROTOCOL_CONFIG)
//    adminProperties.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, KafkaSSLConfig.SSL_TRUSTSTORE_LOCATION_CONFIG)
//    adminProperties.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, KafkaSSLConfig.SSL_TRUSTSTORE_PASSWORD_CONFIG)
//    adminProperties.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, KafkaSSLConfig.SSL_KEYSTORE_LOCATION_CONFIG)
//    adminProperties.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, KafkaSSLConfig.SSL_KEYSTORE_PASSWORD_CONFIG)
//    adminProperties.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, KafkaSSLConfig.SSL_KEY_PASSWORD_CONFIG)



    Using(Admin.create(adminProperties)) {
      admin =>
        val chatConfig: java.util.Map[String, String] = CollectionConverters.asJava(setup.otherConfig)
        // we create  topic
        val result: CreateTopicsResult = admin.createTopics(
          java.util.Collections.singletonList(
            new NewTopic(setup.name, setup.partitionNumber, setup.replicationFactor).configs(chatConfig)
          )
        )

        // extract task of topic creation
        val talkFuture: KafkaFuture[Void] = result.values().get(setup.name)

        // we wait patiently to create topic or get error.
        talkFuture.get //(5L, TimeUnit.SECONDS)

        // simply return topic name as proof of creation
        setup.name
    } match {
      case Failure(ex) =>
        handleWithErrorMessage(ex) match {
          case Left(err: String) =>
            Error(s"Cannot create topic '${setup.name}'. $err")
          case Right(msg) => //If return right this means that topic already exists
            Done(msg)
        }
      case Success(topicName) => Done(topicName)
    }
  }





  private def handleWithErrorMessage(ex: Throwable): Either[String, String] = {
    val message = ex.getMessage // in some exceptions message may be null
    if (message != null) {
      val isInternal: Boolean =
        message.contains("InvalidOffsetException") ||
          message.contains("WakeupException") ||
          message.contains("InterruptException") ||
          message.contains("AuthenticationException") ||
          message.contains("AuthorizationException") ||
          message.contains("IllegalArgumentException") ||
          message.contains("IllegalStateException") ||
          message.contains("ArithmeticException") ||
          message.contains("InvalidTopicException")

      val isServerError =
        message.contains("UnsupportedVersionException") ||
          message.contains("TimeoutException")

      val exists = message.contains("TopicExistsException")

      val topicExistsError = "Topic already exists"
      val internalError    = "Internal Error"
      val serverError      = "Server Error"
      val undefinedErr     = "Undefined Error"


      if (exists) Right( topicExistsError ) // note that if topic already exists we return Right not Left
      else if (isInternal) Left( internalError )
      else if (isServerError) Left( serverError )
      else Left( undefinedErr )
    } else
      Left("Exception has null message.")
  }

}
