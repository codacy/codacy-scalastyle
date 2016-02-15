//#Patterns: NoCloneChecker

//#Err: NoCloneChecker
class NoCloneChecker {

  override def clone(): NoCloneChecker = {
    new NoCloneChecker()
  }

}
