# Space After Comment Start

To bring consistency with how comments should be formatted, leave a space right after the beginning of the comment.

```scala
package foobar

object Foobar {
  /**WRONG
    *
    */
  /** Correct
    *
    */
  val d = 2 /*Wrong*/ //Wrong
  /**
    * Correct
    */
  val e = 3 /** Correct*/ // Correct
}
```
