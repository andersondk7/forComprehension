package org.dka.tutorial.forcomprehension.v2

import org.dka.tutorial.forcomprehension.v2.model.ConstructionError

package object service {
  type BuildResult[A] = Either[ConstructionError, A]

}
