package org.dka.tutorial.forcomprehension.eitherbased.model

/**
  * Represents categories of error codes
  *
  * There can be many types of [[ConstructionError]]s but they all fall into one these categories
  */
sealed trait ConstructionErrorCategory {

  /**
    * @return unique number of the category
    */
  def number: Int
}

case object BadInput extends ConstructionErrorCategory {
  override val number: Int = 300
}

case object BuilderError extends ConstructionErrorCategory {
  override val number: Int = 500
}
