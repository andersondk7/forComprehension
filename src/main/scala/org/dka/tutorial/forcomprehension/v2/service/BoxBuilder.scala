package org.dka.tutorial.forcomprehension.v2.service

import org.dka.tutorial.forcomprehension.v2.model.{Box, BoxHeigthError, Rectangle}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Represents the service that builds 3-D [[Box]]es
  */
trait BoxBuilder {

  /**
    * Build a 3D box with a [[Rectangle]] base and given height
    *
    * @param rectangle base of the box
    * @param height    height of the box
    * @return [[scala.concurrent.Future]] of a [[Either[ConstructionError, Rectangle]]
    */
  def buildBox(rectangle: Rectangle, height: Int): Future[BuildResult[Box]]
}

/**
  * Implementation of a [[BoxBuilder]].
  *
  * Normally this would be in a separate file.
  *
  * @param invalidHeight when build a box of this height, return [[Left[BoxHeightError]]]
  */
class BoxBuilderImpl(invalidHeight: Int) extends BoxBuilder {
  override def buildBox(rectangle: Rectangle, height: Int): Future[BuildResult[Box]] = Future {
    if (height == invalidHeight) Left(BoxHeigthError(height))
    else Right(Box(rectangle.length, rectangle.width, height))
  }
}
