package org.dka.tutorial.forcomprehension.eitherbased.model

/**
  * Represents a 3-D tube shape
  */
case class Tube(base: Circle, height: Int) {
  val radius: Int = base.radius

}
