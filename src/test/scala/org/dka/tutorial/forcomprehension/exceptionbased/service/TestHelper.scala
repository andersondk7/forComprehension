package org.dka.tutorial.forcomprehension.exceptionbased.service

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * User: doug
  * Date: 12/20/17
  * Time: 5:16 PM
  */
object TestHelper {
  val success = "Success"

  def handler[A](future: Future[A], expected: A): Future[String] =
    future
      .map(actual =>
        if (actual == expected) success
        else s"$actual was not equal to $expected"
      )
      .recover {
        case t: Throwable => t.toString
      }

  def handler[A](future: Future[A], expected: Throwable): Future[String] =
    future
      .map(actual => s"got unexpected $actual"
      )
      .recover {
        case t: Throwable => if (t == expected) success
        else t.toString
      }
}
