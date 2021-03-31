//#Patterns: NoCloneChecker

//#Warning:  NoCloneChecker
class NoCloneChecker {

  override def clone(): NoCloneChecker = {
    new NoCloneChecker()
  }

}
