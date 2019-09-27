package codacy.scalastyle

import java.io.File
import java.nio.file.Paths

import com.codacy.plugins.api._
import com.codacy.plugins.api.results.{Pattern, Result, Tool}
import com.codacy.tools.scala.seed.utils._
import com.codacy.tools.scala.seed.utils.ToolHelper._
import play.api.libs.json._

import scala.util.Try
import scala.xml.{Elem, Node, XML}

object ScalaStyle extends Tool {

  def apply(
      source: Source.Directory,
      configuration: Option[List[Pattern.Definition]],
      files: Option[Set[Source.File]],
      options: Map[Options.Key, Options.Value]
  )(implicit specification: Tool.Specification): Try[List[Result]] = {
    Try {

      lazy val nativeConfigFile: Option[File] =
        FileHelper.findConfigurationFile(Paths.get(source.path), nativeConfigFileNames).map(_.toFile)

      val fullConfig: Option[List[Pattern.Definition]] = configuration.withDefaultParameters

      val filesToLint: List[String] = files.fold(List(source.path.toString)) { paths =>
        paths.map(_.toString).toList
      }

      val configurationOption = List(
        "--config",
        //priorities: codacy-patterns then a native config in the project-root then the default config
        getConfigFile(fullConfig).orElse(nativeConfigFile).getOrElse(defaultConfigFile).getAbsolutePath
      )

      val command = List("java", "-jar", "/opt/docker/scalastyle.jar") ++ configurationOption ++ filesToLint

      CommandRunner.exec(command) match {
        case Right(resultFromTool) =>
          parseToolResult(resultFromTool.stdout, source)
        case Left(failure) => throw failure
      }
    }
  }

  private lazy val configFileName = "scalastyle_config.xml"
  private lazy val nativeConfigFileNames = Set(configFileName, "scalastyle-config.xml")

  private lazy val defaultConfigFile: File = {
    (better.files.File.root / "docs" / configFileName).toJava
  }

  private lazy val scalaStyleConfig: Elem = XML.loadFile(defaultConfigFile)

  private def parseToolResult(lines: List[String], source: Source.Directory): List[Result] = {

    val RegMatch = """([a-z]+) file=(.+) id=(.+) message=(.+) line=([0-9]+).*$""".r
    val FileErrorMatch = """^error file=(.+) message=(.+)""".r

    lines.collect {
      case RegMatch(level, file, id, message, line) =>
        val patternId = id.split('.').last
        val filePath = file
        Result.Issue(Source.File(filePath), Result.Message(message), Pattern.Id(patternId), Source.Line(line.toInt))

      case FileErrorMatch(file, message) =>
        Result.FileError(Source.File(file), Option(ErrorMessage(message)))
    }
  }

  private def getConfigFile(conf: Option[List[Pattern.Definition]]): Option[File] = {
    val customConfig = conf.map { patterns =>
      val rulesToApply = patterns.map(_.patternId.value)

      (scalaStyleConfig \ "check").map { check =>
        val level = (check \ "@level").text
        val clazz = (check \ "@class").text
        val patternName = clazz.split('.').last
        val enabled = rulesToApply.contains(patternName)

        val parameters = (check \ "parameters" \ "parameter").map { parameter =>
          val parameterName = (parameter \ "@name").text
          val paramValue = parameterValue(patterns, patternName, parameter, parameterName)

          s"""<parameters><parameter name="$parameterName"><![CDATA[$paramValue]]></parameter></parameters>"""
        }

        s"""<check level="$level" class="$clazz" enabled="$enabled"> ${parameters.mkString} </check>""".stripMargin
      }
    }

    customConfig.map {
      case newConf =>
        val scalaStyleNewConfig = "<scalastyle>" + newConf.mkString + "</scalastyle>"
        FileHelper.createTmpFile(scalaStyleNewConfig).toFile
    }
  }

  private def jsValueToString(value: JsValue) = {
    value match {
      case JsString(v) => v
      case v => v.toString
    }
  }

  private def parameterValue(
      patterns: List[Pattern.Definition],
      patternName: String,
      parameter: Node,
      parameterName: String
  ): String = {
    patterns
      .find(_.patternId.value == patternName)
      .flatMap(
        _.parameters.flatMap(_.find(_.name.value == parameterName).map(jsValue => jsValueToString(jsValue.value)))
      )
      .getOrElse(parameter.text.trim())
  }
}

case class ScalaStyleParserException(message: String) extends Exception(message)
