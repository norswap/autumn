# A5. Creating an Abstract Syntax Tree (AST)

In section [A2. Your First Grammar], we presented a grammar for JSON.

[A2. Your First Grammar]: A2-first-grammar.md

That grammar did recognize valid JSON, but did not build an AST for the parsed JSON input.

In this section, we revisit this grammar, adding in everything it needs to generate a proper AST.
We will then explain the general principles behind AST construction in Autumn.

Without further ado, let's see the new grammar. The intended result is simple: numbers are parsed as
`Double`, strings as `String`, arrays as `List<Object>` and "objects" (dictionaries) as `Map<String,
Object>`. 

```java
import norswap.autumn.Autumn;
import norswap.autumn.Grammar;
import norswap.autumn.ParseOptions;
import norswap.autumn.ParseResult;
import norswap.autumn.positions.LineMapString;
import java.util.Arrays;
import java.util.stream.Collectors;

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
        seq(opt('-'), integer, fractional.opt(), exponent.opt())
        .push($ -> Double.parseDouble($.str()))
        .word();

    public rule string_char = choice(
        seq(set('"', '\\').not(), range('\u0000', '\u001F').not(), any),
        seq('\\', set("\\/bfnrt")),
        seq(str("\\u"), hex_digit, hex_digit, hex_digit, hex_digit));

    public rule string_content =
        string_char.at_least(0)
        .push($ -> $.str());

    public rule string =
        seq('"',string_content , '"')
        .word();

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
        seq(string, COLON, value)
        .push($ -> $.$);

    public rule object =
        seq(LBRACE, pair.sep(0, COMMA), RBRACE)
        .push($ -> Arrays.stream($.$)
            .map(x -> (Object[]) x)
            .collect(Collectors.toMap(x -> (String) x[0], x -> x[1])));

    public rule array =
        seq(LBRACKET, value.sep(0, COMMA), RBRACKET)
        .as_list(Object.class);

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

As you can see, there are relatively few changes, namely the additions of combinator calls `push()`,
`as_val()` and `as_list()`.

## Basic Principles & Changes Explained

The basic idea is that there exists a *value stack* (accessible via [`Parse#stack`]) on which
parsers can push or pop items (often AST nodes).

Typically, a parser will pop the nodes pushed on the stack by its children, aggregate them into
a bigger node, and push that on the stack.

But in fact, it is not the parser itself that will do this, but a new parser — of class [`Collect`]
— created by the AST-construction combinators. That parser wraps the parser on which the combinator
was called and takes care of the AST construction functionality. 

In our JSON example, a simple case is that of the `as_val` combinators. The parsers returned by
those, when they are successful, simply push the parameter of the method on the value stack.

Then there is the `push` combinator. This combinator takes a lambda of one parameter, conventionally
denote `$`. That lambda implements the interface [`StackPush`], and its parameter is of type
[`ActionContext`]. This context makes a lot of information about the parse accessible, refer to the
[Javadoc][`ActionContext`] for full details.

Here in particular, we use `$` in rule `number` to retrieve the string matched by the sequence parser
(`$.str()`). In rule `pair`, `$.$` gives us the array of items pushed onto the value stack
during the invocation of the `seq(string, ":", value)` parser. Specifically, this will be an array
of two items: a string pushed on the stack by rule `string_content`, and a value pushed on the stack
by one of the children of rule `value`.

The lambda passed to `push` returns a value, which is itself pushed onto the value stack. So in rule
`number` we push the double result of the `Double.parseDouble` invocation, while in rule `pair` we
push the array itself (it will be pushed as a single array object, the array items won't be pushed
individually).

In rule `string`, we simply push the quoteless string matched by `string_char.at_least(0)` onto the
stack. ([*1])

In rule `object`, we do something a bit more technical. `$.$` is still the array of items pushed on
stack by sub-parsers, which in this case means that each item is an array pushed by the `pair` rule.
Therefore, we can stream the array, and cast each such item to type `Object[]`. We use
[`Collectors.toMap`] to isolate the key and the value from each sub-array. ([*2])

Finally, in rule `array`, `as_list(Object.class)` collects all items pushed on the stack
by sub-parsers into a list whose parameter type is given by the class parameter (here it's
`Object`), and pushes that list on the stack.

[`Parse#stack`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Parse.html#stack
[`Parse`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Parse.html
[`Collectors.toMap`]: https://docs.oracle.com/javase/8/docs/api/java/util/stream/Collectors.html#toMap-java.util.function.Function-java.util.function.Function-
[`StackConsumer`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/actions/StackConsumer.html
[`StackPush`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/actions/StackPush.html
[`ActionContext`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/actions/ActionContext.html

## Tour of AST Construction Combinators

There are a number of combinators that can be used to read or push objects from the value stack.
Here are the two most general ones:

- [`rule#push(StackPush, CollectOptions...)`]
- [`rule#collect(StackConsumer, CollectOptions...)`]

We've already covered `push` in the last section. `collect` is similar but unlike `StackPush`, the
functional method in `StackConsumer` does not return a value - so nothing is automatically pushed
onto the value stack (it is still possible to manipulate the value stack via `$.push(Object)` and
`$.parse.stack`).

The functional methods of [`StackPush`] and [`StackConsumer`] both expect an [`ActionContext`]. Two
important things you can get of out of the context is the [`Parse`] object ([`ActionContext#parse`])
and a [`Span`] representing the matched input ([`ActionContext#span`]).

The `CollectOptions...` parameter allows you pass zero or more options that will modify
how "collect" parsers (i.e. instances of [`Collect`], which all the parsers we present here are)
work. These options are detailed [in a section below][collect-options].

Next we have a few more specific combinators:

- [`rule#as_val(Object)`]
- [`rule#as_list(Class<?>, CollectOptions...)`]
- [`rule#or_push_null()`]
- [`rule#as_bool()`]

`rule#as_val` pushes its argument on the stack if the parser succeeds. `rule#as_list` takes the
array of collected items and turns it into a list with the given parameter type. The JSON grammar has
examples of both these combinators (in rules `value` and `array`).

`rule#or_push_null` pushes null on the stack if the underlying parser fails, or leaves the stack
untouched otherwise (typically because the underlying parser will push something if it succeeds).
Finally, `rule#as_bool` pushes `true` or `false` on the stack depending on whether the underlying
parser succeeds or fails (respectively). Both of these parsers always succeeds.

Note that of all these only `rule#as_list` takes options, as it is the only case where it makes
sense to customize the behaviour.

[`Grammar`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.html
[`Collect`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/parsers/Collect.html
[`rule`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.rule.html
[`Span`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/positions/Span.html
[`rule#push(StackPush, CollectOptions...)`]:https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.rule.html#push-norswap.autumn.actions.StackPush-norswap.autumn.Grammar.CollectOption...-
[`rule#collect(StackConsumer, CollectOptions...)`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.rule.html#collect-norswap.autumn.actions.StackConsumer-norswap.autumn.Grammar.CollectOption...-
[`ActionContext#parse`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/actions/ActionContext.html#parse--
[`ActionContext#span`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/actions/ActionContext.html#span--

[`rule#as_val(Object)`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.rule.html#as_val-java.lang.Object-
[`rule#as_list(Class<?>, CollectOptions...)`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.rule.html#as_list-java.lang.Class-norswap.autumn.Grammar.CollectOption...-
[`rule#or_push_null()`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.rule.html#or_push_null--
[`rule#as_bool()`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.rule.html#as_bool-- 

[A6]: A6-left-recursion-associativity.md
[collect-options]: #customizing-collect-parsers

## Action Context Helpers

The [`ActionContext`] also supplies a few important helpers which deserve to be mentionned.

First, there is a collection of methods called `$0()`, `$1()`, ..., `$9()`.
These return the corresponding index in the array of items popped from the stack, but additionally
cast the item to the target type.

For instance, the following two rules are equivalent:

```
rule foo = myparser.push($ -> functionExpectingString($.$0()));
rule bar = myparser.push($ -> functionExpectingString((String) $.$[0]));
```

There is also a function called [`$list()`] which returns a `List` view of the popped items. (It's
not to be confused with [`list()`] which returns the sub-list matched by the parser when parsing a
list input.)

[`$list()`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/actions/ActionContext.html#$list--
[`list()`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/actions/ActionContext.html#list--

## Customizing Collect Parsers

Like mentionned before, you can customize the behaviour of [`Collect`] parsers using the following
options (available as static constants or methods in the [`Grammar`] class):

- `PEEK_ONLY`: items are left on the stack instead of popped.
- `LOOKBACK(int)`: an additional number of items are taken from the stack to be added to `$`
  (and if `PEEK_ONLY` isn't present, they are also popped).
- `ACTION_ON_FAIL`: the lambda function will be executed even if the underlying parser fails,
  `$.$` will be set to `null`.

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
    .push($ -> new MacroDefinition($.$1() /* identifier */, $.$0() /* code block */,
        LOOKBACK(1));
    
rule blocks =
    seq(choice(block1, block2, block3), macro_def_suffix.opt());
```

The macro definition suffix does not have to be repeated for every kind of code block, and we don't
have to re-parse a code block in case it turns out to be part of a macro definition.

Regarding `ACTION_ON_FAIL`, it is useful for cases where you want to push something on the stack
regardless of the outcome of the parser — although you probably want to push something different in
each of those scenarios.

As for `PEEK_ONLY`, it is useful when you want to extend the information available on the stack
rather than aggregate it. For instance, you could use it to add a virtual item (not corresponding to
any specific syntactic construct) at the end of a sequence.

## Value Stack as Context

As we've seen in [A3. How Autumn Works](A3-how-autumn-works.md), Autumn parsers may backtrack,
meaning we "rewind" the output they've matched so that we can try an alternative.

You may legitimately wonder what happens to our value stack when such backtracking occurs.

The simple answer is that, if you use the combinators presented above, "it just works". The value
stack isn't polluted by nodes pushed by parsers that have been backtracked over.

The more complicated answer is that the value stack is an example of *context* ([*3]), which we'll
learn about in [B1. Context-Sensititive (Stateful) Parsing](B1-context-sensitive-parsing.md).

In particular, the value stack is an instance of [`SideEffectingArrayStack`] (a class you may
yourself use), some operations of which log their changes so that they may be undone upon
backtracking.

[`SideEffectingArrayStack`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/SideEffectingArrayStack.html

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