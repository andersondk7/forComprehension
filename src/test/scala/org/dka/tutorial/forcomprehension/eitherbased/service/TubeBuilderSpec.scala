package org.dka.tutorial.forcomprehension.eitherbased.service

import org.dka.tutorial.forcomprehension.eitherbased.model._
import org.scalatest.{FunSpec, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._

class TubeBuilderSpec extends FunSpec with Matchers {
  val invalidRadius = 5
  val tubeBuilder: TubeBuilder = new TubeBuilderImpl(invalidRadius)

  describe("building tubes") {
    it("should successfully build a tube") {
      val width = 8
      val length = 8
      val height = 10
      val box = Box(length, width, height)
      val expected = Tube(Circle(width / 2), height)
      val result = Await.result(tubeBuilder.buildTube(box).mapTo[BuildResult[Tube]], 1.second)
      result.isRight shouldBe true
      val actual = result.right.get
      actual shouldBe expected
    }
    it("should throw exception with an invalid radius") {
      val width = invalidRadius * 2
      val height = 10
      val box = Box(width, width, height)
      val expected = TubeConstructionError(invalidRadius)
      val result = Await.result(tubeBuilder.buildTube(box).mapTo[BuildResult[Tube]], 1.second)
      result.isLeft shouldBe true
      val actual = result.left.get
      actual shouldBe expected
    }
    it("should throw exception with an non-square base") {
      val width = 8
      val length = 6
      val height = 10
      val box = Box(length, width, height)
      val expected = TubeNotRoundError(width, length)
      val result = Await.result(tubeBuilder.buildTube(box).mapTo[BuildResult[Tube]], 1.second)
      result.isLeft shouldBe true
      val actual = result.left.get
      actual shouldBe expected
    }
  }
}
