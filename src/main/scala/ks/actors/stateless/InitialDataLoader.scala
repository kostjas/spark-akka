package ks.actors.stateless

trait InitialDataLoader {
  
  def load(): List[String]
}

class InitialDataLoaderImpl {

  //suppose loading initial data e.g. from DB
  def load(): List[String] = List("msg1", "msg2", "msg3", "msg4")

}
