# FAQ: Why is the DSL syntax so ugly/verbose?

You may have noticed that the syntax if full of braces.
Where an EBNF grammar rule might look like:

    AssertStmt ::= Assert Expr (Colon Expr)? Semi
    
The Autumn equivalent will be:
    
    fun assert_stmt() = seq { assert() && expr() && maybe { seq { colon() && expr() } } && semi() } }
    
Quit a mouthful! But why?

Essentially because Autumn leverages Koltin's [inline functions] in order to eliminate
[megamorphic call sites] and get a performance boost.

[inline functions]: https://kotlinlang.org/docs/reference/inline-functions.html
[megamorphic call sites]: /doc/autumn/megamorphic.md

By composing functions in the code rather than at the object level, we avoid creating a bunch
of call sites where the JVM only knows that we are calling a method of an interface, and that
interface has many implementations (one per combinator). The JVM is very bad at optimizing
these calls.

## Inlining Syntax Costs

The unfortunate downside is that passing functions around in a way compatible with inlining is
verbose. Consider the following Autumn grammar:

    class MyGrammar: Grammar() {
        fun XYZ() = string("xyz")
        override fun root() = opt { XYZ() }
    }
      
We could have used a [method reference] and written `opt(this::XYZ)` instead,
but the notation isn't obviously better.
We could shorten if further to `opt(s::XYZ)` if we define `val s = this`. 

Even better would be the ability to write `s::XYZ.opt`. Unfortunately, receiver functions are
not being inlined by Kotlin. There is an [open issue] about this.

[method reference]: https://kotlinlang.org/docs/reference/lambdas.html
[open issue]: https://youtrack.jetbrains.com/oauth?state=%2Fissue%2FKT-5837

## Why not use an External DSL?

I could define a custom grammar language as an external DSL, like many tools do.
The problem is that this does not come with an IDE that enables seamless jump to definition
and refactoring. I'd rather bear with some verbosity than forego tool support.

As to implementing tool support myself, the idea currently fails the cost-benefits analysis check.

## [Experimental] Using a Model

A trade-off between the verbose inline DSL and the external DSL is to use an
internal DSL based on objects, and use that to generate the inline DSL.

This is available through the [model] experimental feature. A model is an object graph
that represents a grammar as a graph of objects. One can then compile this model to a source
file that contains a class implementing `Grammar`.

Here is how our example from before would look using the model syntax:

    val assert_stmt = assert .. expr .. (colon .. expr).opt .. semi
    
There is a pitfall however: custom code in the grammar must be represented as strings, since
without parsing the source separately, we can't acquire the source of a method at run-time. The
problem is similarly to that of external DSLs: we lose all the IDEs niceties. Note that
"custom code" includes all the AST-building directives. So this is a serious issue.
    
Besides the syntax, the model approach has a few benefits. It could be used for grammar
transformations and analyses, but I don't do any of that currently. I'm not sure the
feature really pays for its own complexity, and that's why it is confined to experimental status
for now.

[model]: /doc/autumn/model.md