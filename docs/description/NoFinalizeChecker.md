In general it's best not to rely on finalize() to do any cleaning up.

According to the [Javadoc](http://docs.oracle.com/javase/7/docs/api/java/lang/Object.html#finalize%28%29):

    Called by the garbage collector on an object when garbage collection determines that there are no more references to the object.

This may never happen in the life of a program if the object is always accessible.
Also, the garbage collector is not guaranteed to run at any specific time.
      