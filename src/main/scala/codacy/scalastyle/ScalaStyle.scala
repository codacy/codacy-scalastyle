package codacy.scalastyle

import java.io.File
import java.nio.file.Path

import codacy.dockerApi._
import codacy.dockerApi.utils.{CommandRunner, FileHelper, ToolHelper}
import play.api.libs.json.{JsString, JsValue}

import scala.util.Try
import scala.xml.{Elem, Node,XML}

object ScalaStyle extends Tool {

  override def apply(path: Path, conf: Option[List[PatternDef]], files: Option[Set[Path]])(implicit spec: Spec): Try[List[Result]] = {
    Try {

      lazy val nativeConfigFile: Option[File] = {
        configFileNames.to[Stream].map( name => better.files.File(path) / name ).find(_.isRegularFile).map(_.toJava)
      }

      val fullConf = ToolHelper.getPatternsToLint(conf)
      val filesToLint: List[String] = files.fold(List(path.toString)) {
        paths =>
          paths.map(_.toString).toList
      }

      val configuration = List("--config",
        //priorities: codacy-patterns then a native config in the project-root then the default config
        getConfigFile(fullConf).orElse(nativeConfigFile).getOrElse(defaultConfigFile).getAbsolutePath
      )

      val command = List("java", "-jar", "/opt/docker/scalastyle.jar") ++ configuration ++ filesToLint

      CommandRunner.exec(command) match {
        case Right(resultFromTool) =>
          parseToolResult(resultFromTool.stdout, path)
        case Left(failure) => throw failure
      }
    }
  }

  private lazy val configFileName = "scalastyle_config.xml"
  private lazy val configFileNames = List(configFileName, "scalastyle-config.xml")

  private lazy val defaultConfigFile: File = {
    (better.files.File.root / "docs" / configFileName).toJava
  }

  private lazy val scalaStyleConfig: Elem = XML.loadFile(defaultConfigFile)

  private def parseToolResult(lines: List[String], path: Path): List[Result] = {

    val RegMatch = """([a-z]+) file=(.+) id=(.+) message=(.+) line=([0-9]+).*$""".r
    val FileErrorMatch = """^error file=(.+) message=(.+)""".r

    lines.collect {
      case RegMatch(level, file, id, message, line) =>
        val patternId = id.split('.').last
        val filePath = file
        Issue(SourcePath(filePath), ResultMessage(message), PatternId(patternId), ResultLine(line.toInt))

      case FileErrorMatch(file, message) =>
        FileError(SourcePath(file), Some(ErrorMessage(message)))
    }
  }

  private def getConfigFile(conf: Option[List[PatternDef]]): Option[File] = {
    val customConfig = conf.map {
      patterns =>

        val rulesToApply = patterns.map(_.patternId.value)

        (scalaStyleConfig \ "check").map {
          check =>
            val level = (check \ "@level").text
            val clazz = (check \ "@class").text
            val patternName = clazz.split('.').last
            val enabled = rulesToApply.contains(patternName)

            val parameters = (check \ "parameters" \ "parameter").map {
              parameter =>
                val parameterName = (parameter \ "@name").text
                val paramValue = parameterValue(patterns, patternName, parameter, parameterName)

                s"""<parameters><parameter name="$parameterName"><![CDATA[$paramValue]]></parameter></parameters>"""
            }

            s"""<check level="$level" class="$clazz" enabled="$enabled"> ${parameters.mkString} </check>""".stripMargin
        }
    }

    customConfig.map { case newConf =>
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

  private def parameterValue(patterns: List[PatternDef], patternName: String, parameter: Node, parameterName: String): String = {
    patterns.find(_.patternId.value == patternName)
      .flatMap(_.parameters.flatMap(_.find(_.name.value == parameterName).map(jsValue => jsValueToString(jsValue.value))))
      .getOrElse(parameter.text.trim())
  }
}

case class ScalaStyleParserException(message: String) extends Exception(message)
