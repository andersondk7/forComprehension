package org.dka.tutorial.forcomprehension.actorbased.service

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.pattern._
import org.dka.tutorial.forcomprehension.actorbased.model._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Future, TimeoutException}

/**
  * Represents the service that builds [[Square]]s
  */
trait SquareBuilder {
  /**
    * Build a square of given width
    *
    * @param width size of square
    * @return [[Future]] of a [[Square]]
    */
  def buildSquare(width: Int): Future[BuildResult[Square]]

}

/**
  * Implementation of a [[SquareBuilder]]
  *
  * Normally this would be in separate file
  *
  * @param invalidWidth  when building a square of this width, return [Left[[[SquareError]]
  * @param serviceWait   how long this service will wait for the internal actors to complete
  * @param buildDuration how long the internal actor takes to complete normally
  * @param shouldRespond indicates if the internal actor should respond to the service upon completion
  */
class SquareBuilderImpl(invalidWidth: Int,
                        serviceWait: FiniteDuration,
                        buildDuration: FiniteDuration,
                        shouldRespond: Boolean = true)(implicit actorSystem: ActorSystem) extends SquareBuilder {

  import SquareBuilderActor._

  /**
    *
    * implementation of the [[SquareBuilder.buildSquare()]] method that delegates the building to an actor.
    *
    * This implementation creates an actor to do the building of the square and therefore asks the actor to do the building.
    * This is intended to illustrate when the building of a square has many steps and is modeled as a finite state machine
    * (that is a stateful actor)
    *
    * Because the service uses [[Future]] to model concurrency and the building of the [[Square]] is in an actor which uses
    * _messages_ to model concurrency, the service [[ask]]s the actor to build the square.  The [[ask]] method takes an
    * implicit [[FiniteDuration]] after which the [[ask]] throws an [[AskTimeoutException]] which *must* be caught.
    * In this implementation, this value is the [[serviceWait]] parameter
    *
    * The building actor itself takes a finite amount of time to complete the task of building the [[Square]].
    * In this implementation, this value is the [[buildDuration]] parameter.
    *
    * Finally because the service creates a new instance of an [[SquareBuilderActor]] for each request, it is important that
    * the actor terminates.   This can either be done by the service after it is done with the created [[SquareBuilderActor]]
    * or the [[SquareBuilderActor]] can self terminates upon completion of the task.  This implementation has the service
    * stopping the [[SquareBuilderActor]]
    *
    * @param width size of square
    * @return [[Future]] of a [[Square]]
    */
  override def buildSquare(width: Int): Future[BuildResult[Square]] = {
    val builder = actorSystem.actorOf(SquareBuilderActor.props(invalidWidth, buildDuration))
    // since we are waiting here...
    //  we must handle timeouts, since the timeout is from the ask method, it will
    //    be an akka.pattern.AskTimeoutException rather than a scala.concurrent.TimeoutException
    val result = (builder ? Build(width, shouldRespond)) (serviceWait)
      .mapTo[BuildResult[Square]]
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
  * Actor responsible for building a [[Square]]
  *
  * this actor self terminates
  *
  * @param invalidWidth  [[Square]]s of this width are considered invalid and an [[SquareError]] will be thrown
  * @param buildDuration how long it takes the actor to build the [[Square]]
  */
class SquareBuilderActor(invalidWidth: Int, buildDuration: FiniteDuration) extends Actor with ActorLogging {

  import SquareBuilderActor._

  override def aroundPostStop(): Unit = log.info("square builder stopped")

  override def receive: Receive = {

    case Build(width, shouldRespond) =>
      val result: BuildResult[Square] =
        if (width == invalidWidth) Left(SquareError)
        else Right(Square(width))
      // model that calculation can take some time...
      context.system.scheduler.scheduleOnce(buildDuration, self, Respond(result, sender(), shouldRespond))

    case Respond(result, originalSender, shouldRespond) =>
      if (shouldRespond) originalSender ! result
      log.info(s"stopping square builder")
      context.stop(self) // required so that actor created by service gets cleaned up
  }

}

object SquareBuilderActor {

  // messages
  case class Build(width: Int, shouldRespond: Boolean = true)

  case class Respond(result: BuildResult[Square], originalSender: ActorRef, shouldRespond: Boolean)

  // constructor
  def props(invalidWidth: Int, calculationDelay: FiniteDuration) = Props(classOf[SquareBuilderActor], invalidWidth, calculationDelay)
}

