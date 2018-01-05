package org.dka.tutorial.forcomprehension.eitherbased.service

import java.util.concurrent.TimeoutException

import akka.actor.{ActorSystem, Scheduler}
import org.dka.tutorial.forcomprehension.eitherbased.model._
import org.scalatest.{FunSpec, Matchers}

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._


class BoxBuilderSpec extends FunSpec with Matchers {

  val actorSystem = ActorSystem("BoxBuilderSpec")
  implicit val ec: ExecutionContext = actorSystem.dispatcher
  val scheduler: Scheduler = actorSystem.scheduler
  val invalidHeight = 5

  describe("building boxes") {
    val boxBuilder: BoxBuilder = new BoxBuilderImpl(invalidHeight)
    it("should successfully build a box") {
      val width = 4
      val base = Rectangle(width, width)
      val height = 8
      val expected = Box(width, width, height)
      val result = Await.result(boxBuilder.buildBox(base, height).mapTo[BuildResult[Box]], 1.second)
      result.isRight shouldBe true
      val actual = result.right.get
      actual shouldBe expected

    }
    it("should throw exception with an invalid height") {
      val width = 4
      val base = Rectangle(width, width)
      val expected = BoxHeigthError(invalidHeight)
      val result = Await.result(boxBuilder.buildBox(base, invalidHeight).mapTo[BuildResult[Box]], 1.second)
      result.isLeft shouldBe true
      val actual = result.left.get
      actual shouldBe expected
    }
  }
  describe("timeoutBoxBuilder") {
    val delay = 1.second
    val boxBuilder: BoxBuilder = new BoxBuilderDelayImpl(invalidHeight, delay)(scheduler)

    it("should throw exception when timed out") {
      val width = 4
      val base = Rectangle(width, width)

      // since this is where we are waiting...
      //  here is where we must handle the timeout exception

      val result = try {
        Await.result(boxBuilder.buildBox(base, invalidHeight).mapTo[BuildResult[Box]], 500.millis)
      } catch {
        case to: TimeoutException => Left(TimeoutError(to))
      }
      result.isLeft shouldBe true
      val actual = result.left.get
      actual shouldBe a [TimeoutError]
    }
  }
}
