package ks

import akka.actor.{ActorRef, Props, ActorSystem}
import ks.actors.{NotificationService, TaskExecutor}
import ks.configuration.{ConfigActorLoader, ConfigParser}
import types.Config

import scalaz.{-\/, \/-}
import scalaz.std.string._
import scalaz.syntax.equal._

object BaseSystem extends App {

  implicit val system = ActorSystem("TestActorSystem")


  system.log.info("Start application.")


  ConfigActorLoader.load() match {
    case \/-(config) =>
      system.log.info("Creating structure of actors.")
      createStructure(config)
      system.log.info("Structure of actors was created.")
    case -\/(errors) =>
      system.log.error("Error occured during reading configuration: ")
      errors.list.foreach(system.log.error)
  }

  def createStructure(config: Config): Set[ActorRef] = {
    val layers = ConfigParser.getLayers(config)

    def findDependencies(task: String): Set[String] = {
      config.get(task).get
    }

    def findRecipients(task: String): Set[String] = {
      config.filter(c => c._1 =/= task && c._2.contains(task)).map(_._1).toSet
    }

    val reversedLayers = layers.toList.sortBy(-_._1)

    reversedLayers.foldLeft(Set.empty[ActorRef])((acc, l) =>
      acc ++ l._2.map(name => system.actorOf(Props(new TaskExecutor(findRecipients(name), findDependencies(name), new NotificationService)), name)).toSet
    )
  }
}
