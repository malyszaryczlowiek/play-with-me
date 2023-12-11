package io.github.malyszaryczlowiek
package config

import com.typesafe.config.{Config, ConfigFactory}
// import org.apache.logging.log4j.{LogManager, Logger}


class AppConfig
object AppConfig {


  private val config: Config = ConfigFactory.load("application.conf").getConfig("kafka-sms-analyser")

  def appId: String       = config.getString(s"application-id")

  val kafkaConfig: KafkaConfig = KafkaConfig(
    config.getString(s"kafka-broker.servers"),
    config.getString(s"file-store"),
    config.getInt("topic-partition-num"),
    config.getInt("topic-replication-factor").toShort
  )


  case class PhishingService(server: String, headers: Map[String, String] = Map.empty)

  val phishingService: PhishingService = PhishingService(phishingServiceServer, Map(phishingServiceHeader -> phishingServiceToken))

  private def phishingServiceServer: String = config.getString(s"phishing-service.server")
  private def phishingServiceHeader: String = config.getString(s"phishing-service.header")
  private def phishingServiceToken: String = config.getString(s"phishing-service.token")

  // topics to create

  /**
   * topic where we keep information about uri confidence status
   */
  def uriConfidenceTopicName: String = config.getString(s"kafka-broker.topic.uri-confidence-level-topic")


  /**
   * topic where we keep all numbers with phishing service turned off.
   */
  def userStatusTopicName: String = config.getString(s"kafka-broker.topic.user-status-topic")


  /**
   * topic we read sms from
   */
  def smsInputTopicName: String = config.getString(s"kafka-broker.topic.sms-input-topic")


  /**
   * topic where we redirect sms with many uri links
   */
  def smsWithManyUriTopicName: String = config.getString(s"kafka-broker.topic.sms-with-many-uri-topic")


  /**
   * topic we send fully processed sms
    */
  def smsOutputTopicName: String = config.getString(s"kafka-broker.topic.sms-output-topic")


  /**
   * topic where we put uri to check its confidence level
   */
  def uriToCheckTopicName: String = config.getString(s"kafka-broker.topic.uri-to-check-topic")

  /**
   * number to switch on/off phishing service
    */
  def serviceNumber: String = config.getString(s"service-number")







  object KafkaSSLConfig {

    private val sslConfig = config.getConfig("kafka-security.protocol.ssl")

    def SSL_TRUSTSTORE_LOCATION_CONFIG: String = sslConfig.getString("truststore-location")
    def SSL_TRUSTSTORE_PASSWORD_CONFIG: String = sslConfig.getString("truststore-password")
    def SSL_KEYSTORE_LOCATION_CONFIG: String   = sslConfig.getString("keystore-location")
    def SSL_KEYSTORE_PASSWORD_CONFIG: String   = sslConfig.getString("keystore-password")
    def SSL_KEY_PASSWORD_CONFIG: String        = sslConfig.getString("key-password")

  }







}
