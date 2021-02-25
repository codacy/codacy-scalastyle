Return is implemented in bytecode as an exception catching/throwing pair which, used in hot code, has performance implications.
Additionally, the last statement in a method is automatically returned, thus the use of the `return` keyword is redundant.
      