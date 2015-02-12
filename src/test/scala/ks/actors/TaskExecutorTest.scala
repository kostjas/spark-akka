package ks.actors

import ks.actors.TaskExecutor._
import akka.actor.{ActorRef, Actor, Props, ActorSystem}
import akka.testkit.{ImplicitSender, TestFSMRef, TestKit}
import org.specs2.mock.Mockito
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope

class TaskExecutorTest extends SpecificationWithJUnit with Mockito {

  "TaskExecutor" should {

    "start with ´Waiting´ state and unprocessed data" in new TaskExecutorScope {
      override def dependencyIds: Set[String] = Set("task1", "task2", "task3")

      override def recipientIds: Set[String] = Set.empty[String]

      actorRef.stateName must_== Waiting

      actorRef.stateData must_== ProcessedData(Map(("task1", false), ("task2", false), ("task3", false)))

      there was no(notificationServiceMock)
    }

    "receive messages and process message" in new TaskExecutorScope {
      override def dependencyIds: Set[String] = Set("task1")

      override def recipientIds: Set[String] = Set.empty[String]

      actorRef.stateName must_== Waiting
      actorRef.stateData must_== ProcessedData(Map(("task1", false)))

      actorRef receive Ready("task1")

      actorRef.stateName must_== Done
      actorRef.stateData must_== ProcessedData(Map(("task1", true)))

      there was one(notificationServiceMock).notifyAllRecipients(testActorName, recipientIds)
    }

    "receive messages and process chain of messages in arbitrary order" in new TaskExecutorScope {
      override def dependencyIds: Set[String] = Set("task1", "task2", "task3")

      override def recipientIds: Set[String] = Set("task5", "task6")

      actorRef receive Ready("task2")

      actorRef.stateName must_== Waiting
      actorRef.stateData must_== ProcessedData(Map(("task1", false), ("task2", true), ("task3", false)))

      actorRef receive Ready("task1")
      actorRef receive Ready("task3")

      actorRef.stateName must_== Done
      actorRef.stateData must_== ProcessedData(Map(("task1", true), ("task2", true), ("task3", true)))

      there was one(notificationServiceMock).notifyAllRecipients(testActorName, recipientIds)
    }

    "start automatically if there are no dependencies" in new TaskExecutorScope {
      override def dependencyIds: Set[String] = Set.empty[String]

      override def recipientIds: Set[String] = Set("task2", "task3", "task4")

      actorRef.stateName must_== Done
      actorRef.stateData must_== NoData

      there was one(notificationServiceMock).notifyAllRecipients(testActorName, recipientIds)
    }
  }


  abstract class TaskExecutorScope extends TestKit(ActorSystem("test"))
                          with Scope
                          with ImplicitSender {

    def recipientIds: Set[String]
    def dependencyIds: Set[String]

    val notificationServiceMock = mock[INotificationService]

    val testActorName = "testActorName"

    val actorRef = TestFSMRef(new TaskExecutor(recipientIds, dependencyIds, notificationServiceMock), testActorName)

/*    def createSourceActor(actorName: String): ActorRef = {
      system.actorOf(Props(new Actor() {
        override def receive: Actor.Receive = {case _ => }
      }), actorName)
    }*/
  }
}
