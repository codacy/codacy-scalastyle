//#Patterns: NonASCIICharacterChecker
class NonASCIICharacterChecker{

  "value" match {
    case "value" => println("matched")
  }

  "value" match {
    //#Warning: NonASCIICharacterChecker
    case "value" ⇒ println("matched")
  }

}
