//#Patterns: RedundantIfChecker

class RedundantIfChecker {

  def method(i: Int) = {
    //#Info: RedundantIfChecker
    if (i > 0)
      false
    else
      true

    if (i > 0) {
      false
    } else {
      true
    }
  }

}
