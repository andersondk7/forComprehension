package org.dka.tutorial.forcomprehension.v2.service

import org.dka.tutorial.forcomprehension.v2.model._
import org.scalatest.{FunSpec, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._


class BoxBuilderSpec extends FunSpec with Matchers {

  val invalidHeight = 5
  val boxBuilder: BoxBuilder = new BoxBuilderImpl(invalidHeight)

  describe("building boxes") {
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
}
