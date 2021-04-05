//#Patterns: MethodNamesChecker: {"regex": "^[A-Z][A-Za-z0-9]*$"}

class MethodNamesChecker {

  //#Warning: MethodNamesChecker
  def addOne(i: Int) = i + 1

  def AddOne(i: Int) = i + 1

  //#Warning: MethodNamesChecker
  def * = "stars are beautiful"

}
