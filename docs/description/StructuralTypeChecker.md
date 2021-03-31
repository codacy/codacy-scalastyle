# Structural Type

Structural types in Scala can use reflection - this can have unexpected performance consequences. Warning: This check can also wrongly pick up type lamdbas and other such constructs. This checker should be used with care. You always have the alternative of the scalac checking for structural types.
