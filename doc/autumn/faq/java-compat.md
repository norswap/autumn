# FAQ: Is Autumn compatible with Java?

**Short answer: writing grammars in Java is unsupported, but nothing prevents you from calling
a Kotlin-defined parser from Java.**

It is theoretically possible to define Autumn grammars in Java, although I haven't tested it.
However, doing so would have two major disadvantages:

- It precludes the possibility of using [inline functions] to get improved performance,
  as this is a feature built in the Kotlin compiler.

[inline functions]: https://kotlinlang.org/docs/reference/inline-functions.html

- Java cannot use the Domain Specific Language (DSL) we supply to define grammars, since it relies
  or operator overloading, nice higher-order function syntax, and extension functions, all of whom
  Java does not have.
  
However, it would be nice to have a way to construct [grammar models] in Java, probably
as a [fluent interface]. Contributions welcomes!

[grammar models]: why-ugly.md#experimental-using-a-model
[fluent interface]: https://en.wikipedia.org/wiki/Fluent_interface

---

See also:

- [Why write Autumn in Kotlin?](kotlin.md)
- [Is Autumn compatible with Kotlin's Javascript backend?](js-compat.md)