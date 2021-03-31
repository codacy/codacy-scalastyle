//#Patterns: MethodNamesChecker

class MethodNamesChecker {

  def addOne(i: Int) = i + 1

  //#Warning: MethodNamesChecker
  def AddOne(i: Int) = i + 1
  
  //#Warning: MethodNamesChecker
  def * = "stars are beautiful"

}
