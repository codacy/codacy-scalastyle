//#Patterns: ProcedureDeclarationChecker

class ProcedureDeclarationChecker {

  //#Err: ProcedureDeclarationChecker
  def foo() {
    42
  }

  def betterFoo() = {
    42
  }

}
