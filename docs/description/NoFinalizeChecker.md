# No Finalize

`finalize()` is called when the object is garbage collected, and garbage collection is not guaranteed to happen. It is therefore unwise to rely on code in `finalize()` method.
