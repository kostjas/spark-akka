package ks

import akka.actor.ActorSystem

object Boot extends App {

  implicit val system = ActorSystem("TestActorSystem")



}
