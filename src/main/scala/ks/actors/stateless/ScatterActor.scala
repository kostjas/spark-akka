package ks.actors.stateless

import akka.actor.{Actor, ActorRef, Props}
import ks.configuration.ConfigParser.Layer
import types.Logging

import scala.collection.SortedMap

class ScatterActor(layers: Map[Int, Layer], dataLoader: InitialDataLoader) extends Actor with Logging {

  override def receive: Receive = {
    case Start =>
      val executorActors = createStructureOfExecutors(layers)

      val gatherer = context.actorOf(LayerGatherer.props(executorActors.head._2, executorActors.tail))

      executorActors.head._2.foreach { actor =>
        actor ! ExecutorStart(dataLoader.load(), gatherer)
      }

    case m => logger.error(s"Unsupportable type of message: $m .")
  }

  def createStructureOfExecutors(layers: Map[Int, Layer]): SortedMap[Int, Set[ActorRef]] = {
    layers.foldLeft(SortedMap.empty[Int, Set[ActorRef]])((acc, l) =>
      acc + ((l._1 , l._2.map(name => context.actorOf(Props(new ExecutorActor()), name)).toSet))
    )
  }
}

object ScatterActor {
  def props(layers: Map[Int, Layer], dataLoader: InitialDataLoader) = Props(new ScatterActor(layers, dataLoader))
}

case object Start
