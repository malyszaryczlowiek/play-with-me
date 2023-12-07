package io.github.malyszaryczlowiek
package model

case class PhishingRequestBody(uri: String, threatTypes: List[String], allowScan: Boolean)