package io.github.malyszaryczlowiek
package config

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.kafka.common.config.TopicConfig
// import org.apache.logging.log4j.{LogManager, Logger}


class AppConfig
object AppConfig {


  private val config: Config = ConfigFactory.load("application.conf").getConfig("kafka-sms-analyser")

  def appId: String       = config.getString(s"application-id")


  /**
   * number to switch on/off phishing service
   */
  def serviceNumber: String = config.getString(s"service-number")





  case class KafkaConfig(servers: String, fileStore: String, partitionNum: Int, replicationFactor: Short)

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
  private def phishingServiceToken: String  = config.getString(s"phishing-service.token")











  object KafkaSSLConfig {

    private val sslConfig = config.getConfig("kafka-security.protocol.ssl")

    def SECURITY_PROTOCOL_CONFIG: String       = "SSL"
    def SSL_TRUSTSTORE_LOCATION_CONFIG: String = sslConfig.getString("truststore-location")
    def SSL_TRUSTSTORE_PASSWORD_CONFIG: String = sslConfig.getString("truststore-password")
    def SSL_KEYSTORE_LOCATION_CONFIG: String   = sslConfig.getString("keystore-location")
    def SSL_KEYSTORE_PASSWORD_CONFIG: String   = sslConfig.getString("keystore-password")
    def SSL_KEY_PASSWORD_CONFIG: String        = sslConfig.getString("key-password")

    def SSL_CERTIFICATE: String       = sslConfig.getString("certificate")
    def SSL_CERTIFICATE_ALIAS: String = sslConfig.getString("certificate-alias")



  }










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





  object Topics {

    case class TopicSetup(name: String, servers: String, partitionNumber: Int, replicationFactor: Short, otherConfig: Map[String, String])

    private val topicConfig = Map(
      TopicConfig.CLEANUP_POLICY_CONFIG -> TopicConfig.CLEANUP_POLICY_DELETE,
      TopicConfig.RETENTION_MS_CONFIG -> "-1" // keep all logs forever
    )


    private def userStatusPartNum = config.getInt(s"kafka-broker.topic.user-status-topic.topic-partition-num")
    private def userStatusRepFac  = config.getInt(s"kafka-broker.topic.user-status-topic.topic-replication-factor").toShort

    val userStatusTopic: TopicSetup =
      TopicSetup(userStatusTopicName, kafkaConfig.servers, userStatusPartNum, userStatusRepFac, topicConfig)



    private def uriConfidencePartNum = config.getInt(s"kafka-broker.topic.uri-confidence-level-topic.topic-partition-num")
    private def uriConfidenceRepFac  = config.getInt(s"kafka-broker.topic.uri-confidence-level-topic.topic-replication-factor").toShort

    val uriConfidenceLevelTopic: TopicSetup =
      TopicSetup(uriConfidenceTopicName, kafkaConfig.servers, uriConfidencePartNum, uriConfidenceRepFac, topicConfig)



    private def uriToCheckPartNum = config.getInt(s"kafka-broker.topic.uri-to-check-topic.topic-partition-num")
    private def uriToCheckRepFac  = config.getInt(s"kafka-broker.topic.uri-to-check-topic.topic-replication-factor").toShort

    val uriToCheckTopic: TopicSetup =
      TopicSetup(uriToCheckTopicName, kafkaConfig.servers, uriToCheckPartNum, uriToCheckRepFac, topicConfig)



    private def smsWithManyUriPartNum = config.getInt(s"kafka-broker.topic.sms-with-many-uri-topic.topic-partition-num")
    private def smsWithManyUriRepFac  = config.getInt(s"kafka-broker.topic.sms-with-many-uri-topic.topic-replication-factor").toShort

    val smsWithManyUriTopic: TopicSetup =
      TopicSetup(smsWithManyUriTopicName, kafkaConfig.servers, smsWithManyUriPartNum, smsWithManyUriRepFac, topicConfig)



    private def smsOutputPartNum = config.getInt(s"kafka-broker.topic.sms-output-topic.topic-partition-num")
    private def smsOutputRepFac  = config.getInt(s"kafka-broker.topic.sms-output-topic.topic-replication-factor").toShort

    val smsOutputTopic: TopicSetup =
      TopicSetup(smsOutputTopicName, kafkaConfig.servers, smsOutputPartNum, smsOutputRepFac, topicConfig)

  }








}
