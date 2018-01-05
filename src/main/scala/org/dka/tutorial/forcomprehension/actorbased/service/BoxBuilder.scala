package org.dka.tutorial.forcomprehension.actorbased.service

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props, Scheduler}
import akka.pattern._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, TimeoutException}
import scala.concurrent.duration.FiniteDuration
import org.dka.tutorial.forcomprehension.actorbased.model._

/**
  * Represents the service that builds 3-D [[Box]]es
  */
trait BoxBuilder {

  /**
    * Build a 3D box with a [[Rectangle]] base and given height
    *
    * @param rectangle base of the box
    * @param height    height of the box
    * @return [[scala.concurrent.Future]] of a [[Either[ConstructionError, Rectangle]]
    */
  def buildBox(rectangle: Rectangle, height: Int): Future[BuildResult[Box]]
}

/**
  * Implementation of a [[BoxBuilder]].
  *
  * Normally this would be in a separate file.
  *
  * @param invalidHeight when build a box of this height, return [[Left[BoxHeightError]]]
  * @param serviceWait how long this service will wait for the internal actors to complete
  * @param buildDuration how long the internal actor takes to complete normally
  * @param shouldRespond indicates if the internal actor should respond to the service upon completion
  */
class BoxBuilderImpl(invalidHeight: Int,
                     serviceWait: FiniteDuration,
                     buildDuration: FiniteDuration,
                     shouldRespond: Boolean = true)(implicit actorSystem: ActorSystem) extends BoxBuilder {
  import BoxBuilderActor._

  /**
    * implementation of the [[BoxBuilder.buildBox()]] method that delegates the building to an actor.
    *
    * This implementation creates an actor to do the building of the box and therefore asks the actor to do the building.
    * This is intended to illustrate when the building of a box has many steps and is modeled as a finite state machine
    * (that is a stateful actor)
    *
    * Because the service uses [[Future]] to model concurrency and the building of the Box is in an actor which uses
    * _messages_ to model concurrency, the service [[ask]]s the actor to build the box.  The [[ask]] method takes an
    * implicit [[FiniteDuration]] after which the [[ask]] throws an [[AskTimeoutException]] which *must* be caught.
    * In this implementation, this value is the [[serviceWait]] parameter
    *
    * The building actor itself takes a finite amount of time to complete the task of building the [[Box]].
    * In this implementation, this value is the [[buildDuration]] parameter.
    *
    * Finally because the service creates a new instance of an [[BoxBuilderActor]] for each request, it is important that
    * the actor terminates.   This can either be done by the service after it is done with the created [[BoxBuilderActor]]
    * or the [[BoxBuilderActor]] can self terminates upon completion of the task.  This implementation has the service
    * stopping the [[BoxBuilderActor]]
    *
    * @param rectangle base of the box
    * @param height    height of the box
    * @return [[scala.concurrent.Future]] of a [[Either[ConstructionError, Rectangle]]
    */
  override def buildBox(rectangle: Rectangle, height: Int): Future[BuildResult[Box]] = {
    val builder = actorSystem.actorOf(BoxBuilderActor.props(invalidHeight, buildDuration))
    val result = (builder ? Build(rectangle, height, shouldRespond))(serviceWait)
      .mapTo[BuildResult[Box]]
      .recover {
        case to: AskTimeoutException =>
          actorSystem.log.warning(s"caught $to")
          Left(TimeoutError(to))
        case to: TimeoutException => // should not happen!!!
          actorSystem.log.warning(s"generic TimeoutException, should have been an AskTimeoutException")
          Left(TimeoutError(to))
      }
      .map(r => { // stopping of actor MUST happen after work has been completed
        actorSystem.stop(builder) // since the BoxBuilderActor does NOT self terminate
        r
      })
    result
  }
}

/**
  * Actor responsible for building a [[Box]]
  * @param invalidHeight [[Box]]es of this height are considered invalid and an [[BoxHeigthError]] will be thrown
  * @param buildDuration how long it takes the actor to build the [[Box]]
  */
class BoxBuilderActor(invalidHeight: Int, buildDuration: FiniteDuration) extends Actor with ActorLogging {
  import BoxBuilderActor._

  override def aroundPostStop(): Unit = log.info("builder stopped")

  override def receive: Receive = {

    case Build(rectangle, height, shouldRespond) =>
      val result: BuildResult[Box] =
        if (height == invalidHeight) Left(BoxHeigthError(height))
        else Right(Box(rectangle.length, rectangle.width, height))
      // model that calculation can take some time...
      context.system.scheduler.scheduleOnce(buildDuration, self, Respond(result, sender(), shouldRespond))

    case Respond(result, originalSender, shouldRespond) =>
      if (shouldRespond) originalSender ! result
//      context.stop(self)  // required so that actor created by service gets cleaned up
  }

}

object BoxBuilderActor {
  // messages
  case class Build(rectangle: Rectangle, height: Int, shouldRespond: Boolean = true)
  case class Respond(result: BuildResult[Box], originalSender: ActorRef, shouldRespond: Boolean)
  // constructor
  def props(invalidHeight: Int, calculationDelay: FiniteDuration) = Props(classOf[BoxBuilderActor], invalidHeight, calculationDelay)
}

