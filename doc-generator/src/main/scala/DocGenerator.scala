import better.files._
import better.files.Dsl._
import com.codacy.plugins.api._
import com.codacy.plugins.api.results._
import play.api.libs.json.Json
import scala.xml._

object DocGenerator extends App {
  val scalastyleDefinition = XML.load(Resource.asStream("scalastyle_definition.xml").get)
  val scalastyleDocumentation = XML.load(Resource.asStream("scalastyle_documentation.xml").get)

  case class Checker(id: String, defaultLevel: String, cls: String, parameters: Seq[Checker.Parameter]) {
    def patternId: String = cls.split('.').last

    // Split camel case patternId into separate words
    def title: String = patternId.stripSuffix("Checker").replaceAll("([a-z](?=[A-Z]))", "$1 ")

    def category: Pattern.Category = patternId match {
      case "VarFieldChecker" | "VarLocalChecker" | "ProcedureDeclarationChecker" | "NotImplementedErrorUsage" |
          "EqualsHashCodeChecker" | "NullChecker" | "NoCloneChecker" | "NoFinalizeChecker" | "CovariantEqualsChecker" |
          "StructuralTypeChecker" | "PublicMethodsHaveTypeChecker" =>
        Pattern.Category.ErrorProne
      case _ => Pattern.Category.CodeStyle
    }

    def level: Result.Level = defaultLevel match {
      case "warning" => Result.Level.Warn
      case "error" => Result.Level.Err
      case _ => Result.Level.Info
    }
  }

  object Checker {
    case class Parameter(name: String, default: String)
  }

  val checkers = scalastyleDefinition
    .\("checker")
    .map(
      n =>
        Checker(
          id = n \@ "id",
          defaultLevel = n \@ "defaultLevel",
          cls = n \@ "class",
          parameters = n.\("parameters").\("parameter").map { p =>
            Checker.Parameter(name = p \@ "name", default = p \@ "default")
          }
      )
    )

  val justifications: Map[String, String] = {
    scalastyleDocumentation.\("check").map(c => (c.\@("id"), c.\("justification").text.trim)).toMap
  }

  val docsDirectory = pwd / "docs"
  val descriptionDirectory = docsDirectory / "description"

  val patternDescriptions = checkers.map { checker =>
    Pattern.Description(
      Pattern.Id(checker.patternId),
      Pattern.Title(checker.title),
      None,
      None,
      parameters = checker.parameters
        .map(p => Parameter.Description(name = Parameter.Name(p.name), description = Parameter.DescriptionText(p.name)))
        .toSet
    )
  }

  val patternSpecifications = checkers.map { checker =>
    val parameters = checker.parameters.map { p =>
      Parameter.Specification(Parameter.Name(p.name), Parameter.Value(p.default))
    }.toSet
    Pattern.Specification(
      Pattern.Id(checker.patternId),
      checker.level,
      checker.category,
      subcategory = None,
      enabled = true,
      parameters = parameters
    )
  }

  val specification =
    Tool.Specification(Tool.Name("scalastyle"), Some(Tool.Version(Versions.scalastyle)), patternSpecifications.toSet)

  (pwd / "docs" / "patterns.json").write(Json.prettyPrint(Json.toJson(specification)) + "\n")
  descriptionDirectory.createDirectoryIfNotExists(createParents = true)
  checkers.foreach { checker =>
    val content = s"""# ${checker.title}
                     |
                     |${justifications(checker.id)}
                     |""".stripMargin
    (descriptionDirectory / s"${checker.patternId}.md").writeText(content)
  }
  (descriptionDirectory / "description.json").overwrite(Json.prettyPrint(Json.toJson(patternDescriptions)) + "\n")
}
