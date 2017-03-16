# Your First Grammar

As a means of introduction, this page will walk you through the definition of a simple grammar. This
grammar describes the syntax of a regular expressions subset (the one used in [this article]). This
grammar does not build an abstract syntax tree (this will come later), so it can only be used check
whether a string represents a valid regular expression.

[this article]: https://swtch.com/~rsc/regexp/regexp1.html

Here it is:

````kotlin
import norswap.autumn.Grammar
import norswap.autumn.parsers.*

class RegexGrammar: Grammar()
{
     fun meta_char()
        = char_set("|*+?()\\")

    fun regular_char()
        = seq { not { meta_char() } && char_any() }

    fun quoted_char()
        = seq { string("\\") && char_any() }

    fun character()
        = choice { quoted_char() || regular_char() }

    fun paren_group(): Boolean
        = seq { string("(") && alternation() && string(")") }

    fun atom()
        = choice { paren_group() || character() }

    fun repetition_char()
        = char_set("*+?")

    fun repetition()
        = seq { atom() && repeat0 { repetition_char() } }

    fun concatenation()
        = repeat1 { repetition() }

    fun alternation()
        = around1 ({ concatenation() } , { string("|") })

    override fun root() =
        alternation()
}
````

Let's quickly go through the grammar.

- A *meta character* is `|`,  `*`, `+`, `?`, `(`, `)` or `\ `.
- A *regular character* is any character that is not a meta character.
- A *quoted character* is any character preceded by `\ `.
- A *character* is either a quoted character or a regular character.
- A *paren group* is a parenthesized regular expression. This is a recursive definition because
  regular expressions can contain paren groups. 
- An *atom* is either a paren group or a character.
- A *repetition character* is `*`, `+` or `?`.
- A *repetition* is an atom followed by zero or more repetition characters.
- A *concatenation* is a sequence of one or more alternations.
- An *alternation* a sequence of one or more concatenations, separated by `|`.

Finally we must specify the root of the grammar. We want it to represent the syntax of regular
expressions, so we point it to `alternation`.

Let' try it out:

````kotlin
fun diagnose (grammar: Grammar, input: String)
{
    if (grammar.parse(input))
        println("success")
    else
        println("failure: " + grammar.failure?.invoke())
        
    grammar.reset()
}

fun main (args: Array<String>)
{
    val grammar = RegexGrammar()
    diagnose(grammar, "a(bb)+a|b(cc)*b") // success
    diagnose(grammar, "(xx") // failure: unexpected character
}
````

The second exemple lacks a closing paren. The generated error message ("unexpected character")
is distinctively unhelpful. We'll see later how to generate better error messages.