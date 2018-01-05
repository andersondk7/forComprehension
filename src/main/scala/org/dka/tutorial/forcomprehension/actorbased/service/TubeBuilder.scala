package org.dka.tutorial.forcomprehension.actorbased.service

import org.dka.tutorial.forcomprehension.actorbased.model._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Represents the service that builds [[Tube]]s
  */
trait TubeBuilder {

  /**
    * Build a [[Tube]] circumscribed within the specified [[Box]]
    *
    * @param box box containing the [[Tube]]
    * @return [[Future]] of a [[Tube]]
    */
  def buildTube(box: Box): Future[BuildResult[Tube]]
}

/**
  * Implementation of a [[TubeBuilder]]
  *
  * This implementation only builds [[Tube]]s that are perfect circles (not ellipses), that is the base must be a square
  * when the base is not a square, it returns  [[Left[TubeNotRoundError]]]
  *
  * Normally this would be in a separate file
  *
  * @param invalidRadius when building a [[Tube]] with this radius, return [[Left[TubeConstructionError]]]
  */
class TubeBuilderImpl(invalidRadius: Int) extends TubeBuilder {
  override def buildTube(box: Box): Future[BuildResult[Tube]] = Future {
    val base = Circle(box.width / 2)
    if (box.width != box.length) Left(TubeNotRoundError(box.width, box.length))
    else if (base.radius == invalidRadius) Left(TubeConstructionError(base.radius))
    else Right(Tube(base, box.height))
  }
}
