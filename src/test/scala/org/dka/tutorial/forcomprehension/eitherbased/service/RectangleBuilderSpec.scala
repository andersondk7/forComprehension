package org.dka.tutorial.forcomprehension.eitherbased.service

import org.dka.tutorial.forcomprehension.eitherbased.model._
import org.scalatest.{FunSpec, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._

class RectangleBuilderSpec extends FunSpec with Matchers {

  val invalidLength = 5
  val rectangleBuilder: RectangleBuilder = new RectangleBuilderImpl(invalidLength)

  describe("building rectangles") {
    it("should successfully build a rectangle") {
      val width = 8
      val length = 10
      val expected = Rectangle(length, width)
      val result = Await.result(rectangleBuilder.buildRectangle(length, width).mapTo[BuildResult[Rectangle]], 1.second)
      result.isRight shouldBe true
      val actual = result.right.get
      actual shouldBe expected
    }

    it("should throw exception with an invalid length") {
      val width = 8
      val expected = RetangleLengthError(invalidLength)
      val result = Await.result(rectangleBuilder.buildRectangle(invalidLength, width).mapTo[BuildResult[Rectangle]], 1.second)
      result.isLeft shouldBe true
      val actual = result.left.get
      actual shouldBe expected
    }
  }
}
