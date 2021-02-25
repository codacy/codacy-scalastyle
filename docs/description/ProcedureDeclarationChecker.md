A procedure style declaration can cause confusion - the developer may have simply forgotten to add a `=`,
and now their method returns `Unit` rather than the inferred type:

    def foo() {
      bar()
      42
    }

You should use an explicit return type, or add a `=` before the body.
      