# A5. Creating an Abstract Syntax Tree (AST)

In section [A2. Your First Grammar], we presented a grammar for JSON.

[A2. Your First Grammar]: A2-first-grammar.md

That grammar did recognize valid JSON, but did not build an AST for the parsed JSON input.

In this section, we revisit this grammar, adding in everything it needs to generated a proper AST.
We will then explain the general principles behind AST construction in Autumn.

Without further ado, let's see the new grammar. The intended result is simple: numbers are parsed as
`Double`, strings as `String`, arrays as `List<Object>` and "objects" (dictionaries) as `Map<String,
Object>`. 

```java
import norswap.autumn.Autumn;
import norswap.autumn.DSL;
import norswap.autumn.ParseOptions;
import norswap.autumn.ParseResult;
import java.util.Arrays;
import java.util.stream.Collectors;

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
        seq(character('-').opt(), integer, fractional.opt(), exponent.opt())
        .push(with_string((p,xs,str) -> Double.parseDouble(str)))
        .word();

    public rule string_char = choice(
        seq(set('"', '\\').not(), range('\u0000', '\u001F').not(), any),
        seq(character('\\'), set("\\/bfnrt")),
        seq(str("\\u"), hex_digit, hex_digit, hex_digit, hex_digit));

    public rule string =
        seq(character('"'), string_char.at_least(0), character('"'))
        .push(with_string((p,xs,str) -> str.substring(1, str.length() - 1)))
        .word();

    public rule value = lazy(() -> choice(
        string,
        number,
        this.object,
        this.array,
        word("true")  .as_val(true),
        word("false") .as_val(false),
        word("null")  .as_val(null)));

    public rule pair =
        seq(string, ":", value)
        .push(xs -> xs);

    public rule object =
        seq("{", pair.sep(0, ","), "}")
        .push(xs ->
            Arrays.stream((Object[][]) xs).collect(Collectors.toMap(x -> (String) x[0], x -> x[1])));

    public rule array =
        seq("[", value.sep(0, ","), "]")
        .collect().as_list(Object.class);

    public rule root = seq(ws, value);

    { make_rule_names(); }

    public ParseResult parse (String input) {
        return Autumn.parse(root, input, ParseOptions.get());
    }
}
```

As you can see, there are relatively few changes, namely the additions of combinator calls `push()`,
`as_val()` and `collect().as_list()`.

## Basic Principles & Changes Explained

The basic idea is that there exists a *value stack* (accessible via [`Parse#stack`]) on which
parsers can push or pop items (often AST nodes).

Typically, a parser will pop the nodes pushed on the stack by its children, aggregate them into
a bigger node, and push that on the stack.

But in fact, it is not the parser itself that will do this, but a new parser — of class [`Collect`]
— created by the AST construction combinators. That parser wraps the parser on which the combinator
was called and takes care of the AST construction functionality. 

In our JSON example, a simple case is that of the `as_val` combinators. The parsers returned by
those, when they are successful, simply push the parameter of the method on the value stack.

Then there is the `push` combinator. In its direct form (without `with_string`), this combinator
takes a function of one parameter. This parameter, which we denote `xs` (for "the Xs"), designates
an array of items pushed on the stack by the children of the parser, and popped by the parser. In
rule `pair` we simply push this array (the array object, not all its individual items) back on the
stack! It will contain as first item a string (the key) and as second item a JSON value (the value
mapped to the key).

`with_string` takes a function of three parameters. `xs` is as discussed previously, `p` is an
instance of [`Parse`] (always unused in this grammar) and `str`, which is the string matched by the
parser. The role of `with_string` itself has to do with the Java type system, but basically it
indicates we want to do something using the matched string — and so use a 3-parameters lambda instead
of the single-parameter lambda that `push` normally accepts (details will follow in the next
sub-section).

In rule `number`, we parse the number represented by `str` and push it on the stack. In rule
`string`, we cut off the double quotes and push the resulting string onto the stack. ([*1])

In rule `object`, we do something a bit more technical. `xs` is still the array of items pushed on
stack by sub-parsers, which in this case means that each item is an array pushed by the `pair` rule.
Therefore we can cast `xs` to type `Object[][]` and stream it. We use [`Collectors.toMap`] to isolate
the key and the value from each sub-array. ([*2]) 

Finally, in rule `array`, `collect().as_list(Object.class)` collects all items pushed on the stack
by sub-parsers into a list whose parameter type is given by the class parameter (here it's
`Object`), and pushes that list on the stack.

[`Parse#stack`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/Parse.html#stack
[`Parse`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/Parse.html
[`Collectors.toMap`]: https://docs.oracle.com/javase/8/docs/api/java/util/stream/Collectors.html#toMap-java.util.function.Function-java.util.function.Function-

## Tour of AST Construction Combinators

There are two kinds of DSL combinators that are able to construct instances of `Collect`.

The first kind are defined in class [`CollectBuilder`], an instance of which you can obtain by
calling `rule#collect()`.The [`CollectBuilder`] class lets you customize the behaviour of the
`Collect` parser. We'll touch on this aspect in a bit.

The second kind are defined in class [`rule`] and are reserved for those instances of `Collect`
where it doesn't make sense to customize the behaviour, or shorthands for frequently used
combinators — currently only [`rule#push`].

We already saw (in the previous section) the most frequently used method, which is [`rule#push`].

`push` takes a [`StackAction.Push`] as parameter, which is a functional interface for the
one-parameter function we described earlier. However, when called by parsers such as [`Collect`],
all instances of [`StackAction`] can actually access more data (in particular, the [`Parse`]
object). To actually make use of this data however, you either need to make a class implementing the
interface, or to use one of its sub-interfaces which enable using more complex lambdas:
[`StackAction.PushWithParse`] (takes a parse and `xs`), [`StackAction.PushWithString`] (takes a
parse, `xs`, and the matched string — for parses whose input is a string) and
[`StackAction.PushWithList`] (takes a parse, `xs`, and the matched list — for parses whose input is
a list of objects).

The issue is that due to java type's system, you can't just write `parser.push((p,xs,str) ->
...)`. Java expects a `Push` and cannot figure out you are passing a `PushWithString`. One
solution is casting: `parser.push(StackAction.PushWithString) (p,xs,str) -> ...)`, but since that
is pretty ugly, we supply the functions [`with_parse`], [`with_string`] and [`with_list`] to hint
the compiler. Then you can write: `parser.push(with_string((p,xs,str) -> ...))`.

`push` is also available from [`CollectBuilder#push`] — `parser.push(...)` is
really a shorthand for `parser.collect().push(...)`

Analogous to `push`, there is a family of `action` methods: [`CollectBuilder#action`],
[`CollectBuilder#action_with_string`], [`CollectBuilder#action_with_list`]). The difference with
`push` is that the lambda does not return a value, so nothing is automatically pushed onto the
stack. Pushing on the stack is still possible via [`Parse#stack`] however! These methods have
corresponding types for their parameters: [`StackAction.ActionWithParse`],
[`StackAction.ActionWithString`] and [`StackAction.ActionWithList`].

Note the discrepancy: we write `parser.push(with_string(...))` but `action_with_string(...)`. The
reasons is that, including associativity combinators (which we'll cover in [A6]), there are many DSL
methods that take a `Push`. Making four versions of each would bloat the API, so it's better to use
our "conversion" methods. This is also why there isn't a version of `action` without a `Parse`
parameter: it would rarely be useful.

There are a few other combinators. [`rule#as_val`] pushes its parameter on the stack if the parser
succeeds. [`CollectBuilder#as_list`] takes the array of matched items and turns it into a list with
the given parameter type. The JSON grammar has examples of both these combinators (in rules `value`
and `array`).

Next we have [`CollectBuilder#push_string_match`] and [`CollectBuilder#push_list_match`] which are
simply set up such that `parser.collect().push_string_match()` is equivalent to
`parser.push(with_string((p,xs,str) -> str))` (same idea for `push_list_match`).

Finally, [`rule#maybe`] pushes null on the stack if the underlying parser fails, or leaves the stack
untouched otherwise.  [`rule#as_bool`] pushes `true` or `false` on the stack depending on whether the
underlying parser succeeds or fails (respectively). Both of these parsers always succeeds.

[`CollectBuilder`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/DSL.CollectBuilder.html
[`rule#collect()`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/DSL.rule.html#collect--
[`Collect`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/parsers/Collect.html
[`rule`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/DSL.rule.html
[`rule#push`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/DSL.rule.html#push-norswap.autumn.StackAction.Push-
[`CollectBuilder#push`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/DSL.CollectBuilder.html#push-norswap.autumn.StackAction.Push-
[`CollectBuilder#action`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/DSL.CollectBuilder.html#action-norswap.autumn.StackAction.Collect-
[`CollectBuilder#action_with_string`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/DSL.CollectBuilder.html#action_with_string-norswap.autumn.StackAction.CollectWithString-
[`CollectBuilder#action_with_list`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/DSL.CollectBuilder.html#action_with_list-norswap.autumn.StackAction.CollectWithList- 
[`rule#maybe`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/DSL.rule.html#maybe--
[`rule#as_bool`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/DSL.rule.html#as_bool-- 
[`rule#as_val`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/DSL.rule.html#as_val-java.lang.Object- 
[`CollectBuilder#as_list`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/DSL.CollectBuilder.html#as_list-java.lang.Class- 
[`CollectBuilder#push_string_match`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/DSL.CollectBuilder.html#push_string_match--
[`CollectBuilder#push_list_match`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/DSL.CollectBuilder.html#push_list_match--
[`StackAction.Push`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/StackAction.Push.html
[`StackAction.PushWithParse`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/StackAction.PushWithParse.html
[`StackAction.PushWithString`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/StackAction.PushWithString.html
[`StackAction.PushWithList`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/StackAction.PushWithList.html
[`with_parse`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#with_parse-norswap.autumn.StackAction.PushWithParse-
[`with_string`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#with_string-norswap.autumn.StackAction.PushWithString-
[`with_list`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#with_list-norswap.autumn.StackAction.PushWithList-
[`StackAction.ActionWithParse`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/StackAction.ActionWithParse.html
[`StackAction.ActionWithString`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/StackAction.ActionWithString.html
[`StackAction.ActionWithList`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/StackAction.ActionWithList.html
[`StackAction`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/StackAction.html
[A6]: A6-left-recursion-associativity.md

## AST Building Helpers

The `DSL` class includes some utility functions that are meant for use within the lambdas
passed to AST-building combinators.

The [`$(Object)`] function automatically casts its argument to the type argument of the function,
which can usually be automatically infered.

So for instance you can do `String x = $(xs[0]);` or `function_taking_string($(xs[0]))` instead
of `String x = (String) xs[0];` or `function_taking_string((String) xs[0])`.

Similarly, [`$(Object[], int)`][arrayint] does the same for array access, so `$(xs,0)` is the same as
`$(xs[0])`.

There is also a collection of functions called `list`, which are used to build up lists
from the array supplied to the lambda.

- [`list()`] — returns an empty list after inferring its parameter type in the same manner as `$`.
- [`list(Object...)`] — collects the passed objects (or the passed array) in a list (similar to 
  [`Arrays.asList`]).
- [`list(int, Array)`] and [`list(int, int, Array)`] — allow creating a slice of the
  passed array, by specifying the start index (inclusive) and optionally the end index (exclusive).

[`$(Object)`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#Z:Z:D-java.lang.Object-
[arrayint]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#Z:Z:D-java.lang.Object:A-int-
[`Arrays.asList`]: https://docs.oracle.com/en/java/javase/12/docs/api/java.base/java/util/Arrays.html#asList(T...)
[`list()`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#list--
[`list(Object...)`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#list-java.lang.Object...-
[`list(int, Array)`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#list-int-java.lang.Object:A-
[`list(int, int, Array)`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#list-int-int-java.lang.Object:A-

## Customizing AST Combinators

It's possible to further configure the combinators that can be defined
via [`CollectBuilder`], by using the following methods of that class:

- [`peek_only()`] — items are left on the stack instead of popped.
- [`lookback(int)`] — an additional number of items are taken from the stack to be added to `xs`
  (and if `peek_only()` isn't present, they are also popped)
- [`action_on_fail()`] — the lambda function will be executed even if the underlying parser fails,
  `xs` will be set to `null`.
  
These methods do not return new parsers, they merely act as modifiers, for instance you could write:

```java
my_parser.collect().peek_only().lookback(3).push(xs -> /* ... */);
```

When to use them? Lookback can be useful when you implement "suffix rules". For instance, imagine
you have a language where you can make a macro that expands to a block of code with the syntax
`<code_block> as <macro_name>`, e.g. `{ print("hello") } as hello_world`.  

Further, imagine that you have three different kinds of block of codes, parsed by rules `block1`,
`block2` and `block3`, which each push a node on the value stack. Finally, macro
definitions may appear anywhere that these block may appear.

The best way to define the syntax of code blocks and macro definitions is then the following:

```java
rule macro_def_suffix =
    seq("as", identifier)
    .lookback(1)
    .push(xs -> new MacroDefinition($(xs,1) /* identifier */, $(xs,0) /* code block */);
    
rule blocks =
    seq(choice(block1, block2, block3), macro_def_suffix.opt());
```

The macro definition suffix does not have to be repeated for every kind of code block, and we don't
have to re-parse a code block in case it turns out to be part of a macro definition.

Regarding `action_on_fail`, it is useful for cases where you want to push something on the stack
regardless of the outcome of the parser — although you probably want to push something different in
each of those scenarios.

As for `peek_only`, it is useful when you want to extend the information available on the stack
rather than aggregate it. For instance, you could use it to add a virtual item (not corresponding to
any specific syntactic construct) at the end of a sequence.

[`peek_only()`]:  https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/DSL.CollectBuilder.html#peek_only--
[`lookback(int)`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/DSL.CollectBuilder.html#lookback-int-
[`action_on_fail()`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/DSL.CollectBuilder.html#action_on_fail--

## Value Stack as Context

As we've seen in [A3. How Autumn Works](A3-how-autumn-works.md), Autumn parsers may backtrack,
meaning we "rewind" the output they've matched so that we can try an alternative.

You may legitimately wonder what happens to our value stack when such backtracking occurs.

The simple answer is that, if you use the combinators presented above, "it just works". The value
stack isn't polluted by nodes pushed by parsers that have been backtracked over.

The more complicated answer is that the value stack is an example of *context* ([*3]), which we'll
learn about in [B3. Context-Sensititive (Stateful) Parsing](B3-context-sensitive.md).

In particular, the value stack is an instance of [`SideEffectingArrayStack`] (a class you may
yourself use), some operations of which log their changes so that they may be undone upon
backtracking.

[`SideEffectingArrayStack`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/SideEffectingArrayStack.html

---
**Footnotes**

[*1]: #footnote1 
<h6 id="footnote1" display=none;></h6>

(*1) Beware that this truncated string isn't really the represented string. It may still contain
escaped characters (e.g. '\n') that haven't been processed. Java doesn't really provide a nice
one-liner for that case, but you can take inspiration from [this method] which unescapes Java
strings.

[this method]: https://github.com/norswap/autumn/blob/ff061e49bb1bf14924d27a543551faf6dfb63b26/src/norswap/lang/java/LexUtils.java#L199-L255

[*2]: #footnote2
<h6 id="footnote2" display=none;></h6>

(*2) We cast the key to `String` so that the maps will have the proper type. However, because of
[type erasure], this isn't actually necessary in this case. Still, we're being good citizens and
expliciting the type, which will also make IDEs and linters happy.

[type erasure]: https://en.wikipedia.org/wiki/Type_erasure

[*3]: #footnote3
<h6 id="footnote3" display=none;></h6>

(*3) More accurately, the value stack is an example of *state*, since one does not typically make
parsing decisions based on the value stack, which is the defining characteristic of *context* —
although, in Autumn, that is fully possible and supported. 