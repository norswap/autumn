# FAQ: Why is the DSL syntax so ugly/verbose?

You may have noticed that the syntax if full of braces.
Where an EBNF grammar rule might look like:

    AssertStmt ::= Assert Expr (Colon Expr)? Semi
    
The Autumn equivalent will be:
    
    fun assert_stmt() = seq { assert() && expr() && maybe { seq { colon() && expr() } } && semi() } }
    
Quit a mouthful! But why?

Essentially because Autumn leverages Koltin's [inline functions] in order to eliminate
[megamorphic call sites] and get a performance boost.

By composing functions in the code rather than at the object level, we avoid creating a bunch
of call sites where the JVM only knows that we are calling a method of an interface, and that
interface has many implementations (one per combinator). The JVM is very bad at optimizing
these calls.

There are multiple issues:

- We need to pass function to combinators (defined as inline functions). Consider the following
  Autumn grammar:

      class MyGrammar: Grammar() {
          fun XYZ() = string("xyz")
          override fun root() = opt { XYZ() }
      }
    
  Why couldn't we have written `opt(::XYZ)` instead, using Kotlin's [method references]?
  `opt` expects a function of type `Grammar.() -> Boolean`, that is a function with a grammar
  as receiver returning a boolean. However, `::XYZ` is a bound reference (the receiver is bound
  to an instance of `MyGrammar`) and so has type `() -> Boolean`. Writing
  `Grammar::XYZ` does no good: the receiver of `XYZ` is a subtype of `Grammar`, so that expression
  isn't valid. Neither does `MyGrammar::XYZ` works: the combinator expects a `Grammar.() -> Boolean`,
  not a `MyGrammar.() -> Boolean`.
  
  
  
   [method references]: https://kotlinlang.org/docs/reference/lambdas.html

[inline functions]: https://kotlinlang.org/docs/reference/inline-functions.html
[megamorphic call sites]: /doc/autumn/megamorphic.md

TODO
- link Kotlin bug: can't inline receivers
- you don't care about a generation step? use the model instead