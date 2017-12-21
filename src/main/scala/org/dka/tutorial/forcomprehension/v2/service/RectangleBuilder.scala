package org.dka.tutorial.forcomprehension.v2.service

import org.dka.tutorial.forcomprehension.v2.model._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Represents the service that builds [[Rectangle]]s
  */
trait RectangleBuilder {

  /**
    * Build a rectangle of given length and width
    *
    * @param length length of one side of the rectangle
    * @param width  width of rectangle (side perpendicular to length side)
    * @return [[Future]] of a [[Rectangle]]
    */
  def buildRectangle(length: Int, width: Int): Future[BuildResult[Rectangle]]
}

/**
  * Implementation of a [[RectangleBuilder]]
  *
  * Normally this would be in a separate file
  *
  * @param invalidLength when building a rectangle with this length, return [[Left[RectangleLengthError]]]
  */
class RectangleBuilderImpl(invalidLength: Int) extends RectangleBuilder {
  override def buildRectangle(length: Int, width: Int): Future[BuildResult[Rectangle]] = Future {
    if (length == invalidLength) Left(RetangleLengthError(length))
    else Right(Rectangle(length, width))
  }
}
