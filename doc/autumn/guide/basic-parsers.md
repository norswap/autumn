# Using Basic Parsers

In [Your First Grammar](first-grammar.md) we saw how to define a simple grammar to recognize the
syntax of regular expressions. This works by means of mysterious functions we call *parsers*.

In this page, we look at a few of the basic parsers supplied by Autumn, and how you can create
your own parsers by assembling these pre-defined parsers.

This page is not intended as a reference, see the [Bundled Parsers Reference] instead.

[Bundled Parsers Reference]: ../API/parsers/README.md

## Parsers, Combinators and Input Position

- A parser is any function that returns a boolean, indicating whether the parser succeeded or
  failed.

- We say a parser *matches* or *succeeds* if it is invoked successfully (returns true) and that it
  *fails* otherwise.

- The grammar encapsulates the input text, which it receives when `parse` is called:

````
val grammar = RegexGrammar()
val success = grammar.parse("a(bb)+a|b(cc)*b") // boolean
````

- The grammar also maintains the *input position*, initially 0. Parsers can read and write the input
  position.

- Most parser match or fail depending on the text directly after the current input position.

- Some parsers take other parsers as input, which they invoke as part of their own invocation.
  We call these parsers *parser combinators* and the parser they take as parameter their *sub-parsers*.

- We sometimes say a parser *matches* or *consumes* **\<thing\>**, where **\<thing\>** is anything that can refer
  to part of the input text right after the input position. This means that if the parser finds
  the thing right after the input position, it succeeds and advances the input position past that
  part of the input; otherwise it fails. Here are a few examples:

    - *parser X matches the string 'hello'*
    - *parser Y matches an alphanumeric character*
    - *parser Z matches its sub-parser*

  In that last example, we really mean that Z has a sub-parser, invokes 
  it and matches the same input it does.
  
- **Important**: Per the [Transactionality Rule], ifa parser fails, it must be as though it never
modified the parse state. The input position, AST, etc should be unchanged.

[Transactionality Rule]: transactionality.md

## Matching Characters and Strings

- `string("hello")` matches `hello`
- `char_any()` matches any character
- `char_set("abc")` matches `a`, `b` or `c`
- `char_range('a', 'z')` matches characters from `a` through `z`
- `char_pred { '0' <= it && it <= '9' && (it - '0') % 2 == 1 }` matches odd digits (1, 3, 5, 7, 9)
- `alphanum()` matches any alphanumeric character

And more, including the ability to match decimal, octal and hexadecimal digits, alphabetic
characters.

Reference: [Matching Characters]

[Matching Characters]: ../API/parsers/chars.md

## Sequencing: Matching Things One After the Other (+ Optionality)

- `seq { string("hello") && string("world") }` matches the string `helloworld`.

  [Why does it use `&&`?](../faq/seq-choice-syntax.md)
  
- `opt { string("hello") }` succeeds matching `hello` or succeeds matching no input.

- `repeat0 { string("a") }` matches 0 or more `a`s.

- `repeat1 { string("a") }` matches 1 or more `a`s.

- `repeat(3) { string("a") }` matches the string `aaa`.

- `around0 ({ string("sheep") } , { string(",") })` matches 0 more instances of the string `sheep`,
   separated by commas (e.g. ` ` (empty) or `sheep,sheep`).

- `around1 ({ string("sheep")} , { string(",") })` matches 1 or more instances of the string `sheep`,
   separated by commas.

All repetition combinators, and more generally all parsers match **greedily**. This means they
will always match as much input as possible. This means that for instance,
`seq { repeat0 { string("a") } && string("a") }` can **never** succeed: the `repeat0` parser
consumes all `a`s, and there is none left for the second sub-parser.

Reference: [Matching Sequences and Optionals]

[Matching Sequences and Optionals]: ../API/parsers/sequential.md

## Choice: Matching One of Multiple Possible Things

- `choice { string("hello") || string("howdy") }`
   matches the string `hello` or the string `howdy`.
   
   `choice` tries its sub-parsers in left-to-right order: this means the choice will match the same
   thing as its firs successful sub-parser. This can be counter-intuitive: the parser `choice {
   string("a") || string("ab") }` will **never** match `ab`: `string("a")` will always also match, and
   the choice will look no further, matching only `a`. This is known as **[ordered choice]**
   (or prioritized choice).
   
   [ordered choice]: ../notes/peg.md#ordered-choice
   
   You can really use any parser you want in the braces, but to enact a choice they have to be
   separated by `||` (learn why we use this syntax [here](faq/seq-choice-syntax.md)).
   
- Sometimes, ordered choice is not the right decision. In the next section, we'll learn about
  the `Longest` parser which allows selecting the sub-parser that performs the longest match
  on the input.
  
Reference: [Choices]

[Choices]: ../API/parsers/choices.md
  
## Lookahead

- `ahead { string("cow") }` succeeds if the string `cow` matches, but does not change the input
   position (note that all other side-effects of the sub-parsers are preserved, if that's not desired,
   use `ahead_pure` instead).

- `not { string("cow") }` succeds only if the string `cow` **does not** match. All state remains
   unchanged.
   
Reference: [Lookahead]

[Lookahead]: ../API/parsers/lookahead.md

## More

- `word("frog")` matches the string `frog`, as well as any trailing whitespace. The definition 
   of whitespace is flexible in Autumn, see below.

- `parens { string("frog") }` matches the string `(frog)`. In reality it also matches whitespace
   after `(` and `)`, so under the default whitespace interpretation, it would match `( hello) `
   (but not `( hello )` — you'd need `parens { word("frog") }` for that).
   
- `angles`, `squares` and `curlies` are analogous to `parens`, but for `<>`, `[]` and `{}`
   respectively.
   
- `comma_list0 { string("donkey") }` matches 0 or more repetition of the string `donkey` separated
  by commas. There is a variant that only accepts one or more repetition (`comma_list1`) and variants
  that allow optional trailing commas (`comma_list_term0` and `comma_list_term1`).
   
Reference: [Bundled Parsers Reference]
[Bundled Parsers Reference]: ../API/parsers/README.md
   
## Handling Whitespace

As mentionned above, the `word` parser skips whitespace after the string it matches.
There are also a few other parsers that deal with whitespace.

What consistute whitespace is defined by the [`whitespace`] parser defined the in the [Grammar]
class. This parser can be overriden by your grammar to customize the definition of whitespace. The
default definition considers that whitespace is any number of characters recognized as whitespace by
Java's [Characters#isWhitespace].

[Characters#isWhitespace]: https://docs.oracle.com/javase/8/docs/api/java/lang/Character.html#isWhitespace-char-
[`whitespace`]: ../grammars.md#whitespace
[Grammar]: ../grammars.md