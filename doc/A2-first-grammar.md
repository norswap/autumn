# A2. Your First Grammar

As an introduction to Autumn, let's write a simple but non-trivial grammar and walk through it
together. At first, we'll only define a recognizer, but then we'll extend it to produce an abstract
syntax tree (AST) in [A5. Creating an AST].

[A5. Creating an AST]: A5-creating-an-ast.md

The language for which we'll write a grammar is JSON (Javascript Object Notation), according to 
the specification at https://www.json.org/.

First, here is the grammar using (a kind of) EBNF notation ([*1]). Don't worry if you don't know
what that is, the point is just to make it easy on the eyes as we get started.

```
Integer      ::= 0 | [0-9]+
Fractional   ::= '.' [0-9]+
Exponent     ::= [eE] [+-]? Integer
Number       ::= '-'? Integer Fractional? Exponent?
HexDigit     ::= [0-9] | [a-f] | [A-F]
StringChar   ::= !["\] ![\u0000-\u001F] . | \ [\/bfnrt] | "\u" HexDigit HexDigit HexDigit HexDigit
String       ::= '"' StringChar* '"'
Value        ::= String | Number | Object | Array | "true" | "false" | "null"
Pair         ::= String ':' Value
Object       ::= '{' (Pair (',' Pair)*)? '}'
Array        ::= '[' (Value (',' Value)*)? ']'
Document     ::= Value         
```

## JSON in English

Let's walk through the grammar real fast by reformulating it in English.

- An integer is '0' or a sequence of one or more digits that doesn't start with '0'.
  (It may seem as though the rule allows for a sequence of digits starting with '0', but it actually
  doesn't because choice is *ordered*: if the first digit was zero, then only 0 would be matched.
  This will be explained in [A3. How Autumn Works] (sub-section "Vertical Backtracking")).
  
- The fractional part of a number is a dot followed by a string of digits.
  
- The exponent part of a number is 'e' or 'E' optionally followed by '+' or '-', followed by an
  integer.
  
- A number optionally starts with '-', then has an integer, then an optional fractional part and an
  optional exponent part.
  
- A hexadecimal digit is a letter between '0' and '9'  or between 'a' and 'f', or between 'A' and
  'F'.
  
 - A string character is any unicode character, but not a double quote (\"), a slash (\\), nor
   anything in the range `[\u0000-\u001F]` (which are control characters). Alternatively it can also
   be '\\' followed by either '\\', '/', 'b', 'f', 'n', 'r' or 't' (named character escapes), or the
   characters '\\' then 'u' then four hexadecimal digits.
   
- A string is zero or more string characters enclosed between double quotes (").

- A value is a string, a number, an object (see below), an array (see below), or the words "true",
  "false" or "null".
  
- A pair is a string followed by a colon, followed by a value.

- An object is a (possibly empty) sequence of pairs, separated by commas and enclosed between curly
  brackets ({}).

- An array is a (possibly empty) sequence of values, separated by commas and enclosed between square
  brackets ([]).
  
- A JSON document is comprised of a single JSON value.

However, this does not *fully* specify the grammar either. We still need to specify where whitespace
is allowed. In JSON, whitespace is allowed after all brackets, commas, colons and values.

Whitespace is comprised of spaces, tabs, newlines (\n) and carriage returns (\r).

[A3. How Autumn Works]: A3-how-autumn-works.md#vertical-backtracking

## The Autumn Grammar for JSON

With that in mind, let's see our JSON grammar.

```java
import norswap.autumn.Autumn;
import norswap.autumn.Grammar;
import norswap.autumn.ParseOptions;
import norswap.autumn.ParseResult;
import norswap.autumn.positions.LineMapString;

public final class JSON extends Grammar
{
    // Lexical
    
    { ws = usual_whitespace; }

    public rule integer =
        choice('0', digit.at_least(1));

    public rule fractional =
        seq('.', digit.at_least(1));

    public rule exponent =
        seq(set("eE"), set("+-").opt(), integer);

    public rule number =
        seq(opt('-'), integer, fractional.opt(), exponent.opt()).word();

    public rule string_char = choice(
        seq(set('"', '\\').not(), range('\u0000', '\u001F').not(), any),
        seq('\\', set("\\/bfnrt")),
        seq(str("\\u"), hex_digit, hex_digit, hex_digit, hex_digit));

    public rule string =
        seq('"', string_char.at_least(0), '"').word();

    public rule LBRACE   = word("{");
    public rule RBRACE   = word("}");
    public rule LBRACKET = word("[");
    public rule RBRACKET = word("]");
    public rule LPAREN   = word("(");
    public rule RPAREN   = word(")");
    public rule COLON    = word(":");
    public rule COMMA    = word(",");

    // Syntactic

    public rule value = lazy(() -> choice(
        string,
        number,
        this.object,
        this.array,
        word("true")  .as_val(true),
        word("false") .as_val(false),
        word("null")  .as_val(null)));

    public rule pair =
        seq(string, COLON, value);

    public rule object =
        seq(LBRACE, pair.sep(0, COMMA), RBRACE);

    public rule array =
        seq(LBRACKET, value.sep(0, COMMA), RBRACKET);

    public rule root = seq(ws, value);

    @Override public rule root() {
        return root;
    }

    public void parse (String inputName, String input) {
        ParseResult result = Autumn.parse(root, input, ParseOptions.get());
        if (result.fullMatch) {
            System.out.println(result.toString());
        } else {
            // debugging
            System.out.println(result.toString(new LineMapString(inputName, input), false));
            // for users
            System.out.println(result.userErrorString(new LineMapString(inputName, input)));
        }
    }

    public static void main (String[] args) {
        // failing parse example
        new JSON().parse("<test>", "{ \"test\" : // }");
    }
}
```

The `// Lexical` section marks the part of the grammar where we are concerned about matched
characters and whitespace — which in other parsing systems is typically the role of a lexer like
[flex]. The `// Syntactic` section contains the
rest of the grammar.

[lex]: https://westes.github.io/flex/manual/

As you can see at a glance, the correspondance with EBNF is pretty direct. Let's go over some
peculiarities.

## `Grammar`, `rule`, parsers and combinators

First, notice we inherit from [`Grammar`]. `Grammar` (for Domain Specific Language) is a base class that
contains a bunch of methods which we will use to define our grammar.

`Grammar` also defines the [`rule`] class, which represents a rule in our grammar.

In reality, `rule` is merely a wrapper around the more fundamental [`Parser`] class. `rule` also
defines a lot of methods that help construct new rules (hence, parsers). So for instance, in rule
`integer`, you have `digit.at_least(1)`. [`digit`] is a rule pre-defined in `Grammar` (as is
[`hex_digit`]), and `at_least` is a method in `rule` that returns a new rule (here, a rule that
matches as many repetitions of `digit` as possible, with a minimum of one).

This is a pretty common way to build up objects in object-oriented programming — it's known as [the
builder pattern].

[the builder pattern]: https://dzone.com/articles/design-patterns-the-builder-pattern

In practice, we'll call those things that have type `rule` or `Parser` "parsers". Indeed, they are
parsers in the sense explained in the [previous section](A1-parsing.md).

Parsers can be combined into bigger parsers, such as in `digit.at_least(1)`. This returns a `rule`
wrapping a parser with type [`Repeat`] (a subclass of `Parser`). We say that `digit` is a
*sub-parser* (or *child parser*) of `digit.at_least(1)`.

We also say that [`at_least`] is a *parser combinator* (or *combinator* for short), because it takes
a parser (in our example, `digit`) and returns a bigger parser. Combinators can have multiple
arguments (e.g. `seq` for sequences). We sometimes abuse the term "combinator" to mean any method
from `Grammar`, even if it does not take a parser as argument. In theory, any instance of `Parser` that
has sub-parsers can also be called a combinator.

In practice, we'll reserve the word "rule" for parsers that are assigned to a field in our grammar —
and hence prefixed with the type `rule`!

The [`root()`] overload specifies the main entry point into the grammar (i.e. the rule to use in
order to parse "the whole thing"). Note that it is perfectly possible to initiate the parse from
other rules!

References: [`Grammar`], [`rule`]

[`Grammar`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.html
[`rule`]:  https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.rule.html
[`Parser`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Parser.html
[`Repeat`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/parsers/Repeat.html
[`at_least`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.rule.html#at_least-int-
[`digit`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.html#digit
[`hex_digit`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.html#hex_digit
[`{ make_rule_names(); }`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.html#make_rule_names--

## Whitespace Handling & String Literals

Let's look at the `{ ws = usual_whitespace; }` initializer at the top. [`ws`] is a field of `Grammar`
that designates the rule to be used for parsing whitespace (if it's `null` — which it is by default
— then no special whitespace handling is performed). Here we assign it the predefined
[`usual_whitespace`] rule, which conveniently matches that of JSON.

Where does this whitespace come into play? In all parsers created by a combinator called `word`. The
[`word(String)`] version returns a parser that matches the specified string and any subsequent
whitespace. The [`rule#word()`] version matches what the receiver matches, followed by any whitespace.

It's also possible to use `ws` directly, as we do in the last (`root`) rule, because we want to
match whitespace *before* our JSON value as well.

References: [`ws`]

[`ws`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.html#ws
[`usual_whitespace`]:  https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.html#usual_whitespace
[`word(String)`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.html#word-String-
[`rule#word()`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.rule.html#word--

## `lazy` and `sep`

You should be able to tell what most of the methods do by comparison with our previous descriptions
of the grammar (in EBFN and English). There are a couple of methods that may appear more mysterious,
however.

(Don't worry if there are gaps in your understanding — we'll go over all basic parsers and
combinators briefly in [A4. Basic Parsers](A4-basic-parsers.md).)

First, there is that `lazy` method taking a lambda in the `value` rule, and how we qualified
`object` and `array` with `this` in that rule.

[`lazy`] returns a parser that will be initialized when first used, based on the lambda that it was
passed. The reason we need `lazy` is that there is recursion in the grammar: an array may contain
values, but a value may itself be an array!

Because we use fields to store our rules, a rule's definition cannot reference fields that are
defined after: when the rule is initialized, these fields won't have been initialized yet!

And that's why we use `lazy` to defer the initialization process. `lazy` still produces a rule that
can be referred from `array`, but avoids capturing the value of `array`, which isn't initialized
at that point in time.

A consequence of this is that Java imposes that we need to prefix `array` and `object` with `this`.

The other method that is slightly different is [`sep`]. For instance in the `array` rule,
`value.sep(0, word(","))` means: a sequence of zero or more values, separated by commas. It's as
simple as that. 

References: [`lazy`], [`sep`]

[`lazy`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.html#lazy-java.util.function.Supplier-
[`sep`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.rule.html#sep-int-java.lang.Object-

## Launching the Parse

The `parse` method shows how one can initiate a parse over a string by using the `Autumn.parse`
entry point with a default set of options (`ParseOptions.get()`). The parse returns q
[`ParseResult`], which amongst other things indicates whether the parse was successful
(`ParseResult#success`), and if so, whether it matched the whole input (`ParseResult#fullMatch`), or
otherwise the furthest position to which the parse could progress before encountering an error
(`ParseResult#errorOffset`).

Note that positions are by default expressed as character-based offset, but can be translated
to `line:column` format by using a [`LineMap`].

Our `parse` method shows how to use [`ParseResult`] to print useful information, such as the status
of the string with `ParseResult#toString`. On failure, this method can include useful debugging
information, such as call stack of the furthest parsing error. If the goal is to serve an error
message to the user however, you should use `ParseResult#userErrorString`. Notice how these
two methods take a [`LineMap`] to help them translate positions.

References: [`Autumn`], [`ParseResult`], [`ParseOptions`], [`LineMap`]

[`Autumn`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Autumn.html
[`ParseResult`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/ParseResult.html
[`ParseOptions`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/ParseOptions.html
[`LineMap`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/positions/LineMap.html

## Conclusion

There you have it, your first grammar! From now on, we won't use EBNF notation anymore, in favor
of actual Autumn code. We also won't repeat what the methods we've already seen do.

Next up: a look under the hood of Autumn ([A3. How Autumn Works](A3-how-autumn-works.md)), then
we'll revisit this grammar in order to generate a proper AST ([A4. Creating an Abstract Syntax Tree
(AST)](A5-creating-an-ast.md)).

----
**Footnotes**

[*1]: #footnote1 
<h6 id="footnote1" display=none;></h6>

(*1) Beware that EBNF is normally a notation for Context-Free Grammars (CFG), and Autumn's grammar
are not CFGs (more information in [the "Vertical Backtracking" sub-section of section
A3][vert-back]).

[vert-back]: A3-how-autumn-works.md#vertical-backtracking
