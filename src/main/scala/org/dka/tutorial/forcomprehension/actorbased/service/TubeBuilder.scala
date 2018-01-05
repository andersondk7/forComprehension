package org.dka.tutorial.forcomprehension.actorbased.service

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.pattern._
import org.dka.tutorial.forcomprehension.actorbased.model._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

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
  * @param serviceWait   how long this service will wait for the internal actors to complete
  * @param buildDuration how long the internal actor takes to complete normally
  * @param shouldRespond indicates if the internal actor should respond to the service upon completion
  */
class TubeBuilderImpl(invalidRadius: Int,
                      serviceWait: FiniteDuration,
                      buildDuration: FiniteDuration,
                      shouldRespond: Boolean = true
                     )(implicit actorSystem: ActorSystem) extends TubeBuilder {

  import TubeBuilderActor._

  override def buildTube(box: Box): Future[BuildResult[Tube]] = {
    val builder = actorSystem.actorOf(TubeBuilderActor.props(invalidRadius, buildDuration))
    // since we are waiting here...
    //  we must handle timeouts, since the timeout is from the ask method, it will
    //    be an akka.pattern.AskTimeoutException rather than a scala.concurrent.TimeoutException
    val result = (builder ? Build(box, shouldRespond)) (serviceWait)
      .mapTo[BuildResult[Tube]]
      .recover {
        case to: AskTimeoutException =>
          actorSystem.log.warning(s"caught $to")
          Left(TimeoutError(to))
      }
    // do not need to stop actor, it self terminates
    result
  }
}

/**
  * Actor responsible for building a [[Tube]]
  *
  * This actor only builds tubes with a round base and will throw a [[TubeNotRoundError]] if the base is not square
  * this actor self terminates upon completion of building the tube
  *
  * @param invalidRadius [[Tube]]s with this radius are considered invalid and an [[TubeConstructionError]] will be thrown
  * @param buildDuration how long it takes the actor to build the [[Tube]]
  */
class TubeBuilderActor(invalidRadius: Int, buildDuration: FiniteDuration) extends Actor with ActorLogging {

  import TubeBuilderActor._

  override def aroundPostStop(): Unit = log.info("tube builder stopped")

  override def receive: Receive = {

    case Build(box, shouldRespond) =>
      val base = Circle(box.width / 2)
      val result: BuildResult[Tube] =
        if (box.width != box.length) Left(TubeNotRoundError(box.width, box.length))
        else if (base.radius == invalidRadius) Left(BoxHeigthError(base.radius))
        else Right(Tube(base, box.height))
      //         model that calculation can take some time...

      context.system.scheduler.scheduleOnce(buildDuration, self, Respond(result, sender(), shouldRespond))

    case Respond(result, originalSender, shouldRespond) =>
      if (shouldRespond) originalSender ! result
      log.info(s"stopping tube builder")
      context.stop(self)
  }

}

object TubeBuilderActor {

  // messages
  case class Build(box: Box, shouldRespond: Boolean = true)

  case class Respond(result: BuildResult[Tube], originalSender: ActorRef, shouldRespond: Boolean)

  // constructor
  def props(invalidRadius: Int, buildDuration: FiniteDuration) = Props(classOf[TubeBuilderActor], invalidRadius, buildDuration)
}