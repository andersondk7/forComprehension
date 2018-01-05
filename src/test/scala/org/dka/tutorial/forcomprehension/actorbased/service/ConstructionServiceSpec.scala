package org.dka.tutorial.forcomprehension.actorbased.service


import akka.actor.{ActorSystem, Scheduler}
import org.dka.tutorial.forcomprehension.actorbased.model._
import org.scalatest.{FunSpec, Matchers}

import scala.concurrent.{Await, ExecutionContext, TimeoutException}
import scala.concurrent.duration._

/**
  */
class ConstructionServiceSpec extends FunSpec with Matchers {

  private implicit val actorSystem = ActorSystem("ConstructionServiceSpec")
  implicit val ec: ExecutionContext = actorSystem.dispatcher
  val scheduler: Scheduler = actorSystem.scheduler

  private val invalidWidth = 8
  private val invalidLength = 8
  private val invalidHeight = 12
  private val invalidRadius = 6
  private val tubeWidth = 10
  private val tubeLength = 10
  private val tubeHeight = 16
  private val awaitDelay = 1.second

  private val rectangleBuilder: RectangleBuilder = new RectangleBuilderImpl(invalidLength)

  describe("1. service building tubes") {
    val serviceWait = awaitDelay / 2
    val buildDuration = awaitDelay / 4
    val boxBuilder: BoxBuilder = new BoxBuilderImpl(invalidHeight, serviceWait, buildDuration, shouldRespond = true)
    val squareBuilder = new SquareBuilderImpl(invalidWidth, serviceWait, buildDuration, shouldRespond = true)
    val tubeBuilder = new TubeBuilderImpl(invalidRadius, serviceWait, buildDuration, shouldRespond = true)
    val service = new ConstructionService(squareBuilder,
      rectangleBuilder,
      boxBuilder,
      tubeBuilder
    )
    it("1.1 should buildSquare a tube") {
      val expected = Tube(Circle(tubeWidth / 2), tubeHeight)
      val result = Await.result(service.buildTube(tubeWidth, tubeLength, tubeHeight).mapTo[BuildResult[Tube]], awaitDelay)

      result.isRight shouldBe true
      val actual = result.right.get
      actual shouldBe expected
    }
    it("1.2 should return an error when the rectangle step fails") {
      val expected = RetangleLengthError(invalidWidth)
      val result = Await.result(service.buildTube(invalidWidth, tubeLength, tubeHeight).mapTo[BuildResult[Tube]], awaitDelay)
      result.isLeft shouldBe true
      val actual = result.left.get
      actual shouldBe expected
    }
    it("1.3 should return an error when the box step fails") {
      val expected = BoxHeigthError(invalidHeight)
      val result = Await.result(service.buildTube(tubeWidth, tubeLength, invalidHeight).mapTo[BuildResult[Tube]], awaitDelay)
      result.isLeft shouldBe true
      val actual = result.left.get
      actual shouldBe expected
    }
    it("1.4 should return an error when the tube step fails") {
      val expected = TubeNotRoundError(tubeLength, invalidRadius)
      val result = Await.result(service.buildTube(invalidRadius, tubeLength, tubeHeight).mapTo[BuildResult[Tube]], awaitDelay)
      result.isLeft shouldBe true
      val actual = result.left.get
      actual shouldBe expected
    }
  }
  /**
    * there are 3 timeout values:
    * 1. how long the client (in this case the test) will wait  -- awaitDelay
    * 2. how long the service will wait for the actor to respond -- serviceWait
    * 3. how long the actor takes to build the box -- buildDuration
    */
  describe("2 timeouts") {
    val serviceWait = awaitDelay / 2
    val buildDuration = awaitDelay / 4
    val squareBuilder = new SquareBuilderImpl(invalidWidth, serviceWait, buildDuration, shouldRespond = true)
    val tubeBuilder = new TubeBuilderImpl(invalidRadius, serviceWait, buildDuration, shouldRespond = true)

    it("2.1 should handle when one service (BoxBuilder) returns a Left(TimeoutError)") {
      // force box service to return Left(TimeoutError(...))
      // this will short circuit the rest of the flow (tube service will not be called)
      //  and what was returned from the box service is what is returned
      val boxServiceWait = awaitDelay / 2
      val boxBuildDuration = awaitDelay * 2
      val boxBuilder: BoxBuilder = new BoxBuilderImpl(invalidHeight, boxServiceWait, boxBuildDuration, shouldRespond = true)
      val service = new ConstructionService(squareBuilder,
        rectangleBuilder,
        boxBuilder,
        tubeBuilder
      )

      // since this is where we are waiting...
      //  here is where we must handle the timeout exception
      //  because the boxBuilder does respond in time (but with a TimeoutError), the subsequent steps in the service will not execute
      //    and the service will return the TimeoutError from the boxBuilder

      val result = try {
        Await.result(service.buildTube(tubeWidth, tubeLength, tubeHeight).mapTo[BuildResult[Tube]], awaitDelay)
      } catch {
        case _: TimeoutException => fail("service timed out")
      }
      result.isLeft shouldBe true
      result.left.get shouldBe a [TimeoutError]
    }
    it("2.2 should handle when a service (BoxBuilder) takes longer than the test is willing to wait") {
      // force box service to run longer than the test is willing to wait
      // box service will NOT timeout because it is waiting longer than the box builder takes
      // but the construction serivce WILL timeout because the box service has not completed within await time
      val boxServiceWait = awaitDelay * 3
      val boxBuildDuration = awaitDelay * 2 // returns after the test is done waiting
      val boxBuilder: BoxBuilder = new BoxBuilderImpl(invalidHeight, boxServiceWait, boxBuildDuration, shouldRespond = true)
      val service = new ConstructionService(squareBuilder,
        rectangleBuilder,
        boxBuilder,
        tubeBuilder
      )

      // since this is where we are waiting...
      //  here is where we must handle the timeout exception
      //  because the boxBuilder does not respond in time, the subsequent steps in the service will not execute
      //    and the service will throw a TimeoutException

      try {
        Await.result(service.buildTube(tubeWidth, tubeLength, tubeHeight).mapTo[BuildResult[Tube]], awaitDelay)
        fail("service did not time out")
      } catch {
        case _: TimeoutException =>
          // because the service stopped before the actor in the boxBuilder completes,
          // the stopping of the boxBuilder actor still needs to happen, so don't stop the actor system just yet...
          Thread.sleep(2000)
          succeed
      }

    }
  }

}

