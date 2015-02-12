package ks.actors

import TaskExecutor.Ready


trait INotificationService {
  def notifyAllRecipients(sourceId: String, recipients: Set[String])
}

class NotificationService extends INotificationService {
  override def notifyAllRecipients(sourceId: String, recipientIds: Set[String]): Unit = {
    recipientIds.foreach{taskId =>
      import ks.BaseSystem._
      val actorRef = system.actorSelection(s"*/*$taskId")
      actorRef ! Ready(sourceId)
    }
  }
}
