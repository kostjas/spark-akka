package ks.actors

import akka.actor.Actor.Receive
import akka.actor.{Actor, Props, ActorSystem}
import akka.testkit.{TestActor, TestProbe}
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope

class NotificationServiceTest extends SpecificationWithJUnit {

  "NotificationService" should {

    "notify all recipient actors" in new TestScope {


      ko
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
