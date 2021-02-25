Usage of braces (rather than parentheses) within a for loop mean that you don’t have to specify a semi-colon at the end of every line:

    for {      // braces
      t <- List(1, 2, 3)
      if i % 2 == 0
    } yield t

is preferred to

    for (      // parentheses
      t <- List(1, 2, 3);
      if i % 2 == 0
    ) yield t
      