package org.dka.tutorial.forcomprehension.exceptionbased.service

import org.dka.tutorial.forcomprehension.exceptionbased.model._
import org.scalatest.{FunSpec, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
  */
class ConstructionServiceSpec extends FunSpec with Matchers {
  private val invalidWidth = 8
  private val invalidLength = 8
  private implicit val futureTimeout = 1.second
  private val invalidHeight = 12
  private val invalidRadius = 6
  private val squareBuilder: SquareBuilder = new SquareBuilderImpl(invalidWidth)
  private val rectangleBuilder: RectangleBuilder = new RectangleBuilderImpl(invalidLength)
  private val boxBuilder: BoxBuilder = new BoxBuilderImpl(invalidHeight)
  private val tubeBuilder: TubeBuilder = new TubeBuilderImpl(invalidRadius)
  private val tubeWidth = 10
  private val tubeLength = 10
  private val tubeHeight = 16
  private val service = new ConstructionFunctionService(squareBuilder.buildSquare,
    rectangleBuilder.buildRectangle,
    boxBuilder.buildBox,
    tubeBuilder.buildTube
  )

  describe("service happy flow") {
    it("should buildSquare a tube") {
      val expected = Tube(Circle(tubeWidth / 2), tubeHeight)
      val serviceResult: Future[Tube] = service.buildTube(tubeWidth, tubeLength, tubeHeight)
      val result = Await.result(handler(serviceResult), futureTimeout)

      result.isRight shouldBe true
      val actual = result.right.get
      actual shouldBe expected
    }
  }

  describe("service error flow") {
    it("should fail on first step (building the square)") {
      val expected = SquareError
      val serviceResult = service.buildTube(invalidWidth, tubeLength, tubeHeight)

      val result = Await.result(handler(serviceResult), futureTimeout)
      result.isLeft shouldBe true
      result.isLeft shouldBe true
      val actual = result.left.get
    }
    it("should fail on last step (building the tube)") {
      val expected = BoxHeigthError(12)
      val serviceResult = service.buildTube(tubeWidth, tubeLength, invalidHeight)

      val result = Await.result(handler(serviceResult), futureTimeout)
      result.isLeft shouldBe true
      val actual = result.left.get
      actual shouldBe expected
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
