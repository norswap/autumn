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
        .push_with_string((p,xs,str) -> Double.parseDouble(str))
        .word();

    public rule string_char = choice(
        seq(set('"', '\\').not(), range('\u0000', '\u001F').not(), any),
        seq(character('\\'), set("\\/bfnrt")),
        seq(str("\\u"), hex_digit, hex_digit, hex_digit, hex_digit));

    public rule string =
        seq(character('"'), string_char.at_least(0), character('"'))
        .push_with_string((p,xs,str) -> str.substring(1, str.length() - 1))
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
        .push((p,xs) -> xs);

    public rule object =
        seq("{", pair.sep(0, ","), "}")
        .push((p,xs) ->
            Arrays.stream((Object[][]) xs).collect(Collectors.toMap(x -> (String) x[0], x -> x[1])));

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

As you can see, there are relatively few changes, namely the additions of combinator calls
`push_with_string`, `push`, `as_val` and `as_list`.

## Basic Principles & Changes Explained

The basic idea is that there exists a *value stack* (accessible via [`Parse#stack`]) on which
parsers can push or pop items (often AST nodes).

Typically, a parser will pop the nodes pushed on the stack by its children, aggregate them into
a bigger node, and push that on the stack.

Rather, it is not the parser itself that will do this, but a new parser — of class [`Collect`] —
created by the AST construction combinators. That parser wraps the parser on which the combinator
was callend and takes care of the AST construction functionality. 

In our JSON example, we can first highlight a few example of parsers that push items on the stack
without popping anything. It's the case of all parsers returned by `as_val`. These parsers, when
they are successful, simply push the parameter of the method on the stack.

Other examples include the two uses of `push_with_string` within the `number` and `string` rules.
The method takes a lambda of three parameters. We'll explain the roles of `p` and `xs` later.
Of interest here is `str`, which is the string matched by the parser. In `number`, we parse the
number represented by this string and push it on the stack. In `string`, we cutoff the double quotes
and push the resulting string onto the stack. ([*1])

Let's now talk about parsers which pop items off the stack. The first example is `push` in rule
`pair`. The lambda parameter `p` designates an instance of [`Parse`]. This may be useful for
advanced use cases, but we never use it in this grammar. The parameter `xs` designates an array of
items pushed on the stack by the children of the parser, and popped by the parser. In `pair` we
simply push this array itself onto the stack! It will contain as first item a string (the key) and
and as second item a JSON value (the value mapped to the key).

In rule `object`, we do something a bit more technical. `xs` is still the array of items pushed on
stack by sub-parsers, which in this case means that each item is an array pushed by the `pair` rule.
Therefore we can cast `xs` to type `Object[][]` and stream it. We use [`Collectors.toMap`] to isolate
the key and the value from each sub-array. ([*2]) 

Finally, in rule `array`, `as_list` collects all items pushed on the stack by sub-parsers into a
list whose parmeter type is given by the class parameter (here it's `Object`), and pushes that list
on the stack.

[`Parse#stack`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/Parse.html#stack
[`Parse`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/Parse.html
[`Collectors.toMap`]: https://docs.oracle.com/javase/8/docs/api/java/util/stream/Collectors.html#toMap-java.util.function.Function-java.util.function.Function-

## Tour of AST Construction Combinators

All of the AST-construction combinators presented here are defined in class [`rule`], so we'll omit
that part from their name. 

We already saw (in the previous section) the most frequently used method, which is [`push`].

`push` admits a couple variants, including [`push_with_string`] which takes a lambda of three
arguments, the third of which is the string matched by the parser. Similarly, in
[`push_with_list`], the third argument is the sublist matched by the parser (when the input is
a list of objects).

Analogous, to `push`, there is a family of `collect` functions: [`collect`],
[`collect_with_string`], [`collect_with_list`]). The difference with `push` is that
the lambda does not return a value, so nothing is automatically pushed onto the stack. (Pushing
on the stack is possible via `p.stack`!)

There are a few other combinators. [`as_val`] pushes its parameter on the stack if the parser succeeds.
[`as_list`] takes the array of matched items and turns it into a list with the given parameter type.
The JSON grammar has exemple of both these combinators (in rules `value` and `array`).

Next we have [`push_string_match`] and [`push_list_match`] which are simply setup such that
`parser.push_string_match()` is equivalent to `parser.push_with_string((p,xs,str) -> str)` (same
idea for `push_list_match`).

Finally, [`maybe`] pushes null on the stack if the underlying parser fails, or leaves the stack
untouched otherwise.  [`as_bool`] pushes `true` or `false` on the stack depending on whether the
underlying parser succeeds or fails (respectively). Both of these parsers always succeeds, hence
they are implicit like [`opt`].

[`Collect`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/parsers/Collect.html
[`rule`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.rule.html
[`push`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.rule.html#push-norswap.autumn.StackAction.Push-
[`push_with_string`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.rule.html#push_with_string-norswap.autumn.StackAction.PushWithString- 
[`push_with_list`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.rule.html#push_with_list-norswap.autumn.StackAction.PushWithList-
[`collect`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.rule.html#collect-norswap.autumn.StackAction.Collect-
[`collect_with_string`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.rule.html#collect_with_string-norswap.autumn.StackAction.CollectWithString-
[`collect_with_list`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.rule.html#collect_with_list-norswap.autumn.StackAction.CollectWithList- 
[`maybe`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.rule.html#maybe--
[`as_bool`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.rule.html#as_bool-- 
[`as_val`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.rule.html#as_val-java.lang.Object- 
[`as_list`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.rule.html#as_list-java.lang.Class- 
[`push_string_match`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-a0be0ec7db-1/javadoc/norswap/autumn/DSL.rule.html#push_string_match--
[`push_list_match`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.rule.html#push_list_match--
[`opt`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.rule.html#opt-- 

## AST Building Helpers

The `DSL` class includes some utility functions that are meant for use within the lambdas
passed to AST-building combinators.

The [`$(Object)`] function automatically casts its argument to the type argument of the function,
which can usually be automatically infered.

So for instance you can do `String x = $(xs[0]);` or `function_taking_string($(xs[0]))` instead
of `String x = (String) xs[0];` or `function_taking_string((String) xs[0])`.

Similarly, [`$(Array, int)`] ([*3]) does the same for array access, so `$(xs,0)` is the same as
`$(xs[0])`.

There is also a collection of functions called `list`, which are used to build up lists
from the array supplied to the lambda.

- [`list()`] — returns an empty list after inferring its parameter type in the same manner as `$`.
- [`list(Object...)`] — collects the passed objects (or the passed array) in a list (similar to 
  [`Arrays.asList`]).
- [`list(int, Array)`] and [`list(int, int, Array)`] — these allow creating a slice of the
  passed array, by specifying the start index (inclusive) and optionally the end index (exclusive).

[`$(Object)`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#Z:Z:D-java.lang.Object-
[`$(Array, int)`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#Z:Z:D-java.lang.Object:A-int-
[`Arrays.asList`]: https://docs.oracle.com/en/java/javase/12/docs/api/java.base/java/util/Arrays.html#asList(T...)
[`list()`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#list--
[`list(Object...)`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#list-java.lang.Object...-
[`list(int, Array)`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#list-int-java.lang.Object:A-
[`list(int, int, Array)`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#list-int-int-java.lang.Object:A-

## Customizing AST Combinators

It's possible to further configure most of the combinators listed above (but not `as_bool`, `as_val`
and `maybe`), by using the following methods from the `rule` class:

- [`peek_only()`] — items are left on the stack instead of popped.
- [`lookback(int)`] — an additional number of items are taken from the stack to be added to `xs`
  (and if `peek_only()` isn't present, they are also popped)
- [`collect_on_fail()`] — the lambda function will be executed even if the underlying parser fails,
  `xs` will be set to `null`.
  
These methods do not return new parsers, they merely act as modifiers, for instance you could write:

```
my_parser.peek_only().lookback(3).push((p,xs) -> /* ... */);
```

When to use them? Lookback can be useful when you implement "suffix rules". For instance imagine
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
    .push((p,xs) -> new MacroDefinition($(xs,1) /* identifier */, $(xs,0) /* code block */);
    
rule blocks =
    seq(choice(block1, block2, block3), macro_def_suffix.opt());
```

The macro definition suffix does not have to be repeated for every kind of code block, and we don't
have to re-parse a code block in case it turns out to be part of a macro definition.

Regarding `collect_on_fail`, it is useful for cases where you want to push something on the stack
regardless of the outcome of the parser — although you probably want to push something different in
each of those scenarios.

As for `peek_only`, it is useful when you want to extend the information available on the stack
rather than aggregate it. For instance, you could use it to add a virtual item (not corresponding to
any specific syntactic construct) at the end of a sequence.

[`peek_only()`]:  https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.rule.html#peek_only--
[`lookback(int)`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.rule.html#lookback-int-
[`collect_on_fail()`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.rule.html#collect_on_fail--

## Value Stack as Context

As we've seen in [A3. How Autumn Works](A3-how-autumn-works.md), Autumn parsers may backtrack,
meaning we "rewind" the output they've matched so that we can try an alternative.

You may legitimately wonder what happens to our value stack when such backtracking occurs.

The simple answer is that, if you use the combinators presented above, "it just works". The value
stack isn't polluted by nodes pushed by parsers that have been backtracked over.

The more complicated answer is that the value stack is an example of *context* ([*4]), which we'll
learn about in [B3. Context-Sensititive (Stateful) Parsing](B3-context-sensitive.md).

In particular, the value stack is an instance of [`SideEffectingArrayStack`] (a class you may
yourself use), some operations of which log their changes so that they may be undone upon
backtracking.

[`SideEffectingArrayStack`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/SideEffectingArrayStack.html

---
**Footnotes**

[*1]: #footnote1 
<h6 id="footnote1" display=none;></h6>

(*1) Beware that this string isn't really the represented string. It may still contain escape (e.g.
'\n') that haven't beend processed. Java doesn't really provide a nice one-liner for that case,
but you can take inspiration from [this method] which unescapes Java strings.

[this method]: https://github.com/norswap/autumn4/blob/ff061e49bb1bf14924d27a543551faf6dfb63b26/src/norswap/lang/java/LexUtils.java#L199-L255

[*2]: #footnote2
<h6 id="footnote2" display=none;></h6>

(*2) We cast the key to `String` so that the maps will have the proper type. However, because of
[type erasure], this isn't actually necessary in this case. Still, being good citizens and
everything.

[type erasure]: https://en.wikipedia.org/wiki/Type_erasure

[*3]: #footnote3
<h6 id="footnote3" display=none;></h6>

(*3) We use `Array` instead of `Object[]` because Markdown, the notation we use for this
documentation, is quite limited.

[*4]: #footnote4
<h6 id="footnote4" display=none;></h6>

(*4) More accurately, the value stack is an example of *state*, since one does not typically make
parsing decisions based on the value stack, which is the defining characteristic of *context* —
although, in Autumn, that is fully possible and supported. 