package org.dka.tutorial.forcomprehension.exceptionbased.service

import org.dka.tutorial.forcomprehension.exceptionbased.model._

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
  def buildTube(box: Box): Future[Tube]
}

/**
  * Implementation of a [[TubeBuilder]]
  *
  * This implementation only builds [[Tube]]s that are perfect circles (not ellipses), that is the base must be a square
  * when the base is not a square, it throws a [[TubeNotRoundError]]
  *
  * Normally this would be in a separate file
  *
  * @param invalidRadius when building a [[Tube]] with this radius, throw a [[TubeConstructionError]]
  */
class TubeBuilderImpl(invalidRadius: Int) extends TubeBuilder {
  override def buildTube(box: Box): Future[Tube] = Future {
    val base = Circle(box.width / 2)
    if (box.width != box.length) throw TubeNotRoundError(box.width, box.length)
    else if (base.radius == invalidRadius) throw TubeConstructionError(base.radius)
    else Tube(base, box.height)
  }
}
