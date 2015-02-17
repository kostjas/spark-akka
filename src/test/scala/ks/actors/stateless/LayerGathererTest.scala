package ks.actors.stateless

import akka.actor._
import akka.testkit.{TestFSMRef, ImplicitSender, TestKit}
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.matcher.Scope
import org.specs2.mock.Mockito

import scala.collection.SortedMap

class LayerGathererTest extends SpecificationWithJUnit with Mockito {

  import ks.actors.stateless.LayerGatherer._

  "LayerGatherer" should {

    "start with ´WaitingLayer´ state and unprocessed data" in new InitialTestScope {

      val currentAwaitedLayer: Set[ActorRef] = Set(system.actorOf(Props(new ExecutorActor())))
      val anotherLayers: SortedMap[Int, Set[ActorRef]] = SortedMap.empty

      val actorRef = createActor(currentAwaitedLayer, anotherLayers)

      val expectedData = AwaitedActorsData(Map(currentAwaitedLayer.map(a => (a, false)).toSeq: _*), List.empty[String])

      actorRef.stateName must_== WaitingLayer
      actorRef.stateData must_== expectedData
    }

    "stop if currentAwaitedLayer is empty" in new InitialTestScope {

      val currentAwaitedLayer: Set[ActorRef] = Set.empty
      val anotherLayers: SortedMap[Int, Set[ActorRef]] = SortedMap.empty

      val actorRef = createActor(currentAwaitedLayer, anotherLayers)

      watch(actorRef)

      expectMsgPF(){
        case Terminated(`actorRef`) => ok
        case _ => ko
      }
    }

    "receive and process message" in new ProcessOneMessageHelper {

      val expectedInitialData = AwaitedActorsData(Map(currentAwaitedLayer.map(a => (a, false)).toSeq: _*), List.empty[String])

      actorRef.stateName must_== WaitingLayer
      actorRef.stateData must_== expectedInitialData

      actorRef receive (FinishedSuccess(data), executorActor1)

      val expectedData = AwaitedActorsData(Map(currentAwaitedLayer.map(a => (a, true)).toSeq: _*), data)

      actorRef.stateName must_== Done
      actorRef.stateData must_== expectedData
      there was one(actorNotifierMock).notifyNextLayer(data, anotherLayers)(actorRef.underlyingActor.context)
    }

    "receive messages and process not complete chain of messages" in new ProcessSeveralMessagesHelper {
      import types._

      actorRef receive (FinishedSuccess(data), executorActor1)
      actorRef receive (FinishedSuccess(data), executorActor3)

      val expectedData = AwaitedActorsData(Map(executorActor1 -> true, executorActor2 -> false, executorActor3 -> true), data ::: data)

      actorRef.stateName must_== WaitingLayer
      actorRef.stateData must_== expectedData

      there was no(actorNotifierMock)
    }

    "receive messages and process all chain of messages in arbitrary order" in new ProcessSeveralMessagesHelper {
      import types._

      actorRef receive (FinishedSuccess(data), executorActor1)
      actorRef receive (FinishedSuccess(data), executorActor3)
      actorRef receive (FinishedSuccess(data), executorActor2)

      val expectedSuccessData = AwaitedActorsData(Map(currentAwaitedLayer.map{a => (a, true)}.toSeq: _*), data ::: data ::: data)

      actorRef.stateName must_== Done
      actorRef.stateData must_== expectedSuccessData
      there was one(actorNotifierMock).notifyNextLayer(data, anotherLayers)(actorRef.underlyingActor.context)
    }
  }

  class InitialTestScope extends TestKit(ActorSystem("test"))
                                    with Scope
                                    with ImplicitSender {

    val actorNotifierMock = mock[IActorNotifier]

    val testActorName = "testActorName"

    def createActor(currentAwaitedLayer: Set[ActorRef], anotherLayers: SortedMap[Int, Set[ActorRef]]) =
      TestFSMRef(new LayerGatherer(currentAwaitedLayer, anotherLayers, actorNotifierMock), testActorName)
  }


  class ProcessOneMessageHelper extends TestKit(ActorSystem("test"))
                                with Scope
                                with ImplicitSender {

    val data = List("data1", "data2", "data3")

    val actorNotifierMock = mock[IActorNotifier]

    val executorActor1 = system.actorOf(Props(new ExecutorActor))

    val currentAwaitedLayer: Set[ActorRef] = Set(executorActor1)
    val anotherLayers: SortedMap[Int, Set[ActorRef]] = SortedMap((0, Set(system.actorOf(Props(new ExecutorActor())))))

    val actorRef = TestFSMRef(new LayerGatherer(currentAwaitedLayer, anotherLayers, actorNotifierMock))
  }

  class ProcessSeveralMessagesHelper extends TestKit(ActorSystem("test"))
                                     with Scope
                                     with ImplicitSender {

    val data = List("data1", "data2", "data3")

    val actorNotifierMock = mock[IActorNotifier]

    val executorActor1 = system.actorOf(Props(new ExecutorActor))
    val executorActor2 = system.actorOf(Props(new ExecutorActor))
    val executorActor3 = system.actorOf(Props(new ExecutorActor))

    val currentAwaitedLayer: Set[ActorRef] = Set(executorActor1, executorActor2, executorActor3)
    val anotherLayers: SortedMap[Int, Set[ActorRef]] = SortedMap((0, Set(system.actorOf(Props(new ExecutorActor())))))

    val actorRef = TestFSMRef(new LayerGatherer(currentAwaitedLayer, anotherLayers, actorNotifierMock))
  }
}
