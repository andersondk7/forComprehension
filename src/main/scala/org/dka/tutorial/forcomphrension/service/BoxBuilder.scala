package org.dka.tutorial.forcomphrension.service

import org.dka.tutorial.forcomphrension.model.{Box, BoxHeigthError, Rectangle}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Represents the service that builds 3-D [[Box]]es
  */
trait BoxBuilder {

  /**
    * Build a 3D box with a [[Rectangle]] base and given height
    * @param rectangle base of the box
    * @param height height of the box
    * @return [[Future]] of a [[Rectangle]]
    */
  def buildBox(rectangle: Rectangle, height: Int): Future[Box]
}

/**
  * Implementation of a [[BoxBuilder]].
  *
  * Normally this would be in a separate file.
  *
  * @param invalidHeight when build a box of this height, throw a [[BoxHeigthError]]
  */
class BoxBuilderImpl(invalidHeight: Int) extends BoxBuilder {
  override def buildBox(rectangle: Rectangle, height: Int): Future[Box] = Future {
    if (height == invalidHeight) throw BoxHeigthError(height)
    else Box(rectangle.length, rectangle.width, height)
  }
}
