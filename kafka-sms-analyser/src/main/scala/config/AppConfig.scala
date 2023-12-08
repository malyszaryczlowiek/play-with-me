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

  val appId: String       = config.getString(s"application-id")

  val kafkaBroker: KafkaConfig = KafkaConfig(
    config.getString(s"kafka-broker.servers"),
    config.getString(s"file-store"),
    config.getInt("topic-partition-num"),
    config.getInt("topic-replication-factor").toShort
  )


  // topics to create
  val uriConfidenceTopicName: String = config.getString(s"kafka-broker.topic.uri-confidence-level-topic")
  val userStatusTopicName: String    = config.getString(s"kafka-broker.topic.user-status-topic")

  // topic we read from
  val smsInputTopicName: String    = config.getString(s"kafka-broker.topic.sms-input-topic")

  // topic we send processed sms
  val smsOutputTopicName: String    = config.getString(s"kafka-broker.topic.sms-output-topic")


  // service number to shich on/off phishing service
  val serviceNumber: String = config.getString(s"service-number")

//   val analysisDir: String = config.getString("output-analysis-dir")








}
