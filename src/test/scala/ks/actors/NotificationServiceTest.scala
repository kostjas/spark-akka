package ks.actors

import akka.actor.{Actor, Props, ActorSystem}
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope

class NotificationServiceTest extends SpecificationWithJUnit {

  "NotificationService" should {

    "notify all recipient actors" in new TestScope {


      ok
    }
  }

  class TestScope extends Scope {

    val recipients = Set("task2", "task3", "task4")



    val system = ActorSystem("test")

    val actor1 = system.actorOf(Props(new TestActor))
  }

  class TestActor extends Actor {
    override def receive: Receive = {case _ =>}
  }
}
