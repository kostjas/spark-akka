package ks.configuration

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}
import scalaz.\/
import scalaz.std.string._
import scalaz.syntax.equal._
import scalaz.syntax.std.option._
import scala.concurrent.ExecutionContext.Implicits.global


object ConfigValidator {

  def validate(config: Map[String, Set[String]]): List[String] = {

    val selfCycles = Future(detectSelfCycles(config))
    val unsatisfied = Future(detectUnsatisfiedDependencies(config))
    val cycles = Future(detectCycles(config))

    Await.result(for {
      r1 <- selfCycles
      r2 <- unsatisfied
      r3 <- cycles
    } yield List(r1, r2, r3).flatten, Duration.Inf)


    // variant for sequential execution
/*    (for {
      _ <- detectSelfCycles(config).toLeftDisjunction(())
      _ <-  detectUnsatisfiedDependencies(config).toLeftDisjunction(())
      _ <- detectCycles(config).toLeftDisjunction(())
    } yield ()).fold(List(_), _ => Nil)*/
  }

  private def detectSelfCycles(config: Map[String, Set[String]]): Option[String] = {
    if (config.foldLeft(false)((acc, c) => acc || c._2.contains(c._1)))
      Some(s"There are self cycled dependencies in configuration: $config")
    else None
  }

  private def detectCycles(config: Map[String, Set[String]]): Option[String] = {
    Try(Tarjan.scc(config).filter({case p: (String, String) => p._1 =/= p._2})) match {
      case Success(errors) => if (errors.nonEmpty) Some(s"There are dependency cycles in configuration: $config") else None
      case Failure(e) => Some(e.getMessage)
    }
  }

  private def detectUnsatisfiedDependencies(config: Map[String, Set[String]]): Option[String] = {
    if (config.values.toSet.flatten.diff(config.keys.toSet).size > 0)
      Some(s"There are dependencies that not present in configuration: $config")
    else None
  }
}

