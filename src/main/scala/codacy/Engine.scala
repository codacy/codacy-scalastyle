package codacy

import codacy.dockerApi.DockerEngine
import codacy.scalastyle.ScalaStyle

object Engine extends DockerEngine(ScalaStyle)