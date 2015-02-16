package ks.actors.stateless

import akka.actor.{ActorContext, ActorRef}

import scala.collection.SortedMap

trait IActorNotifier {
  def notifyNextLayer(data: List[String],
                      anotherLayers: SortedMap[Int, Set[ActorRef]])
                     (implicit context: ActorContext)
}

class ActorNotifier extends IActorNotifier {
  override def notifyNextLayer(data: List[String],
                               anotherLayers: SortedMap[Int, Set[ActorRef]])
                              (implicit context: ActorContext): Unit = {
    val nextLayer = anotherLayers.head._2

    val gatherer = context.actorOf(LayerGatherer.props(nextLayer, anotherLayers.tail))

    nextLayer.foreach( _ ! ExecutorStart(data, gatherer) )
  }
}
