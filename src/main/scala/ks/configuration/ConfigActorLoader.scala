package ks.configuration

import settings.dependencies._
import types.Config
import scalaz.{\/, NonEmptyList, -\/, \/-}

object ConfigActorLoader {

  def load(): NonEmptyList[String] \/ Config = {
    val config = actorDependencies("actors.dependencies")
    ConfigValidator.validate(config) match {
      case Nil                   => \/-(config)
      case nonEmptyList@(_ :: _) => -\/(NonEmptyList.nel(nonEmptyList.head, nonEmptyList.tail))
    }
  }
}
