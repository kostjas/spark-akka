package ks.actors.stateless

import akka.actor.{Terminated, Props, ActorRef, ActorSystem}
import akka.testkit.{TestFSMRef, ImplicitSender, TestKit}
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.matcher.Scope
import org.specs2.mock.Mockito

import scala.collection.SortedMap

class LayerGathererTest extends SpecificationWithJUnit with Mockito {

  import ks.actors.stateless.LayerGatherer._

  "LayerGatherer" should {

    "start with ´WaitingLayer´ state and unprocessed data" in new LayerGathererScope {

      val currentAwaitedLayer: Set[ActorRef] = Set(system.actorOf(Props(new ExecutorActor())))
      val anotherLayers: SortedMap[Int, Set[ActorRef]] = SortedMap.empty

      val actorRef = createActor(currentAwaitedLayer, anotherLayers)

      val expectedData = AwaitedActorsData(Map(currentAwaitedLayer.map(a => (a, false)).toSeq: _*), List.empty[String])

      actorRef.stateName must_== WaitingLayer
      actorRef.stateData must_== expectedData
    }

    "stop if currentAwaitedLayer is empty" in new LayerGathererScope {

      val currentAwaitedLayer: Set[ActorRef] = Set.empty
      val anotherLayers: SortedMap[Int, Set[ActorRef]] = SortedMap.empty

      val actorRef = createActor(currentAwaitedLayer, anotherLayers)

      watch(actorRef)

      expectMsgPF(){
        case Terminated(`actorRef`) => ok
        case _ => ko
      }
    }
  }

  class LayerGathererScope extends TestKit(ActorSystem("test"))
                                    with Scope
                                    with ImplicitSender {

    val actorNotifierMock = mock[IActorNotifier]

    val testActorName = "testActorName"

    //val actorRef = TestFSMRef(new LayerGatherer(currentAwaitedLayer, anotherLayers, actorNotifierMock), testActorName)

    def createActor(currentAwaitedLayer: Set[ActorRef], anotherLayers: SortedMap[Int, Set[ActorRef]]) =
      TestFSMRef(new LayerGatherer(currentAwaitedLayer, anotherLayers, actorNotifierMock), testActorName)
  }

}
