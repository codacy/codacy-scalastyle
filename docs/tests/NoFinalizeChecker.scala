//#Patterns: NoFinalizeChecker

//#Warning:  NoFinalizeChecker
class NoFinalizeChecker {

  override def finalize(): Unit = {
    println("Don't do it.")
  }

}
