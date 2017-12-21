package org.dka.tutorial.forcomprehension.service

import org.dka.tutorial.forcomphrension.model.{Square, SquareError}
import org.dka.tutorial.forcomphrension.service.{SquareBuilder, SquareBuilderImpl}
import org.scalatest.{FunSpec, Matchers}

import scala.concurrent.duration._

import scala.concurrent.{Await, Future}

class SquareBuilderSpec extends FunSpec with Matchers {
  import TestHelper._
  val invalidWidth = 5
  val squareBuilder: SquareBuilder = new SquareBuilderImpl(invalidWidth)

  describe("building squares") {
    it("should successfully build a square") {
      val width = 8
      val square: Future[Square] = squareBuilder.buildSquare(width)
      val result = Await.result(handler(square, Square(width)), 1.second)
      result shouldBe success
    }
    it ("should throw exception with an invalid width") {
      val square: Future[Square] = squareBuilder.buildSquare(invalidWidth)
      val result = Await.result(handler(square, SquareError), 1.second)
      result shouldBe success
    }
  }
}


