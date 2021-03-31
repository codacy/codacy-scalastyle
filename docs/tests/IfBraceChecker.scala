//#Patterns: IfBraceChecker

class IfBraceChecker {

  def expr(i: Int) = {
    //#Warning: IfBraceChecker
    if (i < 0)
      println(i)

    //#Warning: IfBraceChecker
    if (i > 0) {
      i + 1
    }
    else
      i - 1
  }

}
