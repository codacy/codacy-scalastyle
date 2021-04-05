# Procedure Declaration

A procedure style declaration can cause confusion - the developer may have simply forgotten to add a `=`, and now their method returns `Unit` rather than the inferred type:

```scala
def foo() { println("hello"); 5 }
```
This checker raises a warning with the first line. To fix it, use an explicit return type, or add a `=` before the body.

```scala
def foo() = { println("hello"); 5 }
```
