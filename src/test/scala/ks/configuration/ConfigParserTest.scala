package ks.configuration

import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope

class ConfigParserTest extends SpecificationWithJUnit {

  "ConfigParser" should {

    "parse trivial resource" in new TestScope {
      val resource = createResource("task1", Set.empty[String])

      val result = ConfigParser.getLayers(Map(resource))

      result must_== Map((0, Set("task1")))
    }

    "parse resources with one line dependency" in new TestScope {
      val resource1 = createResource("task1", Set())
      val resource2 = createResource("task2", Set("task1"))

      val result = ConfigParser.getLayers(Map(resource1, resource2))

      result must_== Map((0, Set("task1")), (1, Set("task2")))
    }

    "parse resources with one line dependency (different order of resources)" in new TestScope {
      val resource1 = createResource("task1", Set())
      val resource2 = createResource("task2", Set("task1"))

      val result = ConfigParser.getLayers(Map(resource2, resource1))

      result must_== Map((0, Set("task1")), (1, Set("task2")))
    }

    "should work with two line dependencies" in new TestScope {
      val resource1 = createResource("task1", Set.empty[String])
      val resource2 = createResource("task2", Set("task1"))
      val resource3 = createResource("task3", Set.empty[String])
      val resource4 = createResource("task4", Set("task3"))

      val result = ConfigParser.getLayers(Map(resource1, resource2, resource3, resource4))

      result must_== Map((0, Set("task1", "task3")), (1, Set("task2", "task4")))
    }

    "should work with two line dependencies (different order of resources)" in new TestScope {
      val resource1 = createResource("task1", Set.empty[String])
      val resource2 = createResource("task2", Set("task1"))
      val resource3 = createResource("task3", Set.empty[String])
      val resource4 = createResource("task4", Set("task3"))

      val result = ConfigParser.getLayers(Map(resource3, resource1, resource4, resource2))

      result must_== Map((0, Set("task1", "task3")), (1, Set("task2", "task4")))
    }

    "should work with mixed dependencies" in new TestScope {

      var resource1 = createResource("task1", Set("task2", "task4"))
      var resource2 = createResource("task2", Set("task3"))
      var resource3 = createResource("task3", Set("task6"))
      var resource4 = createResource("task4", Set("task5"))
      var resource5 = createResource("task5", Set())
      var resource6 = createResource("task6", Set())

      val result = ConfigParser.getLayers(Map(resource1, resource2, resource3, resource4, resource5, resource6))

      result must_== Map( (0, Set("task5", "task6")), (1, Set("task3", "task4")), (2, Set("task2")), (3, Set("task1")))
    }

    "should work with mixed dependencies (different order)" in new TestScope {
      var resource1 = createResource("task1", Set("task2", "task4"))
      var resource2 = createResource("task2", Set("task3"))
      var resource3 = createResource("task3", Set("task6"))
      var resource4 = createResource("task4", Set("task5"))
      var resource5 = createResource("task5", Set())
      var resource6 = createResource("task6", Set())

      val result = ConfigParser.getLayers(Map(resource6, resource5, resource4, resource3, resource2, resource1))

      result must_== Map( (0, Set("task5", "task6")), (1, Set("task3", "task4")), (2, Set("task2")), (3, Set("task1")))
    }
  }
  
  class TestScope extends Scope {
    def createResource(name: String, dependencies: Set[String]): (String, Set[String]) = (name, dependencies)
  }
}
