package org.dka.tutorial.forcomprehension.actorbased.service

import java.util.concurrent.TimeoutException

import akka.actor.ActorSystem
import org.dka.tutorial.forcomprehension.actorbased.model._
import org.scalatest.{FunSpec, Matchers}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}


/**
  * tests the box builder
  *
  * for each call to the [[BoxBuilderImpl.buildBox()]] a [[BoxBuilderActor]] is created by the service and the
  * service *must* stop the created actor
  *
  * this is shown in this test by having the number of _builder stopped_ log messages as there are
  * calls to the [[BoxBuilder.buildBox()]] methods.  Unfortunately this is only verified by inspection of the test
  * output.  In all cases the _builder stopped_ message should be *before* the _x.y completed_ message
  *
  * Currently the test is arranged with one [[BoxBuilder.buildBox()]] call per test
  *
  */
class BoxBuilderSpec extends FunSpec with Matchers {

  // -----------------------------------------------------
  // notes:
  //  for each call to the BoxBuilder
  // -----------------------------------------------------
  private implicit val actorSystem: ActorSystem = ActorSystem("BoxBuilderSpec")
  private implicit val ec: ExecutionContext = actorSystem.dispatcher
  private val invalidHeight = 5
  private val validHeight = 8
  private val width = 4
  private val base = Rectangle(width, width)
  private val awaitDelay = 1.second

  describe("1. building boxes") {
    val serviceWait = awaitDelay / 2
    val buildDuration = awaitDelay / 4
    val boxBuilder: BoxBuilder = new BoxBuilderImpl(invalidHeight, serviceWait, buildDuration, shouldRespond = true)
    it("1.1 should successfully build a box") {
      val expected = Box(width, width, validHeight)
      val result = Await.result(boxBuilder.buildBox(base, validHeight).mapTo[BuildResult[Box]], awaitDelay)
      result.isRight shouldBe true
      val actual = result.right.get
      actual shouldBe expected
      actorSystem.log.info("1.1 completed")

    }
    it("1.2 should throw exception with an invalid height") {
      val expected = BoxHeigthError(invalidHeight)
      val result = Await.result(boxBuilder.buildBox(base, invalidHeight).mapTo[BuildResult[Box]], awaitDelay)
      result.isLeft shouldBe true
      val actual = result.left.get
      actual shouldBe expected
      actorSystem.log.info("1.2 completed")
    }
  }
  /**
    * there are 3 timeout values:
    * 1. how long the client (in this case the test) will wait  -- awaitDelay
    * 2. how long the service will wait for the actor to respond -- serviceWait
    * 3. how long the actor takes to build the box -- buildDuration
    */
  describe("2. timeoutBoxBuilder") {
    it("2.1 should handle actor takes longer than the service is willing to wait") {
      val serviceWait = awaitDelay / 2
      val buildDuration = serviceWait * 2
      val boxBuilder: BoxBuilder = new BoxBuilderImpl(invalidHeight, serviceWait, buildDuration, shouldRespond = true)

      // since this is where we are waiting...
      //  here is where we must handle the timeout exception
      val result = try {
        Await.result(boxBuilder.buildBox(base, validHeight).mapTo[BuildResult[Box]], awaitDelay)
      } catch {
        case _: TimeoutException => fail(s"service should have completed within $awaitDelay")
      }
      result.isLeft shouldBe true
      val actual = result.left.get
      actual shouldBe a[TimeoutError] // because service caught actor.ask timeout
      actorSystem.log.info("2.1 completed")
    }
    it("2.2 should handle when actor never responds") {
      val serviceWait = awaitDelay / 2
      val buildDuration = serviceWait / 4 // actor would response before service gives up
                                          // but the actor has a fault and will never respond
      val boxBuilder: BoxBuilder = new BoxBuilderImpl(invalidHeight, serviceWait, buildDuration, shouldRespond = false)

      // since this is where we are waiting...
      //  here is where we must handle the timeout exception
      val result = try {
        Await.result(boxBuilder.buildBox(base, validHeight).mapTo[BuildResult[Box]], awaitDelay)
      } catch {
        case _: TimeoutException => fail(s"service should have completed within $awaitDelay")
      }
      result.isLeft shouldBe true
      val actual = result.left.get
      actual shouldBe a[TimeoutError]
      actorSystem.log.info("2.2 completed")
    }
    it("2.3 should throw exception when actor service does not respond in time") {
      val serviceWait = awaitDelay * 2 // service will wait longer than than the test will wait
      val buildDuration = serviceWait * 4 // actor will respond longer than the test will wait
      // but the actor has a fault and will never respond
      val boxBuilder: BoxBuilder = new BoxBuilderImpl(invalidHeight, serviceWait, buildDuration, shouldRespond = true)

      // since this is where we are waiting...
      //  here is where we must handle the timeout exception that could be thrown by the Await call
      val result = try {
        Await.result(boxBuilder.buildBox(base, validHeight).mapTo[BuildResult[Box]], awaitDelay)
      } catch {
        case to: TimeoutException => Left(TimeoutError(to))
      }
      result.isLeft shouldBe true
      val actual = result.left.get
      actual shouldBe a[TimeoutError]
      actorSystem.log.info("2.3 completed")
    }
  }
}
