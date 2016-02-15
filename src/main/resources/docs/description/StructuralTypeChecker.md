Scala has support for structural types — type requirements are expressed by interface structure instead of a concrete type.

    def foo(x: { def get: Int }) = 123 + x.get
    foo: (x: AnyRef{def get: Int})Int

    foo(new { def get = 10 })
    res0: Int = 133

This can be quite nice in many situations, but the implementation uses reflection, so it might take a toll on performance.

[Source](https://twitter.github.io/scala_school/advanced-types.html#structural)
      