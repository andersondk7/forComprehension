package org.dka.tutorial.forcomprehension.eitherbased.service

import org.dka.tutorial.forcomprehension.eitherbased.model._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

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
  * Each of the steps are represented by functions.  These functions take various parameters and return a
  * [[Left[ConstructionError]] if construction of the shape fails. For example when building a box,the base must be square.
  *
  * To represent that building can be a blocking function (such as accessing a database, etc.) each function returns
  * a [[Future]] of the shape being built.
  *
  * These functions are implemented in other service layer classes.  This way state that does not change based on data
  * (such as invalid parameters etc.) can be set on the service layer class, rather than being passed in the building
  * function.
  *
  * The [[ConstructionFunctionService]] is only concerned with the flow, the details of each step are in the supporting
  * services and the exported functions.  They can/should be developed and tested independently.  The testing of this
  * service should only be testing the flow and associated error handling
  *
  * In this example the building of the square and rectangle can be done concurrently, so they are started
  * outside the loop
  *
  * @param buildSquare    function to build a square give the width
  * @param buildRectangle function to build a rectangle given the width and length
  * @param buildBox       function to build a 3-D box from a rectangular base and specified height
  * @param buildTube      function to build a tube circumscribed withing a square 3-D box
  */
class ConstructionFunctionService(buildSquare: Int => Future[BuildResult[Square]],
                                  buildRectangle: (Int, Int) => Future[BuildResult[Rectangle]],
                                  buildBox: (Rectangle, Int) => Future[BuildResult[Box]],
                                  buildTube: (Box) => Future[BuildResult[Tube]]
                                 ) {
  def buildTube(width: Int, length: Int, height: Int): Future[BuildResult[Tube]] = {
    val sf = buildSquare(width)
    val rf = buildRectangle(width, length)

    // the for comprehension will NEVER time out, it will take as long as the individual steps take
    // timeout errors MUST be handled in the client

    for {
      _ <- sf // just to show that we can wait for the square, but we don't do anything with any errors!!!
      rectangle <- rf
      box <- rectangle.fold(e => Future(Left(e)), r => buildBox(r, height))
      tube <- box.fold(e => Future(Left(e)), b => buildTube(b))
    } yield tube
  }
}
