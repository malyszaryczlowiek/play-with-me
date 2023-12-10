package io.github.malyszaryczlowiek
package model


@deprecated
case class KafkaOutput(key: Array[Byte], value: Array[Byte])
@deprecated
object KafkaOutput {

  //  def encoder: Encoder[KafkaOutput] = {
  //    ExpressionEncoder.tuple(Seq( encoderFor( Encoders.BINARY ), encoderFor( Encoders.BINARY ))).asInstanceOf[ExpressionEncoder[KafkaOutput]]
  //  }

}
