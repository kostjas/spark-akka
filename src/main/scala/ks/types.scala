package object types {
  import akka.actor.ActorRef
  import scalaz.Equal

  implicit val actorRefEqual = Equal.equalA[ActorRef]

  type Config = Map[String, Set[String]]
  type Logging = com.typesafe.scalalogging.StrictLogging
}