package codacy

import com.codacy.tools.scala.seed.DockerEngine
import codacy.scalastyle.ScalaStyle

object Engine extends DockerEngine(ScalaStyle)()
