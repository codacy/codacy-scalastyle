//#Patterns: CyclomaticComplexityChecker

class CyclomaticComplexityChecker {
  //#Warn: CyclomaticComplexityChecker
  def name(delta: Int): Boolean = {
    val duration = +7 - 8
    val isPastHalf = duration < 250 && Math.abs(delta) > 20 || Math.abs(delta) > 780 / 2
    val direction = delta < 0

    if (duration > 0) {
      if (isPastHalf) {
        if (direction) {
          true
        } else {
          if (duration / 2 == 0 && isPastHalf) {
            return false
          }
          true
        }
      } else {
        if (direction || duration > 45) {
          if (delta * 3 > 7 || delta < 0) {
            return false
          }
          return true
        }
        false
      }
    } else {
      false
    }
  }

}
