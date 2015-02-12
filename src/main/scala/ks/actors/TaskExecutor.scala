package ks.actors

import akka.actor.{Actor, LoggingFSM, Props}
import scalaz.syntax.equal._
import scalaz.std.string._
import types._

class TaskExecutor(recipientIds: Set[String], dependencyIds: Set[String], notificationService: INotificationService)
                  extends Actor with LoggingFSM[TaskExecutor.State, TaskExecutor.Data] {

  import ks.actors.TaskExecutor._

  val firstLayerActor: Boolean = dependencyIds.isEmpty

  if (firstLayerActor) startWith(Waiting, NoData)
  else startWith(Waiting, ProcessedData(dependencies = Map(dependencyIds.map(id => (id, false)).toSeq: _*)))

  if (firstLayerActor) self ! Ready(self.path.name)

  when(Waiting) {
    case Event(Ready(_), _) if firstLayerActor && self === sender() =>
      val senderName = sender().path.name
      executeUserFunction(senderName)
      notificationService.notifyAllRecipients(self.path.name, recipientIds)
      goto(Done) using NoData

    case Event(Ready(senderName), _) =>
      val newState = processMessage(senderName)
      executeUserFunction(senderName)
      if (newState.forall(_._2)) {
        notificationService.notifyAllRecipients(self.path.name, recipientIds)
        goto(Done) using ProcessedData(newState)
      } else stay() using ProcessedData(newState)
  }

  when(Done) {
    case Event(_, _) => stay()
  }

  whenUnhandled {
    case Event(s, _) =>
      log.error(s"Unsupportable state! $s")
      goto(Waiting)
  }

  initialize()

  def processMessage(actorId: String): Map[String, Boolean] = {
    stateData match {
      case NoData => Map.empty
      case ProcessedData(processedDeps) =>
        processedDeps.map { d =>
          if (d._1 === actorId) (d._1, true) else d
        }
    }
  }

  def executeUserFunction(data: String) = {log.info(s"Got data: $data is ready and try to execute user´s function.")}
}

object TaskExecutor {
  sealed trait State
  case object Waiting extends State
  case object Done extends State

  sealed trait Data
  case object NoData extends Data
  case class ProcessedData(dependencies: Map[String, Boolean]) extends Data

  sealed trait Action
  case class Ready(actorName: String) extends Action

  def props(recipientIds: Set[String], dependencyIds: Set[String], notificationService: INotificationService): Props = {
    Props(new TaskExecutor(recipientIds, dependencyIds, notificationService))
  }
}