# Writing Your Own Parsers

On the [last page], we saw that parsers are functions returning a boolean that indicates success.
In fact, the parser type is defined as:

```kotlin
typealias Parser = () -> Boolean
```

On the previous page, we also hinted that parsers need to access the grammar. However, the
[Grammar] type doesn't appear in the type alias.

This page explain how to define your own parsers and give them access to a [Grammar] object.

[last page]: 4-own-parsers.md
[Grammar]: ../API/grammar.md

## Inside a Grammar

The easiest way is simply to define a parser inside a grammar.

```kotlin
class MyGrammar: Grammar() {
    // ...
    /**
     * Matches the string "hello" at the current input position.
     * (Not something you would actually write — you would use the built-in `string(String)` parser.)
     */
    fun hello(): Boolean
    {
        val str = "hello"
        val success = text.regionMatches(pos, str, 0, str.length)
        if (success) pos += str.length
        else fail(pos, NoString(str))
        return success
    }
}
```

This defines a function returning a boolean (a parser!), which has access to an instance of
`Grammar` through its receiver (`this`).

Note that the type of `hello` is `Grammar.() -> Boolean`, which is not quite the same as `() ->
Boolean`. If you pass `hello` using the curly bracket syntax inside `MyGrammar`, the receiver
will be implicitly supplied:

```kotlin
class MyGrammar: Grammar() {
    // ...
    fun hello_opt() = opt { hello() }
}
```

Here `hello()` is implicitly `this.hello()`: the parser captures the receiver of `hello_opt`.

In the rare scenario where you would need to call `hello` in a context where there is not implicit
grammar receiver, you must pass it explicitly: e.g. `opt { grammar.hello() }`

## Advancing Input and Reporting Failures

Before talking about the other ways to define parsers, I want to zoom in on some details of the
previous example.

Advancing the input position is very simple: just increment the [`Grammar#pos`] field.
However, you should never decrement `pos` below the value it had when the parser was invoked!

[`Grammar#pos`]: ../API/grammar.md#pos

For a parser to fail, it is only necessary for it to return `false` (remember it still needs
to [revert all side-effects][transact]). However, if you want the parser to be able to report
that failure, you need to associate it a message with [`fail`].

[transact]: 2-transactionality.md#the-transactionality-rule
[`fail`]: ../API/grammar.md#fail

A word about error reporting: if the whole parse fails, Autumn will report the furthest failure
encountered during the parse that has an associated message. We discuss error reporting in more
details in the [Error Reporting] section.

[Error Reporting]: error-reporting.md

## As An Extension Function

Alternatively, you can define a parser as an [extension function]. For instance, here is how the
built-in `char_pred` parser is defined:

[extension function]: https://kotlinlang.org/docs/reference/extensions.html

```kotlin
inline fun Grammar.char_pred (pred: (Char) -> Boolean): Boolean
{
    val success = pred(text[pos])
    if (success) ++pos
    else fail(pos, UnexpectedChar)
    return success
}
```

`char_pred` matches a character if it satisfies the given predicate. It uses the grammar
to acces the position (`pos`) and to indicate the cause of failure (`fail`).

Extension functions are useful to define functions that do not logically belong to any one
grammar, but still need access to the grammar object.

## As Classes

Sometimes it makes sense to define a parser as a class extending `Parser`.
For instance, here are the signatures for the built-in [`Longest`] parser:

[`Longest`]: ../API/parsers/choice.md#Longest

```kotlin
class Longest (val g: Grammar, val ps: Array<Parser>): Parser
{
    override fun invoke(): Boolean
    {
        // ...
    }
}
```

Why is `Longest` a class rather than a function? Because we don't want to create an array of parsers
(a relatively expensive operation) each time we invoke the parser. Instead, the array is created once
at construction time, and reused each time. The grammar instance has to be supplied explicitly.

Here's an exemple of how `Longest` can be used inside a grammar:

```kotlin
class MyGrammar: Grammar()
{
    fun keyword()
        = choice { string("class") || string("val") || string("var") }
        
    val token
        = Longest(this, arrayOf({ keyword() }, { java_identifier() }))
        
    fun tokens()
        = repeat0 { token() }
    
    // ...
}
```

Notice that `token` is a `val` not a `fun`: it's an instance of `Parser` rather than a method.
We can still call it with `token()` which is syntactic sugar for `token.invoke()`.

In truth, there is some syntactic sugar for `Longest`, we could have written this instead:

```kotlin
val token = longest ({ keyword() }, { java_identifier() })
```

But this does exactly the same thing under the wraps. Notice we still have `val` and not `fun`!

## Parser Combinators

Parser combinators are parsers that take other parsers as parameter. `Longest` from above is
a parser combinator. However, because it is also a class, it's a bit of a weird one.
Here is something more common:

```kotlin
inline fun Grammar.opt (crossinline p: Parser): Boolean
{
    p()
    return true
}
```

The [`opt`] parser matches the same things as its sub-parser `p` if it succeeds, otherwise it
succeeds matching nothing.

[`opt`]: ../API/parsers/sequential.md#opt

First note that the `Grammar` receiver is never used here. I simply adopted the convention
to always make combinators extension function, because (1) it helps future-proofing the API and (2)
reduces the probability of namespace collisions.

Something very important is that parser combinators defined as functions should generally be marked
`inline`. This enables a capital optimization. If you write:

```kotlin
fun opt_hello() = opt { hello() }
```

Inlining will desugar this definition to:

```kotlin
fun opt_hello(): Boolean {
    hello()
    return true
}
```

If you omit `inline`, a new instance of `Parser` will be created to wrap the call to `hello()` and
that instance will be passed to `opt`. Creating the instance is an additional and unecessary cost,
and this would also create a [megamorphic call site], another big performance penalty on the JVM.

[megamorphic call site]: ../notes/megamorphic.md

Finally, `Parser` parameters should be marked as `crossinline`. This prevents them from containing
non-local returns. For instance, without `crossinline` you could write:

```kotlin
fun opt_something() = opt { return false }
```

Hence breaking the semantics of `opt`. This violation is harmless, but in many cases, non-local
returns can bypass capital clean-up code.

Parsers that aren't combinators should not be marked `inline`. The JVM is smart enough to inline
them if required, as long as there are no megamorphic call sites in the way.

**Reminder**

I picked an extremely simple example for this section, but remember that in general you need
to [revert all side-effects][transact] when a parser fails, including the side-effects incurred
by successful sub-parsers. In general, the simplest way to achieve this is to use [`transact`].

[`transact`]: ../API/misc.md#transact