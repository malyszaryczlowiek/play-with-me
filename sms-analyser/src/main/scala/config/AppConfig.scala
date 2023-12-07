package io.github.malyszaryczlowiek
package config

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.logging.log4j.{LogManager, Logger}


class AppConfig
object AppConfig {

  private val logger: Logger = LogManager.getLogger(classOf[AppConfig])

  logger.trace(s"AppConfig started.")

  private val config: Config = ConfigFactory.load("application.conf").getConfig("sms-analyser")
  logger.trace(s"Loading configuration from application.conf.")


  val internalKafkaBroker: KafkaConfig = KafkaConfig(
    config.getString(s"internal-kafka-broker.servers"),
    config.getString(s"file-store"),
    config.getInt("topic-partition-num"),
    config.getInt("topic-replication-factor").toShort
  )

  val userStatusTopicName: String = config.getString("internal-kafka-broker.user-status-topic-name")
  val uriConfidenceLevelTopicName =  config.getString("internal-kafka-broker.uri-confidence-level-topic-name")

  val analysisDir: String = config.getString("output-analysis-dir")
  val appId: String       = config.getString(s"application-id")
  val sparkMaster: String = config.getString("spark.master")



  val externalKafkaServers: String = config.getString(s"external-kafka-broker.servers")
  val smsInputTopicName: String    = config.getString(s"external-kafka-broker.sms-topic")

}
