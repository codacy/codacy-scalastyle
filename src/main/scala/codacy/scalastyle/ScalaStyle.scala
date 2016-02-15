package codacy.scalastyle

import java.io.File
import java.nio.file.Path

import codacy.dockerApi.utils.{CommandRunner, ToolHelper, FileHelper}
import codacy.dockerApi._

import scala.util.Try
import scala.xml.Elem

object ScalaStyle extends Tool {

  override def apply(path: Path, conf: Option[List[PatternDef]], files: Option[Set[Path]])(implicit spec: Spec): Try[List[Result]] = {
    Try {
      val fullConf = ToolHelper.getPatternsToLint(conf)
      val filesToLint: List[String] = files.fold(List(path.toString)) {
        paths =>
          paths.map(_.toString).toList
      }

      val configuration =
        fullConf.fold(List.empty[String]) {
          case patternsToLint if patternsToLint.nonEmpty =>
            List("--config", getConfigFile(patternsToLint).getAbsolutePath.toString)
          case _ => List.empty[String]
        }

      val command = List("java", "-jar", "/opt/docker/scalastyle.jar", "--quiet", "true") ++ configuration ++ filesToLint

      CommandRunner.exec(command) match {
        case Right(resultFromTool) =>
          parseToolResult(resultFromTool.stdout, path)
        case Left(failure) => throw failure
      }
    }
  }

  def parseToolResult(lines: List[String], path: Path): List[Result] = {

    val RegMatch = """([a-z]+) file=(.+) id=(.+) message=(.+) line=([0-9]+).*$""".r
    val ErrorMatch = """^Error.*""".r

    lines.collect {
      case RegMatch(level, file, id, message, line) =>
        val patternId = id.split('.').last
        val filePath = file
        Issue(SourcePath(filePath), ResultMessage(message), PatternId(patternId), ResultLine(line.toInt))

      case line@ErrorMatch() =>
        //FileError()
        throw new ScalaStyleParserException("ScalaStyle crashed: " + lines.mkString("\n"))

      case line =>
        FileError(SourcePath("oops"), Some(ErrorMessage("ScalaStyle crashed: " + lines.mkString("\n"))))
    }
  }

  private lazy val scalaStyleConfig: Elem =
    <scalastyle commentFilter="enabled">
      <name>Scalastyle standard configuration</name>
      <check level="warning" class="org.scalastyle.file.FileTabChecker" enabled="false"/>
      <check level="warning" class="org.scalastyle.file.FileLengthChecker" enabled="false">
        <parameters>
          <parameter name="maxFileLength">
            <![CDATA[ 800 ]]>
          </parameter>
        </parameters>
      </check>
      <check level="warning" class="org.scalastyle.file.HeaderMatchesChecker" enabled="false">
        <parameters>
          <parameter name="header">
            <![CDATA[
                // Copyright (C) 2011-2012 the original author or authors. // See the LICENCE.txt file distributed with this work for additional // information regarding copyright ownership. // // Licensed under the Apache License, Version 2.0 (the "License"); // you may not use this file except in compliance with the License. // You may obtain a copy of the License at // // http://www.apache.org/licenses/LICENSE-2.0 // // Unless required by applicable law or agreed to in writing, software // distributed under the License is distributed on an "AS IS" BASIS, // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. // See the License for the specific language governing permissions and // limitations under the License.
                ]]>
          </parameter>
        </parameters>
      </check>
      <check level="warning" class="org.scalastyle.scalariform.SpacesAfterPlusChecker" enabled="false"/>
      <check level="warning" class="org.scalastyle.file.WhitespaceEndOfLineChecker" enabled="false"/>
      <check level="warning" class="org.scalastyle.scalariform.SpacesBeforePlusChecker" enabled="false"/>
      <check level="warning" class="org.scalastyle.file.FileLineLengthChecker" enabled="false">
        <parameters>
          <parameter name="maxLineLength">
            <![CDATA[ 160 ]]>
          </parameter>
          <parameter name="tabSize">
            <![CDATA[ 4 ]]>
          </parameter>
        </parameters>
      </check>
      <check level="warning" class="org.scalastyle.scalariform.ClassNamesChecker" enabled="false">
        <parameters>
          <parameter name="regex">
            <![CDATA[^[A-Z][A-Za-z0-9]*$]]>
          </parameter>
        </parameters>
      </check>
      <check level="warning" class="org.scalastyle.scalariform.ObjectNamesChecker" enabled="false">
        <parameters>
          <parameter name="regex">
            <![CDATA[^[A-Z][A-Za-z0-9]*$]]>
          </parameter>
        </parameters>
      </check>
      <check level="warning" class="org.scalastyle.scalariform.PackageObjectNamesChecker" enabled="false">
        <parameters>
          <parameter name="regex">
            <![CDATA[^[a-z][A-Za-z0-9]*$]]>
          </parameter>
        </parameters>
      </check>
      <check level="warning" class="org.scalastyle.scalariform.EqualsHashCodeChecker" enabled="false"/>
      <check level="warning" class="org.scalastyle.scalariform.IllegalImportsChecker" enabled="false">
        <parameters>
          <parameter name="illegalImports">
            <![CDATA[ sun._,java.awt._ ]]>
          </parameter>
        </parameters>
      </check>
      <check level="warning" class="org.scalastyle.scalariform.ParameterNumberChecker" enabled="false">
        <parameters>
          <parameter name="maxParameters">
            <![CDATA[ 8 ]]>
          </parameter>
        </parameters>
      </check>
      <check level="warning" class="org.scalastyle.scalariform.MagicNumberChecker" enabled="false">
        <parameters>
          <parameter name="ignore">
            <![CDATA[ -1,0,1,2,3 ]]>
          </parameter>
        </parameters>
      </check>
      <check level="warning" class="org.scalastyle.scalariform.NoWhitespaceBeforeLeftBracketChecker" enabled="false"/>
      <check level="warning" class="org.scalastyle.scalariform.NoWhitespaceAfterLeftBracketChecker" enabled="false"/>
      <check level="warning" class="org.scalastyle.scalariform.ReturnChecker" enabled="false"/>
      <check level="warning" class="org.scalastyle.scalariform.NullChecker" enabled="false"/>
      <check level="warning" class="org.scalastyle.scalariform.NoCloneChecker" enabled="false"/>
      <check level="warning" class="org.scalastyle.scalariform.NoFinalizeChecker" enabled="false"/>
      <check level="warning" class="org.scalastyle.scalariform.CovariantEqualsChecker" enabled="false"/>
      <check level="warning" class="org.scalastyle.scalariform.StructuralTypeChecker" enabled="true"/>
      <check level="warning" class="org.scalastyle.file.RegexChecker" enabled="false">
        <parameters>
          <parameter name="regex">
            <![CDATA[ println ]]>
          </parameter>
        </parameters>
      </check>
      <check level="warning" class="org.scalastyle.scalariform.NumberOfTypesChecker" enabled="false">
        <parameters>
          <parameter name="maxTypes">
            <![CDATA[ 30 ]]>
          </parameter>
        </parameters>
      </check>
      <check level="warning" class="org.scalastyle.scalariform.CyclomaticComplexityChecker" enabled="false">
        <parameters>
          <parameter name="maximum">
            <![CDATA[ 10 ]]>
          </parameter>
        </parameters>
      </check>
      <check level="warning" class="org.scalastyle.scalariform.UppercaseLChecker" enabled="false"/>
      <check level="warning" class="org.scalastyle.scalariform.SimplifyBooleanExpressionChecker" enabled="false"/>
      <check level="warning" class="org.scalastyle.scalariform.IfBraceChecker" enabled="false">
        <parameters>
          <parameter name="singleLineAllowed">
            <![CDATA[ true ]]>
          </parameter>
          <parameter name="doubleLineAllowed">
            <![CDATA[ false ]]>
          </parameter>
        </parameters>
      </check>
      <check level="warning" class="org.scalastyle.scalariform.MethodLengthChecker" enabled="false">
        <parameters>
          <parameter name="maxLength">
            <![CDATA[ 50 ]]>
          </parameter>
        </parameters>
      </check>
      <check level="warning" class="org.scalastyle.scalariform.MethodNamesChecker" enabled="false">
        <parameters>
          <parameter name="regex">
            <![CDATA[^([a-z][A-Za-z0-9]*|<<|>>>|>|==|!=|<|<=|>|>=|\||&|\^|\+|-|\*|\/|%|\|\||&&|\+\+|--|\+=|-=)$]]>
          </parameter>
        </parameters>
      </check>
      <check level="warning" class="org.scalastyle.scalariform.NumberOfMethodsInTypeChecker" enabled="false">
        <parameters>
          <parameter name="maxMethods">
            <![CDATA[ 30 ]]>
          </parameter>
        </parameters>
      </check>
      <check level="warning" class="org.scalastyle.scalariform.PublicMethodsHaveTypeChecker" enabled="false"/>
      <check level="warning" class="org.scalastyle.file.NewLineAtEofChecker" enabled="false"/>
      <check level="warning" class="org.scalastyle.file.NoNewLineAtEofChecker" enabled="false"/>

      <check enabled="false" class="org.scalastyle.file.IndentationChecker" level="warning">
        <parameters>
          <parameter name="tabSize">2</parameter>
        </parameters>
      </check>
      <check enabled="false" class="org.scalastyle.scalariform.NonASCIICharacterChecker" level="warning"/>
      <check enabled="false" class="org.scalastyle.scalariform.FieldNamesChecker" level="warning">
        <parameters>
          <parameter name="regex">^[a-z][A-Za-z0-9]*$</parameter>
        </parameters>
      </check>
      <check enabled="false" class="org.scalastyle.scalariform.XmlLiteralChecker" level="warning"/>
      <check level="warning" class="org.scalastyle.scalariform.WhileChecker" enabled="false"/>
      <check level="warning" class="org.scalastyle.scalariform.VarFieldChecker" enabled="false"/>
      <check level="warning" class="org.scalastyle.scalariform.VarLocalChecker" enabled="false"/>
      <check level="warning" class="org.scalastyle.scalariform.RedundantIfChecker" enabled="false"/>
      <check level="warning" class="org.scalastyle.scalariform.TokenChecker" enabled="false">
        <parameters>
          <parameter name="regex">
            <![CDATA[ println ]]>
          </parameter>
        </parameters>
      </check>
      <check level="warning" class="org.scalastyle.scalariform.DeprecatedJavaChecker" enabled="false"/>
      <check level="warning" class="org.scalastyle.scalariform.EmptyClassChecker" enabled="false"/>
      <check level="warning" class="org.scalastyle.scalariform.ClassTypeParameterChecker" enabled="false">
        <parameters>
          <parameter name="regex">
            <![CDATA[ ^[A-Z_]$ ]]>
          </parameter>
        </parameters>
      </check>
      <check level="warning" class="org.scalastyle.scalariform.UnderscoreImportChecker" enabled="false"/>
      <check level="warning" class="org.scalastyle.scalariform.LowercasePatternMatchChecker" enabled="false"/>
      <check level="warning" class="org.scalastyle.scalariform.MultipleStringLiteralsChecker" enabled="false">
        <parameters>
          <parameter name="allowed">
            <![CDATA[ 1 ]]>
          </parameter>
          <parameter name="ignoreRegex">
            <![CDATA[ ^""$ ]]>
          </parameter>
        </parameters>
      </check>
      <check level="warning" class="org.scalastyle.scalariform.ImportGroupingChecker" enabled="false"/>
      <check level="warning" class="org.scalastyle.scalariform.ProcedureDeclarationChecker" enabled="false"></check>
      <check level="warning" class="org.scalastyle.scalariform.NotImplementedErrorUsage" enabled="false"></check>
      <check level="warning" class="org.scalastyle.scalariform.ForBraceChecker" enabled="false"></check>
      <check level="warning" class="org.scalastyle.scalariform.BlockImportChecker" enabled="false"></check>
    </scalastyle>


  private def getConfigFile(patterns: List[PatternDef]): File = {
    val rulesToApply = patterns.map(_.patternId.value)

    val customConfig = (scalaStyleConfig \ "check").map {
      check =>
        val level = (check \ "@level").text
        val clazz = (check \ "@class").text
        val patternName = clazz.split('.').last
        val enabled = rulesToApply.contains(patternName)

        val parameters = (check \ "parameters" \ "parameter").map {
          parameter =>
            val parameterName = (parameter \ "@name").text
            //Check if value.toString is good or if it is necessary the printfy or wtv cause of "" in ints/strings
            val parameterValue = patterns.find(_.patternId.value == patternName)
              .flatMap(_.parameters.flatMap(_.find(_.name.value == parameterName).map(_.value.toString)))
              .getOrElse(parameter.text.trim())

            s"""<parameters><parameter name="$parameterName"><![CDATA[$parameterValue]]></parameter></parameters>"""
        }

        s"""<check level="$level" class="$clazz" enabled="$enabled"> ${parameters.mkString} </check>""".stripMargin
    }

    val scalaStyleNewConfig = "<scalastyle>" + customConfig.mkString + "</scalastyle>"

    FileHelper.createTmpFile(scalaStyleNewConfig).toFile
  }


}

case class ScalaStyleParserException(message: String) extends Exception(message)
