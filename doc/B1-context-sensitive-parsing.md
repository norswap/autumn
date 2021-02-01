# B1. Context-Sensititive (Stateful) Parsing

**NOTE: This section is outdated and needs to be rewritten/improved.**

What is context sensitivity? There are different ways to approach that question, but the intuitive
way I like to think about it is that it's about *recalling data derived from previous parsing
decisions*.

A prototypal example would be a pair of parsers, the first of which matches a string and saves it,
while the second will match the same as the first string.

This is a toy example, but similar techniques can be deployed in practice, for instance to match XML
tags (with the important difference that tags can be nested).

Let's have a look at the code.

We demonstrate our new parser by using it in a very simple rule: The grammar's root simply attempts
to match a pair of identical identifiers made of letters. e.g. `abc-abc` or `Autumn-Autumn`.

```java
public final class RecallGrammar extends Grammar
{
    ParseState<Map<String, String>> store
        = new ParseState<>(RecallGrammar.class, HashMap::new);

    final class Learn extends AbstractForwarding
    {
        public Learn (String key, rule child)
        {
            super("learn", child.collect().action_with_string(
                (p,xs,str) -> p.log.apply(() -> {
                    Map<String, String> map = store.data(p);
                    String old = map.get(key);
                    map.put(key, str);
                    return () -> { 
                        if (old != null) map.put(key, old);
                        else map.remove(key);
                    };
                })));
        }
    }

    final class Recall extends AbstractPrimitive
    {
        private final String key;

        public Recall (String key) {
            super("recall", false);
            this.key = key;
        }

        @Override protected boolean doparse (Parse parse)
        {
            String string = store.data(parse).get(key);
            if (string == null)
                throw new IllegalStateException(
                    "No registered string for key: " + key);
            if (parse.match(parse.pos, string)) {
                parse.pos += string.length();
                return true;
            }
            return false;
        }
    }

    public rule identifier = alpha.at_least(1);
    public rule learn_id = rule(new Learn("id", identifier));
    public rule recall_id = rule(new Recall("id"));
    public rule root = seq(learn_id, str("-"), recall_id);
}
```

In the above code, we define two new custom parsers (`Learn` and `Recall`). We saw how to define
custom parsers in [B3. Writing Custom Parsers][B3].

(TODO: future reference)

A `Learn` parser takes a string key and a sub-parser as parameter. It runs its sub-parsers, then
collects the matched text and stores it in a map with the given key. We'll give more details soon.

A `Recall` parser takes a key as parameter and attempt to match the string stored in the map under
that key.



This may seem like a lot of work for a trivial grammar, but consider that most parsing tools are
unable to do this. The only exceptions I know of are [Colm] (quite specialized) and [Marpa] (whose
design for this I find harder than Autumn's — make your own opinion).

[B3]: B3-custom-parsers.md
[Colm]: http://www.colm.net/open-source/colm/
[Marpa]: http://jeffreykegler.github.io/Ocean-of-Awareness-blog/individual/2018/05/csg.html

## Parse State

The first detail we need to get into is the [`ParseState`] class. This class enables decoupling
mutable state from parsers. The reason this is necessary is that parsers may be used by multiple
parses — either successive or parallel. Each parse should have its own copy of the data: parses
should not affect one another.

```java
ParseState<DATA> state = new ParseState<>(KEY, () -> new DATA(...));
```

When you write a line like the above, you are specifying that you want the parse to
store an instance of `DATA` (substitue the actual class you need). This instance will be stored
in the [`Parse#state_data`] map, using the given `KEY`. The first time the data is requested, it
will be created using the function passed as second parameter.

Inside a parser, when you need to access the data, you write `state.data(parse)` where `parse` is
the [`Parse`] object received by the parser's [`Parser#doparse`] method.

As for the `KEY`, you should pick something unique. When you need a single instance of the data for
all instances of the parsers, use a class object, e.g. `DATA.class`. When you need an instance of
the data per instance of the parser, simply use the parser object itself (`this`)! 

In our example above, we have:

```java
ParseState<Map<String, String>> store
    = new ParseState<>(RecallGrammar.class, HashMap::new);
```

Our data is a `String -> String` map, the key is the grammar class, and we simply use `new` to
create a new hashmap.

**Important:** A [`ParseState`] by itself does not help with context-sensitivity! It just ensures
that each parse gets its own copy of the data.

This is mostly useful for context-sensitive parsing, but not only. For instance, when memoizing we
want each parse to have its own memoization table, but the table is not context-sensitive data (by
which we mean it is insensitive to backtracking). Memoization in Autumn is covered in [B2.
Memoization][B2]. 

We also note that [`ParseState`] employs caching to avoid performing a map lookup each time the
state is accessed. However, this only works for a single [`Parse`] at a time.

Since the data isn't stored in the [`ParseState`] itself, it's allowed to have multiple
[`ParseState`] with the same key — but only as long as they are constructed with the same supplier
(the second constructor argument)!

[`ParseState`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/ParseState.html
[`Parse`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Parse.html
[`Parse#state_data`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Parse.html#state_data
[`Parser#doparse`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Parser.html#doparse-norswap.autumn.Parse-
[B2]: B2-memoization.md

## Parse State and Backtracking

As covered in [A3. How Autumn Works][A3], Autumn may *backtrack* over parsers that failed to match
... even if some of their sub-parsers did match. In our previous understanding, this simply
meant that we had to reset the input position to what it was before the parser was invoked,
and to discard any AST nodes created by the parser.

But what if the parser also made a state change?

Well, in this case it is necessary to undo the state change.

**This is why state (context) changes should never be made directly, but should be made via the [`Log`] instead**
(which can be accessed via [`Parse#log`]).

To make a state change, you call [`Log#apply`] and supply it an instance of [`SideEffect`] — which 
is a function that (1) makes the state change, (2) returns a function that undoes the state change.
This is exemplified in the `Learn` class above ([*1]).

We return a function (rather than supply one separately) because it might be necessary to capture
some elements of the context in order to properly undo the change.

The [`Log`] stores a list of [`SideEffect.Applied`], which is a pair comprising the executed
[`SideEffect`] and the undo function it returned. This enables us not only to undo applied side
effects, but also to "replay" side-effects that we had previously undone. This capability comes in
handy for parsers that speculatively run multiple parsers before selecting the preferred parsing
outcome — most notably [`Longest`].

Undoing side-effects when backtracking is done automatically by [`Parser#parse`]. However, custom
parsers may also manipulate the log. For more information, refer to the Javadoc of the various
methods in [`Log`].

A word on nomeclature. We mostly talked about mutating parse state. This is in fact what we call
*the context*. In general, I'll indiscriminately use *context* or *parse state* to refer to the
idea.

And so, context-sensitive parsing is just that: parsing with state, with the guarantee that the
state changes will be rolled back upon backtracking.

[A3]: A3-how-autumn-works.md
[`Log`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Log.html
[`Parse#log`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Parse.html#log
[`Log#apply`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Log.html#apply-norswap.autumn.SideEffect-
[`SideEffect`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/SideEffect.html
[`SideEffect.Applied`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/SideEffect.Applied.html
[`Longest`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/parsers/Longest.html
[`Parser#parse`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Parser.html#parse-norswap.autumn.Parse-

## Side Effecting Data Structures

We mentionned earlier (in [A5. Creating an AST][A5]) that the value stack on which we push AST nodes
is a form of context.

The value stack ([`Parse#stack`]) is an instance of [`SideEffectingArrayStack`], which is a
stack-like data structure where some of the operations (refer to the
[Javadoc][`SideEffectingArrayStack`]) automatically apply side-effects on [`Parse#log`].

Similarly, you could create your own side-effecting data structure to simplify context
manipulations. These data structures need to keep a reference to a [`Parse`] so that they can modify
the log.

[A5]: A5-creating-an-ast.md#value-stack-as-context
[`Parse#stack`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Parse.html#stack
[`SideEffectingArrayStack`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/SideEffectingArrayStack.html

## A Full Example (a^n b^n c^n)

**TODO: I think this will move towards the end as a kind of capstone exampele**

One example that is often used to demonstrate that PEG parsers can recognize grammars that CFG
parsers can't is the `a^n b^n cˆn` language — that is, `n` repetitions of`'a'`, then `n` of `'b'`
then `n` of `'c'`, for the same `n > 0`.

This can in fact be expressed with the following grammar:

```java
public rule AB = recursive(self ->
    seq(character('a'), self.opt(), character('b')));
public rule BC = recursive(self ->
    seq(character('b'), self.opt(), character('c'))); 
public rule S = seq(
    seq(AB, character('c')).ahead(),
    character('a').at_least(1),
    BC); 
```

If you think that looks more like the solution to a puzzle than something readable, then I agree.

But in Autumn, we're able to define some custom parsers, so that we can ultimately write:

```java
import norswap.autumn.*;
import norswap.autumn.visitors.WellFormednessChecker;

public final class TripletGrammar extends Grammar
{
    public rule a = CountingRepeat.with(str("a"));
    public rule b = CountedRepeat .with(str("b"));
    public rule c = CountedRepeat .with(str("c"));
    public rule root = seq(a, b, c);

    public static ParseResult parse (String input)
    {
        TripletGrammar grammar = new TripletGrammar();
        WellFormednessChecker checker = new WellFormednessChecker(
            new ParserVisitorTriplet.VisitorFirstParsersTriplet(),
            new ParserVisitorTriplet.VisitorNullableRepetitionTriplet());

        return Autumn.parse(grammar.root, input,
            ParseOptions.well_formedness_checker(() -> checker).get());
    }
}
```

The logic is fairly simple: `a` will count how many `'a'` it can match, and then `b` and `c` need to
match the same amount.

What is necessary to make this happen? We have to define `CountingRepeat` and `CountedRepeat`.
You can access their code here: [`CountingRepeat`], [`CountedRepeat`].

This time, we didn't use `Abstract*` parsers to define them, so if we want to use visitors, we need
to define a new visitor interface and new implementations of our existing visitors (cf. [B5.
Visiting Parsers & Walking The Parser Graph][B5]). Here, we just want the three built-in visitors in
order to benefit from Autumn's well-formedness check. These are all implemented in
[`ParserVisitorTriplet`].

<!-- TODO: well-formed is a forward reference -->

And in fact, it is useful to implement the well-formedness check in this case. Indeed,
if we passed a nullable parser to [`CountingRepeat`], it would loop forever.

I think this example neatly showcases Autumn's philosophy. There is a bit of effort and boilerplate
involved in the definition of the parsers, but the result is neat, modular and safe.

First, the final grammar is pretty readable. Second, the parsers are safe: the context is handled
properly and the well-formedness check is implemented. Third, it's modular: the parsers can
be reused in another grammar.

[`CountingRepeat`]: /examples/triplet/CountingRepeat.java
[`CountedRepeat`]: /examples/triplet/CountedRepeat.java
[`ParserVisitorTriplet`]: /examples/triplet/ParserVisitorTriplet.java
[B5]: B5-parser-visitors-walkers.md

----
**Footnotes**

[*1]: #footnote1 
<h6 id="footnote1" display=none;></h6>

(*1) Note that in this particular grammar, we wouldn't actually need the undo function, since we
always call a `Learn` parser immediately before a `Recall` parser. Nevertheless, it's good practice
to use the context-sensitivity facilities anyway, as it lets us extend and change the grammar
without fear of breaking things.

Also note the semantics of these parsers may not be what you expect, for instance `seq(learn_id,
learn_id, recall_id, recall_id)` does not match `abba` but does match `abbb`: the identifier that
was learned the latest wins!