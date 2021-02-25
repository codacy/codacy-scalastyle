There should be no whitespace before the type parameter:

    def result(i: Int): Option [Int] = {
      Some(i)
    }

This should be:

    def result(i: Int): Option[Int] = {
      Some(i)
    }

[Source](http://docs.scala-lang.org/style/naming-conventions.html#type_parameters_generics)
      