# A6. Left-Recursion and Associativity

As we saw in section [A4. Basic Parsers] (headline "Lazy Parsing and Recursion"), you can use the
[`lazy`] and [`recursive`] combinators in order to perform parser recursion. However, left-recursion
(when a parser invokes itself directly or indirectly at the same input position) is forbidden.

The reason is that, if the position doesn't change, the parser will keep invoking itself at the same
position indifinitely. Or, in practice for Autumn, until it runs out of stack space, resulting in a
`StackOverflowError`. Note that Autumn intercepts this error and wraps it another error
that warns about left-recursion.

In fact, this warning will tell you to specify the [`well_formed_check`] or [`well_formed_checker`]
options (the former works with built-in parsers only, while the second one enables making special
provisions for custom parsers). For instance:

```
Autumn.parse(grammar.root, input_string, ParseOptions.well_formed_check().get());
```

What these options do is that, before starting the parse, they will analyse the grammar and
determine whether it contains left-recursive loops (such a loop is a chain of parser through which a
parser ends up invoking itself at the same position), or repetitions over nullable parsers.

A nullable parser is a parser that can succeed while consuming no input. For
instance`str("foo").opt().at_least(1)` is a repetition over a nullable parser. These are bad because
they cause infinite loops: you can repeatedly invoke a parser that consumes no input at the same
position and the parse makes no progress. This is not however what we're concerned with in this
section.

Nevertheless, there are good reasons why we might want to use left-recursion, and Autumn supplies
solutions for those use-cases.

[A4. Basic Parsers]: A4-basic-parsers.md#lazy-parsing-and-recursion
[`lazy`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#lazy-java.util.function.Supplier-
[`recursive`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#recursive-java.util.function.Function-
[`well_formed_check`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/ParseOptions.html#well_formed_check
[`well_formed_checker`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/ParseOptions.html#well_formed_checker

## Repetitions

The first reason that left-recursion is used, most notably in CFG grammars, is repetitions.
The CFG grammar formalism doesn't have an explicit repetition operator, so a repetition could be
expressed like this:

```
A ::= Aa | a
```

Which we can straighforwardly translate in Autumn to this:

```
str("a").at_least(1)
```

## Left-Associative Parses

The second reason that left-recursion is used is to specify how Abstract Syntax Trees (ASTs) should
be built. Consider for instance the two following formulations of the language of division over
integers:

```
Div ::= Integer '/' Div | Integer
Div ::= Div '/' Integer | Integer
```

These two rules differ on how to interpret the input `1/2/2`. The first one interprets it as
`1/(2/2)` which evaluates to `1`. This is the *right-associative* interpretation. The second one
interprets it as `(1/2)/2` which evaluates to `1/4`. This is the *left-associative* interpretation —
which is traditionally the preferred one for division.

Building the right-associative interpretation is not a problem:

```
rule div = recursive(self ->
    choice(
        seq(integer, str("/"), self).push((p,xs) -> new Div($(xs,0), $(xs,1))),
        integer);
```

Running this rule over input `1/2/2` will yield the equivalent of `new Div(1, new Div(2, 2))`.

To build the left-associative interpretation, we offer the `left` combinator:

```
rule div = left(integer, str("/"), (p,xs) -> new Div($(xs,0), $(xs,1)));
```

Running this rule over input `1/2/2` will yield the equivalent of `new Div(new Div(1, 2), 2)`.

The [`left`] combinator isn't magical, in fact we could formulate it in terms of combinators
that were previously introduced in this guide:

```
rule div =
    seq(integer,
        seq(str("/"), integer)
            .lookback(1)
            .push((p,xs) -> new Div($(xs,0), $(xs,1)))
            .at_least(0))
```

In the above, we express the rule as the left-side (an integer) followed by a repetition of the
operator and the right-side ([*1]). The trick is the use of lookback (cf. TODO) that will retrieve
the leftmost integer on the first repetition then, on each subsequent repetition, the result of the
previous repetition. 

The `left` combinator admits a couple of variants:

- [`left(operand, operator, action)`]

  This is the variant used above, where we assume the left- and the right-hand operands are
  parsed by the same parser.  

- [`left(left, operator, right, action)`]

  The left-hand side mustn't necessarily be identical to the (repeated) right-hand side, and this
  overload allows specifying two different parsers for these.

- [`left_full(operand, operator, action)`] and [`left_full(left, operator, right, action)`]

  These two are equivalent to the two "simple" `left` overloads, but mandate that the operator and
  right-hand side must appear at least once. 

[`left`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#left-java.lang.Object-java.lang.Object-norswap.autumn.StackAction.Push-
[`left(operand, operator, action)`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#left-java.lang.Object-java.lang.Object-norswap.autumn.StackAction.Push-
[`left(left, operator, right, action)`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#left-java.lang.Object-java.lang.Object-java.lang.Object-norswap.autumn.StackAction.Push-
[`left_full(operand, operator, action)`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#left_full-java.lang.Object-java.lang.Object-norswap.autumn.StackAction.Push- 
[`left_full(left, operator, right, action)`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#left_full-java.lang.Object-java.lang.Object-java.lang.Object-norswap.autumn.StackAction.Push-

## Right-Associative Parses

In the previous sub-section, we showed how to write our integer division rule in both
right-associative style using `recursive` and left-associative style using `left`.
Ultimately, the left-associative ends up much simpler to write:

```
// right-associative
rule div = recursive(self ->
    choice(
        seq(integer, str("/"), self).push((p,xs) -> new Div($(xs,0), $(xs,1))),
        integer));

// left-associative
rule div = left(integer, str("/"), (p,xs) -> new Div($(xs,0), $(xs,1)));
```

For the sake of symmetry, we decided to introduce a [`right`] combinator that would enable defining
a rule in right-associative style in a similar way as the left-associative style:

```
rule div = right(integer, str("/"), (p,xs) -> new Div($(xs,0), $(xs,1)));
```

This is equivalent to the first formulation of the right-associative style we showed in the
previous sub-section.

`right` also admits a [`right(left, operator, right, action)`] overload, and this time, it's
the left operand that gets repeated multiple times.

Similarly, [`right_full(operand, operator, action)`] and [`right_full(left, operator, right,
action)`] also exist and require the operand and left-hand side to appear at least once.

[`right`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#right-java.lang.Object-java.lang.Object-norswap.autumn.StackAction.Push-
[`right(left, operator, right, action)`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#right-java.lang.Object-java.lang.Object-java.lang.Object-norswap.autumn.StackAction.Push-
[`right_full(operand, operator, action)`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#right_full-java.lang.Object-java.lang.Object-norswap.autumn.StackAction.Push-
[`right_full(left, operator, right, action)`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#right_full-java.lang.Object-java.lang.Object-java.lang.Object-norswap.autumn.StackAction.Push-

## A Sub-Optimal Solution: Explicit Left-Recursion via Seed Growing

There is a final solution **which we do not recommend** for handling left-recursion.

(As such, this is an advanced and technical section — feel free to skip it.)

It comes in the form of the [`left_recursive`] and [`left_recursive_left_assoc`] combinators.
Those are used just like the [`recursive`] combinator. So for the left-associative formulation
of our integer division example, we'd write:

```
rule div = left_recursive_left_assoc(self ->
    choice(
        seq(self, str("/"), integer).(p,xs) -> new Div($(xs,0), $(xs,1)),
        integer));
```

The symmetry with the initial right-associative formulation should be obvious.

There are some differences between `left_recursive` and `left_recursive_left_assoc`, most notably
whenever they are used to define a rule that is both left- and right-recursive. In that case,
`left_recursive` will produce a right-associative parse, while `left_recursive_left_assoc` will
force a left-associative parse. `left_recursive` can also produce left-associative parses, but only
when the rule isn't also right-recursive!

So why don't we recommend it?

- A weak reason: the `left` and `right` combinators are cleaner for most practical use cases.
- A stronger reason: because of how the combinators are implemented, and the consequent pitfalls.

Here is how the `left_recursive` algorithm works: it starts by blocking left-recursion, but performs
otherwise a normal parse. Once it has this initial result (*the seed*), it will rerun the parse, but
substitute the seed whenever a left-recursive call to `self` is made. Then, if the new result (the
new seed) consumes more input than the initial one, the algorithm will re-run the parse again but
substitute the new seed for left-recursive calls. This process continues until a run fails to grow
the seed (i.e. the amount of input consumed).

This is mostly fine, if slightly inefficient, because the last run has to be done twice (to ensure
it can't grow further), and because of increased state management operations (cf. Section TODO).

In `left_recursive_left_assoc`, however, we must prevent right-recursion to force a left-associative
parse. Blocking right-recursion straight out can't possible work (consider `A ::= AA | a`), so
instead, we must allow *a single* level of right-recursion. And actually, it's impossible to detect
if some recursion is right-recursion (i.e. no input will be matched after the input matched by the
recursion). We must therefore block all recursions, even those forms we could afford to allow. This
is unforunate, as things like the ternary operator (`condition ? expression1 : expression2`) may
have "middle-recursion". We can however make an escape-hatch operator to re-enable middle-recursion
whenever the programmer can reason that it is safe ([`rule#guarded`]).

Even more annoying, how should the rule `A ::= AA | bA | a` behave if we want `AA`  to be
left-associative? The issue is that `bA` has a perfectly legimitate unambigious right-recursive
implementation — but that can't possibly be matched using `left_recursive_left_assoc`.

The issue is that the semantics of associativity selection is trivial in simple expression-based
examples, but difficult to formulate in general, and even harder to implement.

In a sense, this is what the [`left`] and [`right`] combinators do: they impose the simple (and
overwhelmingly useful) form to avoid the possibility of degenerate (and useless) cases.

So why did we leave it in? A couple reasons:

- They can still be useful when porting grammars originally formulated as CFGs. Using these
  combinators help minimize the amount of rewriting to be done.
  
- As a warning for the wise: here be dragons. I've seen plenty of PEG frameworks who claim to
  support left-recursion but fail to specify to which extent or what the pitfalls are. Consider
  this the grain of salt to these claims.
  
- The algorithms and the afferent analysis are one of the contribution of my PhD thesis (coming
  soon), after all.

[`left_recursive`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#left_recursive-java.util.function.Function-
[`left_recursive_left_assoc`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#left_recursive_left_assoc-java.util.function.Function-
[`rule#guarded`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.rule.html#guarded--

---
**Footnotes**

[*1]: #footnote1 
<h6 id="footnote1" display=none;></h6>

(*1) It is not actually required to distinguish the operator and the right-hand side — the operator
can be folded into the right-hand side without less of generality. Nevertheless, since binary
operators is a disproportionally common use case, we do separate it in the combinator in order to
make uses of the combinator terser and more elegant. If the operator is not required, an [`empty`]
combinator can be used there.

[`empty`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#empty