package org.dka.tutorial.forcomphrension.service

import org.dka.tutorial.forcomphrension.model.{Square, SquareError}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Represents the service that builds [[Square]]s
  */
trait SquareBuilder {
  /**
    * Build a square of given width
    * @param width size of square
    * @return [[Future]] of a [[Square]]
    */
  def buildSquare(width: Int): Future[Square]

}

/**
  * Implementation of a [[SquareBuilder]]
  *
  * Normally this would be in separate file
  *
  * @param invalidWidth when building a square of this width, throw a [[SquareError]]
  */
class SquareBuilderImpl(invalidWidth: Int) extends SquareBuilder {
  override def buildSquare(width: Int): Future[Square] = Future {
    if (width == invalidWidth) throw SquareError
    else Square(width)
  }
}
