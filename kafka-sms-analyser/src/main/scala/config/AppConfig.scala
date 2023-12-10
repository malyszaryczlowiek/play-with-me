package io.github.malyszaryczlowiek
package config

import com.typesafe.config.{Config, ConfigFactory}
// import org.apache.logging.log4j.{LogManager, Logger}


class AppConfig
object AppConfig {

  // private val logger: Logger = LogManager.getLogger(classOf[AppConfig])

  //logger.trace(s"AppConfig started.")

  private val config: Config = ConfigFactory.load("application.conf").getConfig("kafka-sms-analyser")
  // logger.trace(s"Loading configuration from application.conf.")

  def appId: String       = config.getString(s"application-id")



  val kafkaBroker: KafkaConfig = KafkaConfig(
    config.getString(s"kafka-broker.servers"),
    config.getString(s"file-store"),
    config.getInt("topic-partition-num"),
    config.getInt("topic-replication-factor").toShort
  )


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






  def phishingServiceServer: String = config.getString(s"phishing-service.server")

  def phishingServiceHeader: String = config.getString(s"phishing-service.header")

  def phishingServiceToken: String = config.getString(s"phishing-service.token")









//   val analysisDir: String = config.getString("output-analysis-dir")








}
