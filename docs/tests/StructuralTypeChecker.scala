//#Patterns: StructuralTypeChecker

class StructuralTypeChecker {

  class Foo {

    def doFoo() = {}

  }

  def regularMethod(foo: Foo) {
    foo.doFoo()
  }

  //#Warn: StructuralTypeChecker
  def structuralMethod(foo: {def doFoo(): Unit}) {
    foo.doFoo()
  }

}

abstract class Foo {

  //#Warn: StructuralTypeChecker
  type BarType = AnyRef {def bar: String}

  def bar1: String

  //#Warn: StructuralTypeChecker
  def t(ff: {type Foo}) = ff

}

