package object settings {

  import com.typesafe.config._

  private val config = ConfigFactory.load().getConfig("spark-akka")

  object db {
    val user   = config.getString("db.user")
    val pass   = config.getString("db.password")
    val url    = config.getString("db.url")
    val driver = config.getString("db.driver")
  }

  object dependencies {

    def actorDependencies(path: String): Map[String, Set[String]] = {
      import scala.collection.JavaConverters._

      val configDeps = config.getConfig(path)

      configDeps.entrySet().asScala.foldLeft[Map[String, Set[String]]](Map.empty)( (acc, c) =>
        acc + ((c.getKey, configDeps.getStringList(c.getKey).asScala.toSet))
      )
    }
  }
}
