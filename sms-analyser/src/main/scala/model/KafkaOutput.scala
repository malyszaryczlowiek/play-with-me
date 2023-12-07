package io.github.malyszaryczlowiek
package model


case class KafkaOutput(key: Array[Byte], value: Array[Byte])

object KafkaOutput {

  //  def encoder: Encoder[KafkaOutput] = {
  //    ExpressionEncoder.tuple(Seq( encoderFor( Encoders.BINARY ), encoderFor( Encoders.BINARY ))).asInstanceOf[ExpressionEncoder[KafkaOutput]]
  //  }

}
