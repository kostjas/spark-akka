package object test_settings {

  import com.typesafe.config._

  private val config = ConfigFactory.load().getConfig("spark-akka")

  object db {
    val user = config.getString("db.user")
    val pass = config.getString("db.password")
    val url = config.getString("db.url")
    val driver = config.getString("db.driver")
  }

  object testDependencies {

    import settings.dependencies._

    def dependenciesWithoutSCC: Map[String, Set[String]] = {
      actorDependencies("actors.dependenciesWithoutSCC")
    }

    def strongDependencies1: Map[String, Set[String]] = {
      actorDependencies("actors.strongDependencies1")
    }

    def strongDependencies2: Map[String, Set[String]] = {
      actorDependencies("actors.strongDependencies2")
    }

    def strongDependencies3: Map[String, Set[String]] = {
      actorDependencies("actors.strongDependencies3")
    }
  }
}
