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

    public rule integer =
        choice('0', digit.at_least(1));

    public rule fractional =
        seq('.', digit.at_least(1));

    public rule exponent =
        seq(set("eE"), set("+-").opt(), integer);

    public rule number =
        seq(opt('-'), integer, fractional.opt(), exponent.opt())
        .push($ -> Double.parseDouble($.str()))
        .word();

    public rule string_char = choice(
        seq(set('"', '\\').not(), range('\u0000', '\u001F').not(), any),
        seq('\\', set("\\/bfnrt")),
        seq(str("\\u"), hex_digit, hex_digit, hex_digit, hex_digit));

    public rule string_content = 
        string_char.at_least(0)
        .push_string_match();
    
    public rule string =
        seq('"', string_content, '"')
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
        .push($ -> $);

    public rule object =
        seq("{", pair.sep(0, ","), "}")
        .push($ ->
            Arrays.stream((Object[][]) $.$).collect(Collectors.toMap(x -> (String) x[0], x -> x[1])));

    public rule array =
        seq("[", value.sep(0, ","), "]")
        .as_list(Object.class);

    public rule root = seq(ws, value);

    { make_rule_names(); }

    public ParseResult parse (String input) {
        return Autumn.parse(root, input, ParseOptions.get());
    }
}
```

As you can see, there are relatively few changes, namely the additions of combinator calls `push()`,
`as_val()` and `as_list()`.

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

Then there is the `push` combinator. It is present here in two forms. In its first form, it takes a
function of one parameter. This parameter, which we denote `$`, designates an array of items pushed
on the stack by the children of the parser, and popped by the parser. In rule `pair` we simply push
this array (the array object, not all its individual items) back on the stack! This array will
contain as first item a string (the key) and as second item a JSON value (the value mapped to the
key).

In its second form, `push` takes a function of three parameters. `$` is as discussed previously, `p`
is an instance of [`Parse`] (always unused in this grammar) and `s` is a [`Span`] represented the
input matched by the underlying parser.

In rule `number`, we use the span to retrieve the string matched the parser (`s.get(p.string)`),
convert it into a `double` using a `Double.parseDouble` and push the result onto the stack. 
In rule `string`, we simply push string (without quotes) onto the stack. ([*1])

In rule `object`, we do something a bit more technical. `$` is still the array of items pushed on
stack by sub-parsers, which in this case means that each item is an array pushed by the `pair` rule.
Therefore, we can cast `$` to type `Object[][]` and stream it. We use [`Collectors.toMap`] to
isolate the key and the value from each sub-array. ([*2]) 

Finally, in rule `array`, `collect().as_list(Object.class)` collects all items pushed on the stack
by sub-parsers into a list whose parameter type is given by the class parameter (here it's
`Object`), and pushes that list on the stack.

[`Parse#stack`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/Parse.html#stack
[`Parse`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/Parse.html
[`Collectors.toMap`]: https://docs.oracle.com/javase/8/docs/api/java/util/stream/Collectors.html#toMap-java.util.function.Function-java.util.function.Function-

## Tour of AST Construction Combinators

There are a number of combinators that can be used to read or push objects from the value stack.
Let's start with the most general ones:

- [`rule#push(StackPush, CollectOptions...)`]
- [`rule#push(StackPushWithSpan, CollectOptions...)`]
- [`rule#collect(StackAction, CollectOptions...)`]
- [`rule#collect(StackActionWithSpan, CollectOptions...)`]

We've already seen the two forms of `push`. With the `StackPush` functional interface, you only
get the stack items, with `StackPushWithSpan` you also get the [`Parse`] object and a [`Span`].

The `CollectOptions...` parameter allows you pass zero or more options that will modify
how "collect" parsers (i.e. instances of [`Collect`], which all the parsers we present here are)
work. These options are detailed [in a section below][collect-options]

The two `collect` methods are analogous, but you cannot return a result from the functional
interface they take, so they do not push things on the stack for you.

The [`StackAction`] functional interface actually supplies more parameters which are useful in
some cases. Refer to the [javadoc][`StackAction`] to learn about these.

Next we have a few more specific combinators:

- [`rule#as_val(Object)`]
- [`rule#as_list(Class<?>, CollectOptions...)`]
- [`rule#push_string_match()`]
- [`rule#push_list_match()`]
- [`rule#or_push_null()`]
- [`rule#as_bool()`]

`rule#as_val` pushes its parameter on the stack if the parser succeeds. `rule#as_list` takes the
array of matched items and turns it into a list with the given parameter type. The JSON grammar has
examples of both these combinators (in rules `value` and `array`).

Next we have `rule#push_string_match` and `rule#push_list_match` which are simply set up such
that `parser.push_string_match()` is equivalent to `parser.push($ -> $.str()` (same
idea for `push_list_match`, but using the `p.list` input).

Finally, `rule#or_push_null` pushes null on the stack if the underlying parser fails, or leaves
the stack untouched otherwise.  `rule#as_bool` pushes `true` or `false` on the stack depending on
whether the underlying parser succeeds or fails (respectively). Both of these parsers always
succeeds.

Note that of all these only `rule#as_list` takes options, as it is the only case where it makes
sense to customize the behaviour.

[`DSL`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/DSL.html
[`Collect`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/parsers/Collect.html
[`rule`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/DSL.rule.html
[`Span`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/positions/Span.html
[`StackAction`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/actions/StackAction.html
[`StackActionWithSpan`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/actions/StackActionWithSpan.html
[`StackPush`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/actions/StackPush.html
[`StackPushWithSpan`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/actions/StackPushWithSpan.html

[`rule#push(StackPush, CollectOptions...)`]: TODO
[`rule#push(StackPushWithSpan, CollectOptions...)`]: TODO
[`rule#collect(StackAction, CollectOptions...)`]: TODO
[`rule#collect(StackActionWithSpan, CollectOptions...)`]: TODO

[`rule#as_val(Object)`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/DSL.rule.html#as_val-java.lang.Object-
[`rule#as_list(Class<?>, CollectOptions...)`]: TODO
[`rule#push_string_match()`]: TODO
[`rule#push_list_match()`]: TODO
[`rule#or_push_null()`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/DSL.rule.html#or_push_null--
[`rule#as_bool()`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/DSL.rule.html#as_bool-- 

[A6]: A6-left-recursion-associativity.md
[collect-options]: #customizing-collect-parsers

## AST Building Helpers

The `DSL` class includes some utility functions that are meant for use within the lambdas
passed to AST-building combinators.

The [`$(Object)`] function automatically casts its argument to the type argument of the function,
which can usually be automatically infered.

So for instance you can do `String x = $($[0]);` or `function_taking_string($($[0]))` instead
of `String x = (String) $[0];` or `function_taking_string((String) $[0])`.

Similarly, [`$(Object[], int)`][arrayint] does the same for array access, so `$($,0)` is the same as
`$($[0])`.

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

## Customizing Collect Parsers

Like mentionned before, you can customize the behaviour of [`Collect`] parsers using the following
options (available as static constants or methods in the [`DSL`] class):

- `PEEK_ONLY`: items are left on the stack instead of popped.
- `ACTION_ON_FAIL`: an additional number of items are taken from the stack to be added to `$`
  (and if `peek_only()` isn't present, they are also popped).
- `LOOKBACK(int)`: the lambda function will be executed even if the underlying parser fails,
  `$` will be set to `null`.

When to use them? Lookback can be useful when you implement "suffix rules". For instance, imagine
you have a language where you can make a macro that expands to a block of code with the syntax
`<code_block> as <macro_name>`, e.g. `{ print("hello") } as hello_world`.  

Further, imagine that you have three different kinds of block of codes, parsed by rules `block1`,
`block2` and `block3`, which each push a node on the value stack. Finally, macro
definitions may appear anywhere that these block may appear.

The best way to define the syntax of code blocks and macro definitions is then the following:

```
rule macro_def_suffix =
    seq("as", identifier)
    .lookback(1)
    .push($ -> new MacroDefinition($($,1) /* identifier */, $($,0) /* code block */);
    
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

(*1) Beware that this quoteless string isn't really the represented string. It may still contain
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