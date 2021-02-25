//#Patterns: VarLocalChecker

class VarLocalChecker {

  def method() = {
    //#Err: VarLocalChecker
    var a = 10
  }

}
