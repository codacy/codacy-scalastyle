//#Patterns: NoFinalizeChecker

//#Err: NoFinalizeChecker
class NoFinalizeChecker {

  override def finalize(): Unit = {
    println("Don't do it.")
  }

}
