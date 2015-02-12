package ks.configuration

import org.specs2.mutable.SpecificationWithJUnit
import test_settings.testDependencies

class TarjanTest extends SpecificationWithJUnit {

  "Tarjan.scc" should {

    "visit all edges of the graph" in {

      val depsWithoutSCC = testDependencies.dependenciesWithoutSCC

      val result = Tarjan.scc(depsWithoutSCC)

      result.keys.toList must_== depsWithoutSCC.keys.toList
    }

    "should find only strong connected nodes without links to itself case 1" in {

      val result = Tarjan.scc(testDependencies.strongDependencies1)

      val expectedResult =

        Map("task1" -> "task3",
            "task2" -> "task2",
            "task3" -> "task3",
            "task4" -> "task2",
            "task5" -> "task5"
        )

      expectedResult must_== result
    }

    "should find only strong connected nodes without links to itself case 2" in {

      val result = Tarjan.scc(testDependencies.strongDependencies2)

      val expectedResult =

        Map("task1" -> "task3",
          "task2" -> "task3",
          "task3" -> "task3",
          "task4" -> "task3",
          "task5" -> "task5"
        )

      expectedResult must_== result
    }
  }
}
