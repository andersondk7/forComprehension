package org.dka.tutorial.forcomprehension.exceptionbased.service

import org.dka.tutorial.forcomprehension.exceptionbased.model._
import org.scalatest.{FunSpec, Matchers}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class TubeBuilderSpec extends FunSpec with Matchers {

  import TestHelper._

  val invalidRadius = 5
  val tubeBuilder: TubeBuilder = new TubeBuilderImpl(invalidRadius)

  describe("building tubes") {
    it("should successfully build a tube") {
      val width = 8
      val length = 8
      val height = 10
      val box = Box(length, width, height)
      val tube: Future[Tube] = tubeBuilder.buildTube(box)
      val result = Await.result(handler(tube, Tube(Circle(width / 2), height)), 1.second)
      result shouldBe success
    }
    it("should throw exception with an invalid radius") {
      val width = invalidRadius * 2
      val height = 10
      val box = Box(width, width, height)
      val tube: Future[Tube] = tubeBuilder.buildTube(box)
      val result = Await.result(handler(tube, TubeConstructionError(invalidRadius)), 1.second)
      result shouldBe success
    }
    it("should throw exception with an non-square base") {
      val width = 8
      val length = 6
      val height = 10
      val box = Box(length, width, height)
      val tube: Future[Tube] = tubeBuilder.buildTube(box)
      val result = Await.result(handler(tube, TubeNotRoundError(width, length)), 1.second)
      result shouldBe success
    }
  }
}
