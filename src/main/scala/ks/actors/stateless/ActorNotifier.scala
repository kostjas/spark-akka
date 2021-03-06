package ks.actors.stateless

import akka.actor.{ActorContext, ActorRef}
import types.Logging

import scala.collection.SortedMap

trait IActorNotifier {
  def notifyNextLayer(data: List[String],
                      anotherLayers: SortedMap[Int, Set[ActorRef]])
                     (implicit context: ActorContext)
}

class ActorNotifier extends IActorNotifier with Logging {
  override def notifyNextLayer(data: List[String],
                               anotherLayers: SortedMap[Int, Set[ActorRef]])
                              (implicit context: ActorContext): Unit = {
    anotherLayers.headOption match {
      case Some((_, actors)) if actors.nonEmpty =>
        val gatherer = context.actorOf(LayerGatherer.props(actors, anotherLayers.tail))
        actors.foreach( _ ! ExecutorStart(data, gatherer) )

      case None => logger.info("Processing was finished!!!!")
    }

  }
}
