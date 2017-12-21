package org.dka.tutorial.forcomprehension.service

import org.dka.tutorial.forcomphrension.model.{ConstructionError, SquareError, Tube}
import org.dka.tutorial.forcomphrension.service._
import org.scalatest.{FunSpec, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
  */
class ConstructionServiceSpec extends FunSpec with Matchers {
  private val invalidWidth = 8
  private val invalidLength = 8
  private val invalidHeight = 12
  private val invalidRadius = 6
  private implicit val futureTimeout = 1.second

  private val squareBuilder: SquareBuilder = new SquareBuilderImpl(invalidWidth)
  private val rectangleBuilder: RectangleBuilder = new RectangleBuilderImpl(invalidLength)
  private val boxBuilder: BoxBuilder = new BoxBuilderImpl(invalidHeight)
  private val tubeBuilder: TubeBuilder = new TubeBuilderImpl(invalidRadius)

  private val tubeWidth = 10
  private val tubeLength = 10
  private val tubeHeight = 16

  val service = new ConstructionFunctionService(squareBuilder.buildSquare,
    rectangleBuilder.buildRectangle,
    boxBuilder.buildBox,
    tubeBuilder.buildTube
  )

  describe("service happy flow") {
    it("should buildSquare a tube") {
      val serviceResult: Future[Tube] = service.buildTube(tubeWidth, tubeLength, tubeHeight)
      val result = Await.result(handler(serviceResult), futureTimeout)

      result.isRight shouldBe true
      val tube = result.right.get
      tube.height shouldBe tubeHeight
      tube.radius shouldBe tubeWidth / 2
    }
  }

  describe("service error flow") {
    it("should fail on first step (building the square)") {

      val service = new ConstructionFunctionService(squareBuilder.buildSquare,
        rectangleBuilder.buildRectangle,
        boxBuilder.buildBox,
        tubeBuilder.buildTube
      )

      val serviceResult = service.buildTube(invalidWidth, tubeLength, tubeHeight)

      val result = Await.result(handler(serviceResult), futureTimeout)
      result.isLeft shouldBe true
      result.left.get shouldBe SquareError
    }
    it("should fail on last step (building the tube)") {

      val squareBuilder: SquareBuilder = new SquareBuilderImpl(invalidWidth)
      val rectangleBuilder: RectangleBuilder = new RectangleBuilderImpl(invalidLength)
      val boxBuilder: BoxBuilder = new BoxBuilderImpl(invalidHeight)
      val tubeBuilder: TubeBuilder = new TubeBuilderImpl(invalidRadius)

      val service = new ConstructionFunctionService(squareBuilder.buildSquare,
        rectangleBuilder.buildRectangle,
        boxBuilder.buildBox,
        tubeBuilder.buildTube
      )

      val serviceResult = service.buildTube(invalidWidth, tubeLength, tubeHeight)

      val result = Await.result(handler(serviceResult), futureTimeout)
      result.isLeft shouldBe true
      result.left.get shouldBe SquareError
    }
  }

  // represents handling the future (such as the Play controller's async method)
  def handler(future: Future[Tube]): Future[Either[ConstructionError, Tube]] = {
    future.map(tube => Right(tube))
      .recover {
        case ce: ConstructionError => Left(ce)
      }
  }

}
