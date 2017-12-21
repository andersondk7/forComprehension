package org.dka.tutorial.forcomprehension.v1.service

import org.dka.tutorial.forcomprehension.v1.model.{Rectangle, RetangleLengthError}
import org.scalatest.{FunSpec, Matchers}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class RectangleBuilderSpec extends FunSpec with Matchers {

  import TestHelper._

  val invalidLength = 5
  val rectangleBuilder: RectangleBuilder = new RectangleBuilderImpl(invalidLength)

  describe("building rectangles") {
    it("should successfully build a rectangle") {
      val width = 8
      val length = 10
      val rectangle: Future[Rectangle] = rectangleBuilder.buildRectangle(length, width)
      val result = Await.result(handler(rectangle, Rectangle(length, width)), 1.second)
      result shouldBe success
    }
    it("should throw exception with an invalid length") {
      val width = 8
      val rectangle: Future[Rectangle] = rectangleBuilder.buildRectangle(invalidLength, width)
      val result = Await.result(handler(rectangle, RetangleLengthError(invalidLength)), 1.second)
      result shouldBe success
    }
  }
}
