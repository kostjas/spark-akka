package ks.actors.stateless

import akka.actor.{Actor, ActorRef, Props}
import ks.configuration.ConfigParser
import ks.configuration.ConfigParser.Layer
import types.{Config, Logging}

import scala.collection.SortedMap

class ScatterActor(config: Config, dataLoader: InitialDataLoader) extends Actor with Logging {

  override def receive: Receive = {
    case _ =>

      val layers = ConfigParser.getLayers(config)

      val executorActors = createStructureOfExecutors(layers)

      executorActors.head._2.foreach { actor =>
        actor ! ExecutorStart(dataLoader.load(), context.actorOf(LayerGatherer.props(executorActors.head._2, executorActors.tail)))
      }
  }

  def createStructureOfExecutors(layers: Map[Int, Layer]): SortedMap[Int, Set[ActorRef]] = {
    layers.foldLeft(SortedMap.empty[Int, Set[ActorRef]])((acc, l) =>
      acc + ((l._1 , l._2.map(name => context.actorOf(Props(new ExecutorActor()), name)).toSet))
    )
  }
}
