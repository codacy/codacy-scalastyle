The `clone` method relies on strange/hard to follow rules that do not work in all situations.
Consequently, it is difficult to override correctly.
Below are some of the rules/reasons why the clone method should be avoided.

Classes supporting the `clone` method should implement the `Cloneable` interface but the `Cloneable` interface does not include the `clone` method.
As a result, it doesn't enforce the method override.
The `Cloneable` interface forces the `Object`'s `clone` method to work correctly.
Without implementing it, the `Object`'s `clone` method will throw a `CloneNotSupportedException`.
Non-final classes must return the object returned from a call to `super.clone()`.
Final classes can use a constructor to create a clone which is different from non-final classes.
If a super class implements the clone method incorrectly all subclasses calling `super.clone()` are doomed to failure.
If a class has references to mutable objects then those object references must be replaced with copies in the `clone` method after calling `super.clone()`.
The clone method does not work correctly with final mutable object references because final references cannot be reassigned.
If a super class overrides the clone method then all subclasses must provide a correct clone implementation.
Two alternatives to the `clone` method, in some cases, is a copy constructor or a static factory method to return copies of an object.
Both of these approaches are simpler and do not conflict with final fields.
They do not force the calling client to handle a `CloneNotSupportedException`.
They also are typed therefore no casting is necessary.
Finally, they are more flexible since they can take interface types rather than concrete classes.

Sometimes a copy constructor or static factory is not an acceptable alternative to the `clone` method.
The example below highlights the limitation of a copy constructor (or static factory).
Assume `Square` is a subclass for `Shape`.

    val s1 = new Square()
    println(s1.isInstanceOf[Square]) //true

Assume at this point that the code knows nothing of s1 being a `Square`.
That's the beauty of polymorphism but the code wants to copy the `Square` which is declared as a `Shape` - its super type.

    val s2 = new Shape(s1) //using the copy constructor
    println(s2,isInstanceOf[Square]) //false

The working solution - without knowing about all subclasses and doing many casts - is to do the following:

    val s2 = s1.clone()
    println(s2.isInstanceOf[Square]) //true

[Source](http://checkstyle.sourceforge.net/config_coding.html#NoClone)
      