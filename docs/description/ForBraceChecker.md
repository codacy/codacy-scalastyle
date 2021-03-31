# For Brace

Usage of braces (rather than parentheses) within a `for` comprehension mean that you don't have to specify a semi-colon at the end of every line:

```scala
for { // braces
  t <- List(1,2,3)
  if (t % 2 == 0)
} yield t
```

is preferred to

```scala
for ( // parentheses
  t <- List(1,2,3);
  if (t % 2 == 0)
) yield t
```

To fix it, replace the `()` with `{}`. And then remove the `;` at the end of the lines.
