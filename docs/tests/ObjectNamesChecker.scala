//#Patterns: ObjectNamesChecker
package analysis.samples.scala.scalastyle

object Foobar {
  val foo = 1
}

//#Warning: ObjectNamesChecker
object foobaz {

  //#Warning: ObjectNamesChecker
  object barbar {
  }

}

package object foobarz {

  //#Warning: ObjectNamesChecker
  object barbar {
  }

}
