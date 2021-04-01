package codacy.scalastyle

import java.io.File
import java.nio.file.Paths

import com.codacy.plugins.api._
import com.codacy.plugins.api.results.{Pattern, Result, Tool}
import com.codacy.tools.scala.seed.utils._
import com.codacy.tools.scala.seed.utils.ToolHelper._
import play.api.libs.json._

import org.scalastyle._

import scala.util.Try
import scala.xml.{Elem, Node, XML}
import com.typesafe.config.ConfigFactory

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

      val scalastyleConfiguration = {
        val pathOpt = getConfigFile(fullConfig).orElse(nativeConfigFile).map(_.getAbsolutePath)
        pathOpt match {
          case Some(path) => ScalastyleConfiguration.readFromXml(path)
          case None => ScalastyleConfiguration.getDefaultConfiguration()
        }
      }

      val messageHelper = new MessageHelper(ConfigFactory.load())

      val toolResult = new ScalastyleChecker()
        .checkFiles(scalastyleConfiguration, Directory.getFiles(None, filesToLint.map(new File(_))))
      parseToolResult(toolResult, messageHelper)
    }
  }

  private lazy val configFileName = "scalastyle_config.xml"
  private lazy val nativeConfigFileNames = Set(configFileName, "scalastyle-config.xml")

  private lazy val defaultConfigInputStream: java.io.InputStream =
    better.files.Resource.getAsStream(ScalastyleConfiguration.DefaultConfiguration.stripPrefix("/"))

  private lazy val scalaStyleConfig: Elem = XML.load(defaultConfigInputStream)

  private def parseToolResult(result: List[Message[FileSpec]], messageHelper: MessageHelper): List[Result] =
    result.collect {
      case styleError: StyleError[FileSpec] =>
        val patternId = styleError.clazz.getSimpleName()
        val message = styleError.customMessage.getOrElse(messageHelper.message(styleError.key, styleError.args))
        val line = styleError.lineNumber.getOrElse(0)

        Result.Issue(
          Source.File(styleError.fileSpec.name),
          Result.Message(message),
          Pattern.Id(patternId),
          Source.Line(line.toInt)
        )
      case styleException: StyleException[FileSpec] =>
        Result.FileError(Source.File(styleException.fileSpec.name), Some(ErrorMessage(styleException.message)))
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
      .flatMap(_.parameters.find(_.name.value == parameterName).map(jsValue => jsValueToString(jsValue.value)))
      .getOrElse(parameter.text.trim())
  }
}

case class ScalaStyleParserException(message: String) extends Exception(message)
