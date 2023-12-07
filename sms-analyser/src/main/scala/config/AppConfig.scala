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




  val kafkaConfig: KafkaConfig = KafkaConfig(
    config.getString(s"kafka-servers"),
    config.getString(s"file-store"),
    config.getInt("topic-partition-num"),
    config.getInt("topic-replication-factor").toShort
  )

  val analysisDir: String = config.getString("output-analysis-dir")


  val appId: String = config.getString(s"application-id")


}
