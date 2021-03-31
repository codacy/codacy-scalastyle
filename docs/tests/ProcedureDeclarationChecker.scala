//#Patterns: ProcedureDeclarationChecker

class ProcedureDeclarationChecker {

  //#Warning:  ProcedureDeclarationChecker
  def foo() {
    42
  }

  def betterFoo() = {
    42
  }

}

class Foo(s: String) {
  def this(){
    this("Bar")
  }
}
