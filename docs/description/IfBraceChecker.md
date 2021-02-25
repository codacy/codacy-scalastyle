Scala allows you to omit curly braces when the block consists of only one statement, for example:

    if (list.isDefined)
      list.shuffle()

However, in some circumstances, it can lead to bugs (you'd think that `shuffle()` is a part of the if while in reality it is not):

    if (list.isDefined)
      prepare()
      list.shuffle()
      