package org.dka.tutorial.forcomprehension.exceptionbased.service

import org.dka.tutorial.forcomprehension.exceptionbased.model._
import org.scalatest.{FunSpec, Matchers}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class BoxBuilderSpec extends FunSpec with Matchers {

  import TestHelper._

  val invalidHeight = 5
  val boxBuilder: BoxBuilder = new BoxBuilderImpl(invalidHeight)

  describe("building boxes") {
    it("should successfully build a box") {
      val width = 4
      val base = Rectangle(width, width)
      val height = 8
      val box: Future[Box] = boxBuilder.buildBox(base, height)
      val result = Await.result(handler(box, Box(width, width, height)), 1.second)
      result shouldBe success
    }
    it("should throw exception with an invalid height") {
      val width = 4
      val base = Rectangle(width, width)
      val box: Future[Box] = boxBuilder.buildBox(base, invalidHeight)
      val result = Await.result(handler(box, BoxHeigthError(invalidHeight)), 1.second)
      result shouldBe success
    }
  }
}
