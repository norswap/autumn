# A2. Your First Grammar

As an introduction to Autumn, let's write a simple but non-trivial grammar and walk through it
together. At first we'll only define a recognizer, but then we'll extend it to produce an abstract
syntax tree (AST).

The language for which we'll write a grammar is JSON (Javascript Object Notation), according to 
the specification at https://www.json.org/.

First, here is the grammar using (a kind of) BNF notation ([*1]). Don't worry if you don't know what that
is, the point is just to make it easy on the eyes as we get started.

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
```

## JSON in English

Let's walk through this real fast by reformulating all of this in English.

- An integer is 0 or a sequence of one or more digit that doesn't start with 0.

- The fractional part of a number is a dot followed by a string of digits.

- The exponent part of a number is 'e' or 'E' optionally followed by '+' or '-', followed by an
  integer.
  
- A number optionally starts with '-', then has an integer, then an optional fractional part and an
  optional exponent part.
  
- An hexadecimal digit is a letter between '0' and '9  or between 'a' and 'f', or between 'A' and
  'F'.
  
 - A string character is any unicode character, but not '"', '\', nor anything in the range
   `[\u0000-\u001F]` (which are control characters). Alternatively it can also be '\' followed
   by either '\', '/', 'b', 'f', 'n', 'r' or 't' (named character escapes), or the characters
   '\' then 'u' then four hexadecimal digits.
   
- A string is zero or more string characters enclosed between double quotes (").

- A value is a string, a number, an object (see below), an array (see below), or the words "true",
  "false" or "null".
  
- A pair is a string followed by a colon, followed by a value.

- An object is a (possibly empty) sequence of pairs, separated by commas and enclosed between curly
  brackets ({}).

- An array is a (possibly empty) sequence of values, separated by commas and enclosed between square
  brackets ([]).

However, this does not *fully* specify the grammar either. We still need to specify where whitespace
is allowed. In JSON, whitespace is allowed after all brackets, commas, colons and values.

Whitespace is comprised of spaces, tabs, newlines (\n) and carriage returns (\r).

## The Autumn Grammar for JSON

With that in mind, let's see our JSON grammar.

```java
import norswap.autumn.DSL;

public final class JSON extends DSL
{
    { ws = usual_whitespace; }

    public rule integer = choice(
        character('0'),
        digit.at_least(1));

    public rule fractional =
        seq(character('.'), digit.at_least(1));

    public rule exponent =
        seq(set("eE"), set("+-").opt(), integer);

    public rule number =
        seq(character('-').opt(), integer, fractional.opt(), exponent.opt()).word();

    public rule string_char = choice(
        seq(set('"', '\\').not(), range('\u0000', '\u001F').not(), any),
        seq(character('\\'), set("\\/bfnrt")),
        seq(str("\\u"), hex_digit, hex_digit, hex_digit, hex_digit));

    public rule string =
        seq(character('"'), string_char.at_least(0), character('"')).word();

    public rule value = lazy(() -> choice(
        string,
        number,
        this.object,
        this.array,
        word("true"),
        word("false"),
        word("null")));

    public rule pair =
        seq(string, word(":"), value);

    public rule object =
        seq(word("{"), pair.sep(0, word(",")), word("}"));

    public rule array =
        seq(word("["), value.sep(0, word(",")), word("]"));

    public rule root = seq(ws, value);
    
    { make_rule_names(); }
}

```

As you can see at a glance, the correspondance is pretty direct. Let's go over some peculiarities.

## `DSL` and `rule`

First, notice we inherit from `DSL`. `DSL` (for Domain Specific Language) is a base class that
contains a bunch of methods which we will use to define our grammar.

`DSL` also defines the `rule` class, which represents a rule in our grammar.

In reality, `rule` is merely a wrapper around the more fundamental `Parser` class. It defines a bunch of methods that helps
construct new rules (hence, parsers). So for instance, in rule `integer`, you have
`digit.at_least(1)`. `digit` is a rule pre-defined in `DSL` (as is `hex_digit`!), and `at_least` is
a method in `rule` that returns a new rule (here, a rule that matches as many repetition of `digit`
as possible with a minimum of one).

This is a pretty common way to build up objects in object-oriented programming — it's known as [the
builder pattern].

[the build pattern]: https://dzone.com/articles/design-patterns-the-builder-pattern

The `{ make_rule_names(); }` bit is an instance initializer that specifies to give each `rule`
(actually each `Parser`) a printable name corresponding to the name of the field it has been
assigned to. This makes for much more pleasant error output.

References: [`DSL`], [`rule`]

[`DSL`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.html
[`rule`]:  https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.rule.html

## Whitespace Handling

Let's look at the `{ ws = usual_whitespace; }` initializer at the top. `ws` is a field of `DSL` that
designates the rule to be used for parsing whitespace (if it's `null`, which it is by default, then
no special whitespace handling is performed). Here we assign it the predefined `usual_whitespace`
rule, which conveniently matches that of JSON.

Where does this whitespace come into play? In all methods called `word`. The `word(String)` version
returns a rule that matches the specified string followed by any whitespace. The `rule#word()`
version says to match what the receiver matches, followed by any whitespace.

It's also possible to use `ws` directly, as we do in the last (`root`) rule, because we want to
match whitespace *before* our JSON value as well.

We note that as a rule, `ws` must always succeed — it should be able to succeed while matching no
input, which is typically achieved by wrapping a simple whitespace rule with `rule#at_least(0)`. For
instance, `usual_whitespace`is defined as `set(" \t\n\r").at_least(0)`.

References: [`ws`]: set(" \t\n\r").at_least(0);https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#ws

## `lazy` and `sep`

You should be able to tell what most of the methods do by comparison with our previous description
of the grammar. There are two of them that are slightly off, however.

First, there is that `lazy` method taking a lambda in rule `value`, and how we qualified `object`
and `array` with `this.` in that rule.

`lazy` returns a rule whose parser will be initialized when first used, based on the lambda that it
was passed. The reason we need `lazy` is that there is recursion in the grammar: an array may
contain values, but a value may itself be an array!

Because we use fields to store our rules, a rule's definition cannot reference fields that are
defined after: when the rule is initialized, these fields won't have been initialized yet!

And that's why we use `lazy` to defer the initialization process. `lazy` still produces a rule that
can be referred from `array`, but avoids capturing the value of `array`, which isn't initialized
yet. Now recursion works!

A consequence of this is that Java imposes that we need to prefix `array` and `object` with `this.`.

The other method that is slightly different is `sep`. For instance in the `array` rule,
`value.sep(0, word(","))` means: a sequence of zero or more values, separated by commas. It's as
simple as that. 

References: [`lazy`], [`sep`]

[`lazy`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#lazy-java.util.function.Supplier-
[`sep`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.rule.html#sep-int-java.lang.Object-

## Conclusion

There you have it, your first grammar! From now on, we won't use BNF notation anymore, in favor
of actual Autumn code. We also won't repeat what the methods we've already seen do.

Next up: a look inside the hood of Autumn ([A3. How Autumn Works](A3-how-autumn-works.md)), then
we'll revisit this grammar in order to generate a proper AST ([A4. Creating an Abstract Syntax Tree
(AST)](A4-creating-an-ast.md)).

----
**Footnotes**

[*1]: #footnote1 
<h6 id="footnote1" display=none; />

(*1) Beware that BNF is normally a notation for Context-Free Grammars (CFG), and Autumn's grammar
are not CFGs. See (TODO) for more info.