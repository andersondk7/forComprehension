package org.dka.tutorial.forcomprehension.v2.service


import akka.actor.{ActorSystem, Scheduler}
import org.dka.tutorial.forcomprehension.v2.model._
import org.scalatest.{FunSpec, Matchers}

import scala.concurrent.{Await, ExecutionContext, TimeoutException}
import scala.concurrent.duration._

/**
  */
class ConstructionServiceSpec extends FunSpec with Matchers {

  val actorSystem = ActorSystem("ConstructionServiceSpec")
  implicit val ec: ExecutionContext = actorSystem.dispatcher
  val scheduler: Scheduler = actorSystem.scheduler

  private val invalidWidth = 8
  private val invalidLength = 8
  private implicit val futureTimeout: FiniteDuration = 500.millis
  private val invalidHeight = 12
  private val invalidRadius = 6
  private val tubeWidth = 10
  private val tubeLength = 10
  private val tubeHeight = 16

  private val squareBuilder: SquareBuilder = new SquareBuilderImpl(invalidWidth)
  private val rectangleBuilder: RectangleBuilder = new RectangleBuilderImpl(invalidLength)
  private val tubeBuilder: TubeBuilder = new TubeBuilderImpl(invalidRadius)

  describe("service happy flow") {
    val boxBuilder: BoxBuilder = new BoxBuilderImpl(invalidHeight)
    val service = new ConstructionService(squareBuilder.buildSquare,
      rectangleBuilder.buildRectangle,
      boxBuilder.buildBox,
      tubeBuilder.buildTube
    )
    it("should buildSquare a tube") {
      val expected = Tube(Circle(tubeWidth / 2), tubeHeight)
      val result = Await.result(service.buildTube(tubeWidth, tubeLength, tubeHeight).mapTo[BuildResult[Tube]], futureTimeout)

      result.isRight shouldBe true
      val actual = result.right.get
      actual shouldBe expected
    }
  }

  describe("service error flow") {
    val boxBuilder: BoxBuilder = new BoxBuilderImpl(invalidHeight)
    val service = new ConstructionService(squareBuilder.buildSquare,
      rectangleBuilder.buildRectangle,
      boxBuilder.buildBox,
      tubeBuilder.buildTube
    )
    it("should fail on first step (building the square)") {
      val expected = RetangleLengthError(invalidWidth)

      val result = Await.result(service.buildTube(invalidWidth, tubeLength, tubeHeight), futureTimeout)
      result.isLeft shouldBe true
      result.isLeft shouldBe true
      val actual = result.left.get
      actual shouldBe expected
    }
    it("should fail on last step (building the tube)") {
      val expected = BoxHeigthError(12)

      val result = Await.result(service.buildTube(tubeWidth, tubeLength, invalidHeight), futureTimeout)
      result.isLeft shouldBe true
      val actual = result.left.get
      actual shouldBe expected
    }
  }

  describe("service timeout flow") {
    val boxBuilder: BoxBuilder = new BoxBuilderDelayImpl(invalidHeight, futureTimeout * 2)(scheduler)
    val service = new ConstructionService(squareBuilder.buildSquare,
      rectangleBuilder.buildRectangle,
      boxBuilder.buildBox,
      tubeBuilder.buildTube
    )

    it("should handle timeout") {

      // since this is where we are waiting...
      //  here is where we must handle the timeout exception

      val result = try {
        Await.result(service.buildTube(tubeWidth, tubeLength, tubeHeight).mapTo[BuildResult[Tube]], futureTimeout)
      } catch {
        case to: TimeoutException => Left(TimeoutError(to))
      }
      result.isLeft shouldBe true
      val actual = result.left.get
      actual shouldBe a [TimeoutError]
    }
  }
}
