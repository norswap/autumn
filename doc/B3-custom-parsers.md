# B3. Writing Custom Parsers

**NOTE: This section is outdated and needs to be rewritten/improved.**

In [A4. Basic Parsers], we presented the basic parsers you'll use frequently to write parsers.

But sometimes you'll need something more specific, and a key point in Autumn's philosophy is that
it lets you write your own parsers. This section examines how to do this.

[A4. Basic Parsers]: A4-basic-parsers.md

To process the information in this section, you'll need a good understanding of Autumn's basics,
especially as covered in [A3. How Autumn Works][A3], and in particular the [`Parser` and `Parse`]
section.

[A3]: A3-how-autumn-works.md
[`Parser` and `Parse`]: A3-how-autumn-works.md#parser-and-parse

## Extending `Parser`

To create a custom parser in Autumn, you extend the [`Parser`] class.

You'll need to implement four methods:

### 1. `protected boolean doparse (Parse parse)`

Javadoc: [`Parser#doparse`]

This is the essential method to implement. As exlained in [A3], this method is called by
[`Parser#parse`] and does the actualy work (`parse` takes care of some book-keeping around the call
to `doparse`).

What must [`Parser#doparse`] do at a minimum? Return true if and only if the parse succeeded, and
increment [`Parse#pos`] to point past the input that was matched in case of success. There is no
need to reset the input in case of failure — `parse` will take care of it.

Very broadly, we can classify parser as *primitive* or *compound*. Compound parsers call sub-parsers
to determine whether they match or not. Primitive parsers, on the other hand, consult the
input directly to determine if a match occurred — using [`Parse#char_at(index)`],
[`Parse#object_at(index)`] or [`Parse#match(index, String)`]

Importantly, parsers must not modify any kind of state outside of what  `Parse` allows, which is
basically: [`Parse#pos`] (cf. above), [`Parse#error`] (covered later: TODO) and *some
operations* on [`Parse#stack`] as explained in the documentation for [`SideEffectingArrayStack`] (in
practice you never rarely need to access [`Parse#stack`] directly, instead, use the facilities
explained in [A5. Creating an Abstract Syntax Tree (AST)]).

It's most certainly an error for `doparse` to modify state inside the parser! For two reasons:

1. The parser may be shared between multiple parses, which shouldn't affect each other.
2. The parser may be backtracked over, in which case the changes it induced should be undone. This
   is something that cannot be guarded against locally!
   
(If these two criteria don't apply, maybe it is safe for you to modify the parser. Exercise extreme
caution!)
   
But parse state is actually useful! And in fact Autumn supports it (as well as context-sensitive
parsing — the result of using parse state to making parse decisions) via a specific API. This will
all be covered in [B1. Context-Sensititive (Stateful) Parsing][B1].

<!-- TODO reorder sections -->

### 2. `public Iterable<Parser> children()`

Javadoc: [`Parser#children`]

This method should return all the sub-parsers that can be *directly* (not transitively) called by
this parser.

Common implementations of this method are:

- `return Collections.emptyList();` — for parsers with no sub-parsers
- `return Collections.singletonList(child);` — for parsers with a single sub-parser
- `return Collections.unmodifiableList(Arrays.asList(children));` — for parsers whose children are stored within an array
- `return Arrays.asList(a, b, c);` — for parsers with sub-parsers `a`, `b`, `c`, ...

(where `Collections` is [`java.util.Collections`] and `Arrays` is [`java.util.Arrays`])

This is notably used by [`ParserWalker`] and some implementations of [`ParserVisitor`]. This
will be explained in [B5. Visiting Parsers & Walking The Parser Graph][B5].

### 3. `public String toStringFull()`

Javadoc: [`Parser#toStringFull`]

This function must return a full string representation of the parser. Here "full" is in opposition
to "just the rule name", where the "rule name" is what parsers stored in a field get assigned when
calling [`Grammar#make_rule_names`].

The traditional [`Parser#toString`] method will always prefer the rule name, but will fall back to
calling this method if the parser has no assigned rule name.

While `toStringFull` shouldn't return its own rule name, it should get a string representation for
its sub-parser using [`Parser#toString`] — which will often yield a rule name. This ensures (a) that
string representations will stay compact, and (b) that no infinite recursion will occur while
generating the string representation (for this to hold, you must have called [`Grammar#make_rule_names`]
or otherwise have assigned rule names for recursive parsers!).

So for the rule `aba` below, `aba.get().toString()` will return `"aba"`, while
`aba.get().toStringFull()` will return `"seq(a, match[b], a)"`.

```java
public rule a = str("a");
public rule aba = seq(a, str("b"), a);
// ...
{ make_rule_names(); }
```

### 4. `public void accept (ParserVisitor visitor)`

Javadoc: [`Parser#accept`]

This is part of the implementation of the [visitor pattern] for parsers, which, in brief, is a way
to add new functionality specialized per-parser.

What you put here will depend on whether you use any visitors and whether you will redistribute
your parsers to third parties.

If none of those are true, you could leave the method empty, though this is not considered good
practice.

Note that by default Autumn runs a well-formedness check ([`WellFormednessChecker`]) on your
grammar, which uses three built-in visitor implementations. However this can be disabled through
[`ParseOptions#well_formedness_check`].

The details of how the visitor parser works and how you should implement this method are covered in
[B5. Visiting Parsers & Walking The Parser Graph][B5], so we will say no more of it here.

[`Parser`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Parser.html 
[`Parse`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Parse.html
[`Parse#char_at(index)`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Parse.html#char_at-int-
[`Parse#object_at(index)`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Parse.html#object_at-int-
[`Parser#doparse`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Parser.html#parse-norswap.autumn.Parse- 
[`Parser#parse`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Parser.html#doparse-norswap.autumn.Parse-
[`Parse#match(index, String)`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Parse.html#match-int-java.lang.String-
[`Parse#pos`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Parse.html#pos
[`Parse#error`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Parse.html#error
[`SideEffectingArrayStack`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/SideEffectingArrayStack.html
[A5. Creating an Abstract Syntax Tree (AST)]: A5-creating-an-ast.md
[B1]: B1-context-sensitive-parsing.md
[B5]: B5-parser-visitors-walkers.md
[`java.util.Collections`]: https://docs.oracle.com/javase/8/docs/api/java/util/Collections.html
[`java.util.Arrays`]: https://docs.oracle.com/javase/8/docs/api/java/util/Arrays.html
[`ParserWalker`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/ParserWalker.html
[`ParserVisitor`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/ParserVisitor.html
[`Parser#toStringFull`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Parser.html#toStringFull--
[`Grammar#make_rule_names`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.html#make_rule_names--
[`Parser#toString`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Parser.html#toString--
[visitor pattern]: https://dzone.com/articles/design-patterns-visitor
[`WellFormednessChecker`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/visitors/WellFormednessChecker.html
[`ParseOptions#well_formedness_check`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/ParseOptions.html#well_formedness_check
[`Parser#accept`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Parser.html#accept-norswap.autumn.ParserVisitor-
[`Parser#children`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Parser.html#children--
[`Parse#stack`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Parse.html#stack

## Examples

If you need examples of parser implementations, it is recommended to consult the sources for the
built-in parsers in the [norswap.autumn.parsers] package. This is especially interesting since these
are implemented without using any tricks that are unavailable to users. No cheating!

[norswap.autumn.parsers]: https://github.com/norswap/autumn/tree/master/src/norswap/autumn/parsers

## Abstract Base Parser Classes

Implementing all the methods can get tedious and somewhat repetitive. To ease the task for simple
cases, we provide three abstract base classes that do some of the work for you:

- [`AbstractForwarding`] — a parser that fully delegates parsing to a single sub-parser. The
custom parser exists so as to make for more meaningful errors/debugging, as well as to enable
specialization using a [`ParserVisitor`] extension.

- [`AbstractWrapper`] — a parser that will the match the same thing as a single sub-parser.
Contrarily to `AbstractForwarding`, the whole parsing is not delegated to the child, so restrictions
(usually context-sensitive ones) can be added.

- [`AbstractPrimitive`] — a parser that does not have any sub-parsers, and so matches directly
  against the input.
 
- [`AbstractChoice`] — a parser whose match will be the same as one of its sub-parsers.

Why four classes instead of one? It has to do with the visitor pattern and the [`Parser#accept`]
method. This is covered in more detail in [B5. Visiting Parsers & Walking The Parser Graph][B5],
but we'll cover the basics here briefly.

When a specific override of [`ParserVisitor#visit`][visit] doesn't exist for your custom parser, the
method gets handled by the `ParserVisitor#visit(Parser)` overload — this overload can't make any
specific assumption about the parser, and so must be as general as possible. Sometimes nothing can
really be done without knowing details about the parser, and so the visitor implementations will be
incomplete or broken when using custom parsers.

As we'll see in [B5], the normal way out of this is to extend [`ParserVisitor`] with an overload
for your custom parser, and to cast to this extension in [`Parser#accept`].

The three abstract classes offer a middle ground, they each enable making specific useful
assumptions about the custom parser in case the specific overload doesn't exist (see the list just
above!).

As for the other methods:

- [`Parser#toStringFull`] will use a `name` string passed to the constructor
  to build a representation such as `<name>(<subparser1.toString(), <subparser2.toString()>, ...`,
  e.g. `my_custom_parser(match([abc]), match([def]))`.
  
- [`Parser#children`] will return the sub-parsers passed to the constructor.

- [`Parser#doparse`] has to be implemented explicitly, except for [`AbstractForwarding`] where it
  will be implement as simply invoking the sub-parser.  

[`AbstractForwarding`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/parsers/AbstractForwarding.htmln
[`AbstractWrapper`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/parsers/AbstractWrapper.html
[`AbstractPrimitive`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/parsers/AbstractPrimitive.html
[`AbstractChoice`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/parsers/AbstractChoice.html
[visit]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/ParserVisitor.html#visit-norswap.autumn.Parser-
