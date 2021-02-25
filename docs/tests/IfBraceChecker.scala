//#Patterns: IfBraceChecker

class IfBraceChecker {

  def expr(i: Int) = {
    //#Info: IfBraceChecker
    if (i < 0)
      println(i)

    //#Info: IfBraceChecker
    if (i > 0) {
      i + 1
    }
    else
      i - 1
  }

}
