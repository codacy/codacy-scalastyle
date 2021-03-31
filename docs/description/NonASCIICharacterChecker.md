# Non ASCIICharacter

Scala allows unicode characters as operators and some editors misbehave when they see non-ascii character. In a project collaborated by a community of developers. This check can be helpful in such situations. 

```scala
"value" match {
  case "value" => println("matched")
  ...
}
```

is preferred to

```scala
"value" match {
  case "value" ⇒ println("matched")
  ...
}
```

To fix it, replace the (unicode operator) `⇒` with `=>`.
