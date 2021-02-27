# A6. Left-Recursion and Associativity

As we saw in section [A4. Basic Parsers] (headline "Lazy Parsing and Recursion"), you can use the
[`lazy`] and [`recursive`] combinators in order to perform parser recursion. However, left-recursion
(when a parser invokes itself directly or indirectly at the same input position) is forbidden.

The reason is that, if the position doesn't change, the parser will keep invoking itself at the same
position indifinitely. Or, in practice for Autumn, until it runs out of stack space, resulting in a
`StackOverflowError`. Note that Autumn intercepts this error and wraps it in another error
that warns about left-recursion.

In fact, by default Autumn specifies the [`well_formedness_check`] option, which analyses the
grammar before the parse to determine if it contains left-recursive loops (such a loop is a chain of
parsers through which a parser ends up invoking itself at the same position) ([*1]).

Note that if you use custom parsers (cf. [B3. Writing Custom Parsers]), you'll additionally need to
use the [`well_formedness_checker`] option. I also recommend disabling these options in
production to avoid their overhead, but they'll help you catch bugs while you construct your
grammar.

Nevertheless, there are good reasons why one might want to use left-recursion, and Autumn supplies
solutions for those use-cases.

[A4. Basic Parsers]: A4-basic-parsers.md#lazy-parsing-and-recursion
[B3. Writing Custom Parsers]: B3-custom-parsers.md
[`lazy`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.html#lazy-java.util.function.Supplier-
[`recursive`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.html#recursive-java.util.function.Function-
[`well_formedness_check`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/ParseOptions.html#well_formedness_check
[`well_formedness_checker`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/ParseOptions.html#well_formedness_checker

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
which is the standard interpretation for division.

Building the right-associative interpretation is not a problem:

```
rule div = recursive(self ->
    choice(
        seq(integer, word("/"), self).push($ -> new Div($.$0(), $.$1())),
        integer);
```

Running this rule over input `1/2/2` will yield the equivalent of `new Div(1, new Div(2, 2))`.

(Here, `Div` is a class for an AST node representing division that the user defined. It's nothing
special, just imagine it has two node fields matching the two constructor parameters: one for the
numerator and one for the denominator.)

To build the left-associative interpretation, we offer the [`left_expression`] combinator:

```
rule div = left_expression()
    .operand(integer)
    .infix(word("/"), $ -> new Div($.$0(), $.$1()));
```

Running this rule over input `1/2/2` will yield the equivalent of `new Div(new Div(1, 2), 2)`.

The [`left_expression`] combinator isn't magical, in fact we could formulate the same rule in terms
of combinators that were previously introduced in this guide:

```
rule div =
    seq(integer,
        seq(str("/"), integer)
        .push($ -> new Div($.$0(), $.$1()), LOOKBACK(1))
        .at_least(0))
```

In the above, we express the rule as the left-side (an integer) followed by a repetition of the
operator and the right-side ([*2]). The trick is the use of lookback (cf. [A5. Creating an Abstract
Syntax Tree (AST)][A5-custom], sub-section "Customizing AST Combinators") that will retrieve the
leftmost integer on the first repetition and then, on each subsequent repetition, the result of the
previous repetition.

As you will have noticed, the `left_expression` combinator is different from combinators we saw
before, in that it consists of multiple chained calls, but it otherwise works similarly.
`left_expression` actually returns a [`LeftExpressionBuilder`] which subclasses `rule`. You can use
the method of this class (and its parent [`ExpressionBuilder`]) to construct the left-expression
parser, an instance of [`LeftExpression`].

Besides the `operand` method we used before, you can provide separate parsers for the left- and
right-hand sides through the `left` and `right` method.

The `infix` method used above can be specified multiple times, for different operators. The operators
will be tried in the order in which the `infix` methods are called.

If the distinction between the operator and the right hand-side does not exist, you can use `suffix`
to specify a single parser. Just like `infix`, multiple suffixes can be provided and suffixes will
be repeated if possible. For instance, if you specify `++` as a suffix on variable operands, `i ++
++` will yield a legal parse, interpreted a `(i++)++`.

Besides suffix operators, `suffix` is notably useful for Haskell-style function calls, where `f g x`
is understood as `f(g(x))`.

Note that if a `left_expression` has both infixes and suffixes, the infixes are tried before the
suffixes (inside each category, the parsers are tried in the order in which the methods are called).

[`left_expression`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.html#left_expression--
[A5-custom]: A5-creating-an-ast.md#customizing-collect-parsers
[`LeftExpressionBuilder`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.LeftExpressionBuilder.html
[`ExpressionBuilder`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.ExpressionBuilder.html
[`LeftExpression`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/parsers/LeftExpression.html

## Right-Associative Parses

In the previous sub-section, we showed how to write our integer division rule in both
right-associative style using `recursive` and left-associative style using `left_expression`.
Ultimately, the left-associative ends up much simpler to write:

```
// right-associative
rule div = recursive(self ->
    choice(
        seq(integer, str("/"), self).push((p,xs) -> new Div($(xs,0), $(xs,1))),
        integer));

// left-associative
rule div = left_expression()
    .operand(integer)
    .infix(word("/"), $ -> new Div($.$0(), $.$1()));
```

For the sake of symmetry, we decided to introduce a [`right_expression`] combinator that would enable defining
right-associate rules similarly to left-associative rules:

```
rule div = right_expression()
    .operand(integer)
    .infix(word("/"), $ -> new Div($.$0(), $.$1()));
```

This is semantically equivalent to our previous right-associative formulation.

`right_expression` returns [`RightExpressionBuilder`] (inheriting [`ExpressionBuilder`] and `rule`).
Its methods are similar to those of `LeftExpressionBuilder`. The constructed parser is a
[`RightExpression`].

In particular, `RightExpressionBuilder` does not have a `suffix` method, but a `prefix` method.
So using the prefix increment operator, `++ ++ i` will be interpreted as `++(++i)`.

Unlike for `left_expression` (where infixes have priority over suffixes), here prefixes have
priority on the infixes.

[`right_expression`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.html#right_expression--
[`RightExpressionBuilder`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.RightExpressionBuilder.html
[`RightExpression`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/parsers/RightExpression.html

## Mandatory Operators

By default, the expression parsers will match their operand even if there are no operators.
If you want the parser to fail when no operators are present, simply add `requireOperator()`:

```
rule div = left_expression()
    .operand(integer)
    .infix(word("/"), $ -> new Div($.$0(), $.$1()))
    .requireOperator();
```

---
**Footnotes**

[*1]: #footnote1 
<h6 id="footnote1" display=none;></h6>

(*1) The option also checks for repetition over nullable parsers. A nullable parser is a parser that
can succeed while consuming no input. For instance`str("foo").opt().at_least(1)` is a repetition
over a nullable parser. These are bad because they cause infinite loops: you can repeatedly invoke a
parser that consumes no input at the same position while the parse makes no progress. This is not
however what we're concerned with in this section.

[*2]: #footnote2
<h6 id="footnote2" display=none;></h6>

(*2) It is not actually required to distinguish the operator from the right-hand side — the operator
can be folded into the right-hand side without loss of generality. Nevertheless, since binary
operators are a disproportionally common use case, we do separate it in the combinator in order to
make uses of the combinator terser and more elegant. If the operator is not required, an [`empty`]
combinator can be used there.

[`empty`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.html#empty