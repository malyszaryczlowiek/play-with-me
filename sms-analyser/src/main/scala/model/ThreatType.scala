package io.github.malyszaryczlowiek
package model

object ThreatType {

  val THREAT_TYPE_UNSPECIFIED: String = "THREAT_TYPE_UNSPECIFIED"
  val SOCIAL_ENGINEERING: String      = "SOCIAL_ENGINEERING"
  val MALWARE: String                 = "MALWARE"
  val UNWANTED_SOFTWARE: String       = "UNWANTED_SOFTWARE"


  val ALL_SPECIFIED: List[String] = List(
    SOCIAL_ENGINEERING,
    MALWARE,
    UNWANTED_SOFTWARE
  )

}
