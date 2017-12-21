package org.dka.tutorial.forcomprehension.v2.service

import org.dka.tutorial.forcomprehension.v2.model._
import org.scalatest.{FunSpec, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._


class SquareBuilderSpec extends FunSpec with Matchers {
  val invalidWidth = 5
  val squareBuilder: SquareBuilder = new SquareBuilderImpl(invalidWidth)

  describe("building squares") {
    it("should successfully build a square") {
      val width = 8
      val expected = Square(width)
      val result = Await.result(squareBuilder.buildSquare(width).mapTo[BuildResult[Square]], 1.second)
      result.isRight shouldBe true
      val actual = result.right.get
    }
    it("should throw exception with an invalid width") {
      val expected = SquareError
      val result = Await.result(squareBuilder.buildSquare(invalidWidth).mapTo[BuildResult[Square]], 1.second)
      result.isLeft shouldBe true
      val actual = result.left.get
      actual shouldBe expected
    }
  }
}
