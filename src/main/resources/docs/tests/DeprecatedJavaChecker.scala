//#Patterns: DeprecatedJavaChecker

class DeprecatedJavaChecker {

  //#Info: DeprecatedJavaChecker
  @Deprecated
  def method(i: Int) = {
    i == 10
  }

  @deprecated("this method will be removed", "v2.0")
  def otherMethod(i: Int) = {
    i >= 10
  }

}
