package io.github.malyszaryczlowiek
package config

case class KafkaConfig(servers: String, fileStore: String, partitionNum: Int, replicationFactor: Short)
