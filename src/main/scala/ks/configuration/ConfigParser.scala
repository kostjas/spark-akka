package ks.configuration

import types.Config
import scalaz.std.string._
import scalaz.syntax.equal._

object ConfigParser {

  type Layer = Set[String]
  case class Resource(name: String, dependencies: Set[String])

  def getLayers(config: Config): Map[Int, Layer] = {

    val resources = config.map(c => Resource(c._1, c._2)).toSet

    def addToLayer(resourceName: String, indexOfLayer: Int, layers:  Map[Int, Layer]): Map[Int, Layer] = {
      layers.get(indexOfLayer) match {
        case Some(l) => layers + ((indexOfLayer, l + resourceName))
        case None    => layers + ((indexOfLayer, Set(resourceName)))
      }
    }

    def getLayer(element: String, layers: Map[Int, Layer]): Int = {
      layers.foldLeft(-1)((acc, layer) =>
        (acc, layer) match {
          case (-1, _)           => if (layer._2.contains(element)) layer._1 else acc
          case (indexOfLayer, _) => indexOfLayer
        })
    }


    def countLayer(resource: Resource, layers: Map[Int, Layer], alreadyAligned: Set[String]): Int = {

      if (alreadyAligned.contains(resource.name)) getLayer(resource.name, layers)

      else if (resource.dependencies.isEmpty) 0

      else resource.dependencies.map{ name =>
              countLayer(
                resources.find(_.name === name).getOrElse(throw new IllegalStateException(s"This resource $resource is not present in configuration.")),
                layers,
                alreadyAligned
              )
           }.max + 1
    }

    def processResources(resources: Set[Resource], layers: Map[Int, Layer], alreadyAligned: Set[String]): Map[Int, Layer] = {
      if (resources.isEmpty) layers
      else {
        val resource = resources.head
        val unprocessedResources = resources.tail

        if (resource.dependencies.isEmpty)
          processResources(unprocessedResources, addToLayer(resource.name, 0, layers), alreadyAligned + resource.name)

        val indexOfLayer = countLayer(resource, layers, alreadyAligned)

        processResources(unprocessedResources, addToLayer(resource.name, indexOfLayer, layers), alreadyAligned + resource.name)
      }
    }

    processResources(resources, Map.empty[Int, Layer], Set.empty[String])
  }
}
