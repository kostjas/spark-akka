package ks

import akka.actor.ActorSystem
import ks.actors.stateless.{Start, InitialDataLoaderImpl, ScatterActor}
import ks.configuration.{ConfigParser, ConfigActorLoader}

import scalaz.{-\/, \/-}

object Boot extends App {

  implicit val system = ActorSystem("TestActorSystem")

  system.log.info("Start application.")


  ConfigActorLoader.load() match {
    case \/-(config) =>
      system.log.info("Starting ScatterActor.")
      val scatterActor = system.actorOf(ScatterActor.props(ConfigParser.getLayers(config), new InitialDataLoaderImpl()))
      scatterActor ! Start
      system.log.info("Scatter actor was created and started!")
    case -\/(errors) =>
      system.log.error("Error occured during reading configuration: ")
      errors.list.foreach(system.log.error)
  }
}
