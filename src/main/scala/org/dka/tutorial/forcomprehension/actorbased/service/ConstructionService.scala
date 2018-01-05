package org.dka.tutorial.forcomprehension.actorbased.service

import org.dka.tutorial.forcomprehension.actorbased.model._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, TimeoutException}

/**
  * Represents the business logic of building shapes
  *
  * The building of shapes is a simple linear flow where each step depends on previously performed steps.
  *
  * This process builds a tube in 4 steps:
  *
  * 1. build a square base
  * 1. build a rectangle
  * 1. convert the rectangle base into a 3-D box
  * 1. create a tube circumscribed within the box
  *
  * Each of the steps are represented by other services.  These services have a method to build a shape and return a
  * [[Left[ConstructionError]] if construction of the shape fails. For example when building a box,the base must be square.
  *
  * To represent that building can be a blocking function (such as accessing a database, etc.) each service method returns
  * a [[Future]] of the shape being built.
  *
  * The [[ConstructionService]] is only concerned with the flow, the details of each step are in the supporting
  * services.  They can/should be developed and tested independently.  The testing of this service should only be
  * testing the flow and associated error handling
  *
  * In this example the building of the square and rectangle can be done concurrently, so they are started
  * outside the loop
  *
  * @param squareBuilder    service to build a square give the width
  * @param rectangleBuilder service to build a rectangle given the width and length
  * @param boxBuilder       service to build a 3-D box from a rectangular base and specified height
  * @param tubeBuilder      service to build a tube circumscribed withing a square 3-D box
  */
class ConstructionService(squareBuilder: SquareBuilder,
                          rectangleBuilder: RectangleBuilder,
                          boxBuilder: BoxBuilder,
                          tubeBuilder: TubeBuilder
                         ) {
  def buildTube(width: Int, length: Int, height: Int): Future[BuildResult[Tube]] = {
    val sf = squareBuilder.buildSquare(width)
    val rf = rectangleBuilder.buildRectangle(width, length)

    // the for comprehension will NEVER time out, it will take as long as the individual steps take
    // timeout errors MUST be handled in the client

    for {
      _ <- sf // just to show that we can wait for the square, but we don't do anything with the errors
      rectangle <- rf
      box <- rectangle.fold(e => Future(Left(e)), r => boxBuilder.buildBox(r, height))
      tube <- box.fold(e => Future(Left(e)), b => tubeBuilder.buildTube(b))
    } yield tube
  }
}
