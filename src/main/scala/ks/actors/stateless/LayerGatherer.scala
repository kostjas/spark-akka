package ks.actors.stateless

import akka.actor.FSM.Normal
import akka.actor.{ActorRef, Actor, LoggingFSM, Props}
import scala.collection.SortedMap
import scalaz.std.string._
import scalaz.syntax.equal._

class LayerGatherer(currentAwaitedLayer: Set[ActorRef], anotherLayers: SortedMap[Int, Set[ActorRef]], notifier: IActorNotifier)
                    extends Actor with LoggingFSM[LayerGatherer.State, LayerGatherer.Data] {

  import ks.actors.stateless.LayerGatherer._

  if (currentAwaitedLayer.nonEmpty)
    startWith(WaitingLayer, AwaitedActorsData(Map(currentAwaitedLayer.map(a => (a, false)).toSeq: _*), List.empty[String]))
  else {
    log.error("Current awaited layer is empty!")
    startWith(Stopped, NoData)
    self ! "terminate"
  }


  when(WaitingLayer) {
    case Event(FinishedSuccess(senderName, resultData), currentStateData: AwaitedActorsData) =>
      val newState = processMessage(senderName, resultData, currentStateData)
      if (newState.processedActors.forall(_._2)) {
        implicit val system = context.system
        notifier.notifyNextLayer(resultData, anotherLayers)
        stop(Normal)
      } else stay() using newState
  }

  when(Stopped) {
    case Event("terminate", _) => stop(Normal)
  }

  initialize()

  def processMessage(actorName: String, resultData: List[String], stateData: AwaitedActorsData): AwaitedActorsData = {
    stateData match {
      case AwaitedActorsData(processedDeps, data) =>
        val processedActors = processedDeps.map { d =>
          if (d._1.path.name === actorName) (d._1, true) else d
        }
        AwaitedActorsData(processedActors, data ::: resultData)
    }
  }
}

object LayerGatherer {
  sealed trait Action
  case class FinishedSuccess(actorName: String, data: List[String]) extends Action

  sealed trait Data
  case object NoData extends Data
  case class AwaitedActorsData(processedActors: Map[ActorRef, Boolean], summaryResult: List[String]) extends Data

  sealed trait State
  case object Stopped extends State
  case object WaitingLayer extends State
  case object Done extends State

  def props(currentAwaitedLayer: Set[ActorRef], anotherLayers: SortedMap[Int, Set[ActorRef]]) = Props(new LayerGatherer(currentAwaitedLayer, anotherLayers, new ActorNotifier))
}

