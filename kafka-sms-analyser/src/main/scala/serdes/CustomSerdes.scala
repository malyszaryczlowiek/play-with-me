package io.github.malyszaryczlowiek
package serdes

object CustomSerdes {

  val smsSerde                  = new SmsSerde
  val uriListSerde              = new UriListSerde
  val confidenceAndUriListSerde = new ConfidenceAndUriListSerde

}
