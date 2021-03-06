package org.dka.tutorial.forcomprehension.eitherbased.model

import scala.concurrent.TimeoutException
import scala.concurrent.duration.FiniteDuration


/**
  * Represents errors generated during construction of shapes
  *
  * This trait collects common parameters of all the errors that can be thrown during construction of a shape
  *
  */
sealed trait ConstructionError {
  def reason: String

  def code: ConstructionErrorCategory
}

case object SquareError extends ConstructionError {
  override val reason: String = "it was not square"
  override val code: ConstructionErrorCategory = BadInput
}

case class RetangleLengthError(length: Int) extends ConstructionError {
  override val reason: String = s"$length was invalid"
  override val code: ConstructionErrorCategory = BadInput
}

case class BoxHeigthError(height: Int) extends ConstructionError {
  override val reason = s"height: $height is invalid"
  override val code: ConstructionErrorCategory = BuilderError
}

case class TubeNotRoundError(width: Int, length: Int) extends ConstructionError {
  override def reason: String = s"width: $width was not equal to length $length"
  override def code: ConstructionErrorCategory = BadInput
}

case class TubeConstructionError(radius: Int) extends ConstructionError {
  override def reason: String = s"radius: $radius was not valid"
  override def code: ConstructionErrorCategory = BadInput
}

case class TimeoutError(te: TimeoutException) extends ConstructionError {
  override def reason: String = te.getMessage
  override val code: ConstructionErrorCategory = BuilderError

}
