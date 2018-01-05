package org.dka.tutorial.forcomprehension.actorbased

import org.dka.tutorial.forcomprehension.actorbased.model.ConstructionError

package object service {
  type BuildResult[A] = Either[ConstructionError, A]

}
