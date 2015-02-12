package ks.configuration

import org.specs2.mutable.SpecificationWithJUnit

class ConfigValidatorTest extends SpecificationWithJUnit {

  "ConfigValidator" should {

    "detect self cycles" in {
      val config = Map("task1" -> Set("task1"), "task2" -> Set("task1"))

      val result = ConfigValidator.validate(config)

      result must beLike {
        case h :: Nil => h.toString must beMatching("There are self cycled dependencies in configuration: .*")
      }
      ok
    }

    "not detect self cycles" in {
      val config = Map("task1" -> Set.empty[String], "task2" -> Set("task1"))

      val result = ConfigValidator.validate(config)

      result must beEmpty
    }

    "detect cycle dependencies" in {
      val config = Map("task1" -> Set("task2"), "task2" -> Set("task1"))

      val result = ConfigValidator.validate(config)

      result must beLike {
        case h :: Nil => h.toString must beMatching("There are dependency cycles in configuration: .*")
      }
    }

    "not detect cycle dependencies" in {
      val config = Map("task1" -> Set.empty[String], "task2" -> Set("task1"))

      val result = ConfigValidator.validate(config)

      result must beEmpty
    }

    "detect unsatisfied dependencies" in {
      val config = Map("task1" -> Set.empty[String], "task2" -> Set("task1", "task5"))

      val result = ConfigValidator.validate(config)

      result must beLike {
        case h :: tail => h.toString must beMatching("There are dependencies that not present in configuration: .*")
      }
    }

    "not detect unsatisfied dependencies" in {
      val config = Map("task2" -> Set("task1"), "task1" -> Set.empty[String])

      val result = ConfigValidator.validate(config)

      result must beEmpty
    }
  }
}
