# Parsers

In Autumn, the `Parser` type is defined as

````kotlin
typealias Parser = () -> Boolean
````

If you're not familiar with function types in Kotlin, [read this page](https://kotlinlang.org/docs/reference/lambdas.html) before pursuing.

So a parser is any function returning a boolean, indicating
whether it succeeded.

That doesn't sound very useful. How does the parser know at which input
position it was invoked? How can it advance the input position or create
AST nodes? All that should be done through a reference to a `Grammar` object.

There are multiple techniques to get hold of a `Grammar` object. The most common is to use
a `Grammar` receiver, for instance by defining your parser in a subclass of `Grammar`:

````kotlin
class MyGrammar: Grammar()
{
    // ...
    
    /**
     * Matches the string "hello" at the current input position.
     * (Not something you would actually write -- you would use the built-in `string(String)` parser.)
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
````

`hello` can access all the fields of the instance of `MyGrammar` on which it is called.
However, `hello` doesn't have type `() -> Boolean`. It has type `MyGrammar.() -> Boolean`.

What's the matter? Many parser combinators expect `Parser` parameters. For instance, here is the
definition of the `opt` combinator:

````kotlin
inline fun Grammar.opt (crossinline p: Parser): Boolean
{
    p()
    return true
}
````

Here is how you can pass `hello` to `opt`:

````kotlin
class MyGrammar: Grammar()
{
    // ...
    
    fun opt_hello() = opt { hello() }
}
````

Deceptively simple, but there is a lot going on:

- `opt` is an extension function, so it expects a `Grammar` receiver. This is implicitly
  supplied by the receiver of `opt_hello`.

- Using the curly bracket (`{}`), we create an anonymous function whose type is `Parser`.
  
- Since `opt` is a function with a single function parameter, we can omit the parens when calling
  it with an anonymous function.
  
- Theoretically, the anonymous function is constructed each time `opt_hello` is called, and captures
  its `MyGrammar` receiver.
 
- However, `opt` is defined as `inline` and so the code desugars to:

````kotlin
class MyGrammar: Grammar()
{
    // ...
    
    fun opt_hello(): Boolean
    {
        hello()
        return true
    }
}
````

# TODO
- link function types
- link input position concept
- link grammar concept
- link extension fucntion