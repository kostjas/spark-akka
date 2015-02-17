package ks.actors.stateless

import akka.actor.{Actor, ActorRef}
import LayerGatherer.FinishedSuccess
import types.{ActorData, Logging}


class ExecutorActor extends Actor with Logging {

  override def receive: Receive = {
    case ExecutorStart(data, gatherer) =>
      val result = executeFunction(data)
      gatherer ! FinishedSuccess(result)
    case msg => logger.error(s"This message cannot be processed: $msg !")
  }

  //Hier should be defined users` function
  def executeFunction(data: List[String]): ActorData = {
    //TODO fix logging
    context.system.log.info(s"Actor with name ${self.path.name} executed task and processed data: $data.")
    data.map(_ + self.path.name)
  }
}

case class ExecutorStart(data: List[String], gatherer: ActorRef)