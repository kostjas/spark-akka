package ks

import org.apache.spark.{SparkConf, SparkContext}

object DatabaseConnector extends App {

  val classes = Seq(
    getClass,                   // To get the jar with our own code.
    classOf[com.mysql.jdbc.Driver]  // To get the connector.
  )
  val jars = classes.map(_.getProtectionDomain.getCodeSource.getLocation.getPath)
  val conf = new SparkConf(false)
             .setMaster("local[*]")
             .setJars(jars)
             .setAppName("Spark with Scala and Akka")

  val sc = new SparkContext(conf)

  val rdd = new org.apache.spark.rdd.JdbcRDD[Trader](
    sc,
    () => {
      java.sql.DriverManager.getConnection("jdbc:mysql://localhost:3306/akka_spark?user=root&password=123456")
    },
    "SELECT * FROM TRADER WHERE ? <= ID AND ID <= ?",
    0,
    1000,
    10,
    row => Trader(row.getLong("ID"), row.getString("NAME"))
  )


  rdd.foreach{tr =>
   println(s"Name: ${tr.id}, ${tr.name}")
  }

  case class Trader(id: Long, name: String)

}
