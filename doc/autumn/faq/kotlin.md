# FAQ: Why write Autumn in Kotlin?

Multiple reasons. At first, it was just because Kotlin is a better (mostly: less verbose) Java.
Since it is fully compatible with Java, the plan was to provide a Java-compatible API.

After writing the second version of Autumn howveer, I realized that I could leverage Kotlin's
[inline function] to avoid [megamorphic call sites] and get a big performance boost.

[inline function]: https://kotlinlang.org/docs/reference/inline-functions.html
[megamorphic call sites]: /doc/autumn/megamorphic.md

Writing Autumn in Kotlin also enables us to have a nice Domain Specific Language (DSL), which
uses operator overloading, nice higher-order function syntax,
and extension functions.

---

See also:

- [Is Autumn compatible with Java?](java-compat.md)
- [Is Autumn compatible with Kotlin's Javascript backend?](js-compat.md)