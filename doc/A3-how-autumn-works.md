# A3. How Autumn Works

In the [last section][A2], we saw how to create a simple grammar and use it to
parse input by calling [`Autumn.parse`]. In this section, we'll see what goes on under the hood when
you do this.

This understanding is pretty important in order to understand how to implement your own custom
parsers ([B3. Writing Custom Parsers]), as well as how to interpret parse results (for instance,
what is the *furthest error position*).

It might also be illuminating in order to understand why certain parsers work the way they do. For
instance, why does a [`Choice`] parser always pick the first alternative that matches instead of
exploring all of them? In passing, this will shed light on the relationship between Autumn and
grammar formalisms like PEG and CFG.

[A2]: A2-first-grammar.md
[`Autumn.parse`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Autumn.html
[`Choice`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/parsers/Choice.html
[B3. Writing Custom Parsers]: B3-custom-parsers.md

## Grammars

A grammar is nothing more than a collection of [`Parser`]. The [`Grammar`] class itself is mostly a
(great) convenience to help you build these parses (which will then be wrapped in instances of
[`rule`] — see [previous section][A2]). Each `Parser` is potentially an entry point that can be
used, though one generally uses a "root" parser that corresponds to the unit of choice in the
language (e.g. a source file). If using `Grammar`, you'll define the root parser by overriding
`Grammar#root()`.

All the parsers form a "parser graph" whose edges are given by the [`Parser#children()`]. We'll
see how to traverse this graph in section [B5. Visiting Parsers & Walking The Parser Graph][B5].

Finally, there is one big requirement on Autumn grammars: they have to be *well-formed*. This mean
they shouldn't contain unprotected left-recursion, nor repetition of nullable parsers (a nullable
parser is a parser that can succeed while matching no input). These violations lead to stack
overflows and infinite loops (respectively).

By default, Autumn is able to check for well-formedness. This is explained in [the "Built-In
Visitors" sub-section of section B5][builtinvis].

[`Grammar`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.html
[`Parser`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Parser.html
[`rule`]:  https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.rule.html
[`Parser#children()`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Parser.html#children-
[B5]: B5-parser-visitors-walkers.md
[builtinvis]: B5-parser-visitors-walkers.md#built-in-visitors

## `Parser` and `Parse`

We already mentionned the [`Parser`] type. Instances of this type are capable of recognizing some
input.

More precisely, a parser is at core a function that, given the remaining input, succeeds or fails at
matching a prefix of this remaining input.

Each kind of parser is a subclass of `Parser` which overrides its [`boolean doparse(Parse)`] method.
`doparse` is a protected method used to implement [`boolean parse(Parse)`], which is the method that
triggers the parser. The reason for the separation of both methods is that `parse` takes care of
some bookkeeping automatically.

Together, these methods fullfill the function of parsing, and do so by reading and modifying a
[`Parse`] object. As the name implies, this represents a "parse" over an input. A `Parse` contains,
amongst other things, the input, the current position within the input, the position of the furthest
error encountered so far and a stack to build an AST (see [A5. Creating an AST][A5]).

Currently, parses admit two different types of input, either a `String` or a list of objects. ([*1])

A parser checks if it matches the input by calling subparsers, or by direct comparison against
characters or objects (via [`Parse#charAt(index)`] or [`Parse#objectAt(index)`]).

`doparse` must return `true` if it matched some input, in which case it must set `Parse#pos` past
the input that was matched. Otherwise, it must return `false` — `parse` will take care to reset
`Parse#pos` to its initial value.

References: [`Parser`], [`Parse`]

[`Parser`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Parser.html 
[`Parse`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Parse.html
[`Parse#charAt(index)`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Parse.html#charAt-int-
[`Parse#objectAt(index)`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Parse.html#objectAt-int-
[`boolean doparse(Parse)`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Parser.html#doparse-norswap.autumn.Parse-
[`boolean parse(Parse)`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Parser.html#parse-norswap.autumn.Parse-
[A5]: A5-creating-an-ast.md

## Vertical Backtracking

[`seq`] and [`choice`] (which return instances of the [`Sequence`] and [`Choice`] parsers,
respectively) are two of the most important combinators.

A sequence parser matches all of its children in a sequence. A choice parser matches the first thing
as its first matching child (meaning the children are **ordered**).

The behaviour of the choice parser can sometimes be surprising to the beginner: the parser
created by `choice(string("a"), string("aa"))` will never match "aa" — only "a" because that
alternative is first. And in consequence `seq(choice(string("a"), string("aa")), string("b"))`
will fail on the input "aab"!

This behaviour makes sense when you consider what we explained in the previous section.

The sequence parser starts by calling the choice parser. The choice parsers calls the `string("a")`
parser. It succeeds, and so the choice succeeds as well, having consumed "a". The sequence proceeds
to then call the `string("b")` parser. But since the next input character is "a", it fails, and
so does the sequence.

To make the parser succeed, we'd have to remember somehow that the choice had an untried alternative
`string("aa")` and then to redo the whole process. This is what I call *lateral backtracking*.

Autumn only does *vertical backtracking*: in `choice(string("x"), string("y"))`, if we can't match
"x", we'll try to match "y". But once the choice has matched, we'll never revisit that decision.

Why *vertical* and *lateral*? Well in our examples, vertical backtracking backtracks into the
choice from one of its children; while lateral backtracking backtracks into the choice from one 
its siblings (`string("b")` has the same parent as the choice).

There's an important property called *the single parse rule*, which says that a parser, when called
at the same input position (and in context-sensitive parses, with the same context — as we'll see
later) should always yield the same (singular) result. This is equivalent to saying that we
only support vertical backtracking.

Why don't we adopt the alternative semantics where `string("aa")` would be tried? There are two
big reasons:

- It makes it much harder to write custom parsers.
- When using these semantics with a naive implementation, there is a big performance impact.
  But we rely on the fact that the implementation of our parsers is relatively naive to make
  it easy to write custom parsers and to handle context!
  
The semantics of Autumn is based on that of [Parsing Expression Grammars (PEGs)] (although we go
much further than plain PEGs), while the other one is that of [Context Free Grammars (CFGs)]. CFGs
are more ancient and common, which is a reason why the behaviour might be surprising to some.

The issue in our example matching "aab" is called *prefix capture* (because we only match "a", a
prefix of the longer "aa" that we could match).

The CFG semantics prevents prefix capture, but allows ambiguity: the grammar `ab* | a (ba)* b` is
ambiguous with input "abab" which could be intpreted as either *(ab)(ab)* or as *a(ba)b*. PEGs on
the other hand, are unambiguous by construction.

The issues are actually dual: prevent prefix capture, get ambiguity, or vice-versa. And the issues
are equally difficult to manage.

If you do value avoiding prefix capture and don't need the customization & context-sensitivity
possibilities of Autumn, you might be better served with a CFG parser like [ANTLR]. But if writing
custom parsers, context-sensitivity and grammar reification do seem useful, Autumn is your best bet.

I will add that prefix capture has never been a source of hard problems for me, but some people
seem to really dislike it (almost always, those are people who have extensively worked with CFGs
before, though not all CFG experts share this dislike).

<!-- TODO link to debugging and grammar reification -->

References: [`Sequence`], [`Choice`]

[`seq`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.html#seq-java.lang.Object...-
[`choice`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.html#choice-java.lang.Object...- 
[`Sequence`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/parsers/Sequence.html
[`Choice`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/parsers/Choice.html
[Parsing Expression Grammars (PEGs)]: https://en.wikipedia.org/wiki/Parsing_expression_grammar
[Context Free Grammars (CFGs)]: https://en.wikipedia.org/wiki/Context-free_grammar
[ANTLR]: https://www.antlr.org/

## Greed

A direct consequence of the *single parse rule* explained above is that repetitions are *greedy* in
Autumn: the parser `seq(string("a").at_least(0), string("a"))` will never succeed on any input! This
is because the repetition will consume all the "a" characters, leaving none for the next parser in
the sequence.

The principle is the same as before. The repetition has to decide once and for all what it will
match, and so by default it matches as much input as possible. In fact, a parser `a+` is the same
as calling a parser `A` that was defined as `A = aA | a`.

Or in Autumn terms: `string("a").at_least(1)` is the same as a parser `as` with

```
rule as = recursive(self ->
    choice(
        seq(string("a"), self),
        string("a"));
```

Note: the `recursive` combinator just lets us reference `as` as `self` within its own definition.

## Conclusion

You should now understand the basic mode of operation of Autumn. We'll add more details later,
especially regarding context-sensitivity.

Next up is a short presentation of the [common parsers and combinators of Autumn], so that you know
what is available!

[common parsers and combinators of Autumn]: A4-basic-parsers.md

----
**Footnotes**

[*1]: #footnote1 
<h6 id="footnote1" display=none;></h6>

(*1) So far, that's what we support. It's not impossible that support for different type of inputs
will be added in the future. In particular, it would be interesting to support parsing input
streams: inputs that are initially incomplete (e.g. inputs delivered over a network connection).

There are two pitfalls to consider. First, currently we are dependent on a special end-of-input
sentinel, which we produce via [`Parse#charAt(index)`] and [`Parse#objectAt(index)`] based on the
length of the input. This might be abstracted away by an interface, but this abstraction might
introduce undesirable performance overheads. Another solution would to separate input streams from
full inputs. But that forces primitive char- and object-level parser to be retooled to be
polymorphic over both kinds of inputs.

The second pitfall is that since we backtrack, we will ultimately need to buffer the whole input
anyway. Streaming parsing might not be worth it, compared to simply receiving the whole input and
then parsing that.