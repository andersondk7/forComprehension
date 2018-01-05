package org.dka.tutorial.forcomprehension.eitherbased

import org.dka.tutorial.forcomprehension.eitherbased.model.ConstructionError

package object service {
  type BuildResult[A] = Either[ConstructionError, A]

}
