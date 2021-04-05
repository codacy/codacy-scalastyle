//#Patterns: EqualsHashCodeChecker

//#Warning:  EqualsHashCodeChecker
class EqualsHashCodeChecker {

  override def hashCode(): Int = 42

}

//#Warning:  EqualsHashCodeChecker
class AnotherEqualsHashCodeChecker {

  override def equals(x$1: Any): Boolean = true

}
