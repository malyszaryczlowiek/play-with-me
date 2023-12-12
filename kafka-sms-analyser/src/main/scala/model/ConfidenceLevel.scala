package io.github.malyszaryczlowiek
package model

object ConfidenceLevel {

  type Confidence = String

  val CONFIDENCE_LEVEL_UNSPECIFIED: Confidence = "CONFIDENCE_LEVEL_UNSPECIFIED"
  val SAFE: Confidence           = "SAFE"
  val LOW: Confidence            = "LOW"
  val MEDIUM: Confidence         = "MEDIUM"
  val HIGH: Confidence           = "HIGH"
  val HIGHER: Confidence         = "HIGHER"
  val VERY_HIGH: Confidence      = "VERY_HIGH"
  val EXTREMELY_HIGH: Confidence = "EXTREMELY_HIGH"

  val secureConfidenceLevels: List[Confidence] = List(SAFE,LOW)

  val notSecureConfidenceLevels: List[Confidence] = List(MEDIUM, HIGH, HIGHER, VERY_HIGH, EXTREMELY_HIGH) // , CONFIDENCE_LEVEL_UNSPECIFIED

}
