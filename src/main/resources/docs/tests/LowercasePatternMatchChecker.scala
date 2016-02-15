//#Patterns: LowercasePatternMatchChecker

class LowercasePatternMatchChecker {

  val foo = "foo"
  val Bar = "bar"

  "bar" match {
    case Bar => "we got bar"
  } // result = "we got bar"

  "bar" match {
    //#Warn: LowercasePatternMatchChecker
    case foo => "we got foo"
  } // result = "we got foo"

  "bar" match {
    case `foo` => "we got foo"
  } // result = MatchError

}
