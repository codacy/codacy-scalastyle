//#Patterns: ForBraceChecker

class ForBraceChecker {

  def method(i: Int) = {
    //#Warning: ForBraceChecker
    for (
      t <- List(1, 2, 3)
      if i % 2 == 0
    ) yield t
  }

}
