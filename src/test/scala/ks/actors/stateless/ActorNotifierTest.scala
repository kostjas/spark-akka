package ks.actors.stateless

import akka.actor.{Actor, Props, ActorSystem}
import akka.testkit.{TestProbe, TestKit}
import org.specs2.matcher.Matchers
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope

import scala.collection.SortedMap

class ActorNotifierTest extends SpecificationWithJUnit with Matchers {

  "ActorNotifier" should {
    "notify next layer of executor actors" in new TestScope {

      system.actorOf(Props(new TestGatherActor)) ! "start"

      firstLayerActor1.expectMsgPF(){case ExecutorStart(List("data1", "data2", "data3"), _) => ok}
      firstLayerActor2.expectMsgPF(){case ExecutorStart(List("data1", "data2", "data3"), _) => ok}

      secondLayerActor.expectNoMsg()

      thirdLayerActor.expectNoMsg()
    }

    class TestScope extends TestKit(ActorSystem("test")) with Scope {

      val testService = new ActorNotifier

      class TestGatherActor extends Actor {
        override def receive: Receive = {
          case _ =>
            testService.notifyNextLayer(data, layers)
        }
      }

      val firstLayerActor1 = TestProbe()
      val firstLayerActor2 = TestProbe()

      val secondLayerActor = TestProbe()

      val thirdLayerActor = TestProbe()

      val data = List("data1", "data2", "data3")

      val layers = SortedMap(0 -> Set(firstLayerActor1.ref, firstLayerActor2.ref), 1 -> Set(secondLayerActor.ref), 2 -> Set(thirdLayerActor.ref))
    }
  }
}