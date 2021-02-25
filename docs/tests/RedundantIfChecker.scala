//#Patterns: RedundantIfChecker

class RedundantIfChecker {

  def method(i: Int) = {
    //#Info: RedundantIfChecker
    if (i > 0)
      false
    else
      true
    //#Info: RedundantIfChecker
    if (i > 0) {
      false
    } else {
      true
    }
  }

}
