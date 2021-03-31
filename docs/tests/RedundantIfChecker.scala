//#Patterns: RedundantIfChecker

class RedundantIfChecker {

  def method(i: Int) = {
    //#Warning: RedundantIfChecker
    if (i > 0)
      false
    else
      true
    //#Warning: RedundantIfChecker
    if (i > 0) {
      false
    } else {
      true
    }
  }

}
