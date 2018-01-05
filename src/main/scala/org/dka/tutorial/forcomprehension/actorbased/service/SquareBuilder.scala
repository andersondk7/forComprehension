package org.dka.tutorial.forcomprehension.actorbased.service

import org.dka.tutorial.forcomprehension.actorbased.model._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Represents the service that builds [[Square]]s
  */
trait SquareBuilder {
  /**
    * Build a square of given width
    *
    * @param width size of square
    * @return [[Future]] of a [[Square]]
    */
  def buildSquare(width: Int): Future[BuildResult[Square]]

}

/**
  * Implementation of a [[SquareBuilder]]
  *
  * Normally this would be in separate file
  *
  * @param invalidWidth when building a square of this width, return [Left[[[SquareError]]
  */
class SquareBuilderImpl(invalidWidth: Int) extends SquareBuilder {
  override def buildSquare(width: Int): Future[BuildResult[Square]] = Future {
    if (width == invalidWidth) Left(SquareError)
    else Right(Square(width))
  }
}
