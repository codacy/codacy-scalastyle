    "value".match {
      case "value" => println("matched")
        ...
    }

is preferred to

    "value".match {
      case "value" ⇒ println("matched")
        ...
    }

To fix it, replace the (unicode operator)⇒ with =>.