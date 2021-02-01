# B5. Visiting Parsers & Walking The Parser Graph

**NOTE: This section is outdated and needs to be rewritten/improved.**

Parser visitors are an example of [the visitor pattern] and allow you to create new behaviour 
specialized per-parser — just as though you were able to add new abstract methods to [`Parser`] and
the implementation thereof for all existing parser classes.

[the visitor pattern]: https://dzone.com/articles/design-patterns-visitor
[`Parser`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Parser.html

## The Visitor Pattern: Recap

If you don't know or don't remember what the visitor pattern is about, here is a quick recap
of what it looks like in Java (and most object-oriented languages, really).

Note: this sub-section is going to be a condensed version of [a tutorial I wrote on my blog][tuto].

[tuto]: https://norswap.com/java-visitor-pattern

In Java, it's easy to add new *data-type variants*, i.e. new sub-classes. These classes can
naturally override the methods in the super-class. What if you want to add methods to an existing
super-class (that someone else wrote) though? That's the role of the visitor pattern.

Here is a toy example for the visitor pattern, with the explanation to follow:

```java
interface Visitor {
    void visit (A object);
    void visit (B object);
}

interface Base {
    void accept (Visitor visitor);
}

class A implements Base {
    @Override public void accept (Visitor visitor) {
        visitor.visit(this); // calls visit(A)
    }
}

class B implements Base {
    @Override public void accept (Visitor visitor) {
        visitor.visit(this); // calls visit(B)
    }
}

class PrintVisitor implements Visitor
{
    @Override public void visit (A object) {
        System.out.println("printing an A");
    }
    @Override public void visit (B object) {
        System.out.println("printing a B");
    }
}
```

Say you really would like to add a `print()` method to `Base` but you are not the author of
`Base`, `A` and `B` (e.g. they come from a library). Fortunately, the author of `Base`
made it support the visitor pattern.

Instead of writing `new A().print()`, you will write `new A().accept(new PrintVisitor())`.

What happens is that the `accept()` method declared in `Base` must be overriden in all of its
implementations (or subclasses, `Base` could have been a class as well). The role of the overriden
method is to redirect the execution to the correct `Visitor#visit` overload. This is possible (and
type-safe) because the static  type of `this` corresponds to its dynamic type (`A` or `B`) in the
overriden method.

### Adding New Classes

The previous sub-section dealt with the traditional visitor pattern. But we need to go further — we
want this to keep working even if we add new implementations of `Base`. The problem is that
there won't be a corresponding overload in `Visitor`!

Behold, a solution:

```java
interface VisitorC extends Visitor {
    void visit (C object);
}

class C extends Base {
    @Override public void accept (Visitor visitor) {
        ((VisitorC) visitor).visit(this);
    }
}

class PrintVisitorC extends PrintVisitor implements VisitorC {
    @Override public void visit (C object) {
        System.out.println("printing a C");
    }
}
```

And now you can do `new C().accept(new PrintVisitorC())` — but also `new A().accept(new
PrintVisitorC())`!

Note the solution is not entirely typesafe because of the cast: `new C().accept(new
PrintVisitor())` does typecheck but fails with a `ClassCastException`. This is a small trade-off
it is preferable to accept. ([*1])

### Composing Independent Extensions

So far so good, but let's go even further. Assume two developer independently develop new
implementations of `Base`: class `C` from above, and class `D` (same idea as class `C` but using "D"
instead of "C" ).

You want to have a single visitor you can use for all `Base` instances: maybe they are mixed
in a list, for instance.

The current design poses a problem, because what we'd really like to do is:

```java
class PrintVisitorCD extends PrintVisitorC, PrintVisitorD {}
```

But Java doesn't support multiple inheritance...

Fortunately, with default methods, we can approach that — assuming all the developers involved
follow a couple of guidelines. Here is what the retooled code would look like:

```java
interface _PrintVisitor extends Visitor
{
    @Override default void visit (A object) {
        System.out.println("printing an A");
    }
    @Override default void visit (B object) {
        System.out.println("printing a B");
    }
}

class PrintVisitor implements  _PrintVisitor {}

interface _PrintVisitorC extends _PrintVisitor, VisitorC {
    @Override default void visit (C object) {
        System.out.println("printing a C");
    }
}

class PrintVisitorC implements _PrintVisitorC {}

interface _PrintVisitorD extends _PrintVisitor, VisitorD {
    @Override default void visit (D object) {
        System.out.println("printing a D");
    }
}

class PrintVisitorD implements _PrintVisitorD {}
```

And now you can just do:

```java
class PrintVisitorCD implements _PrintVisitorC, _PrintVisitorD {}
```

In principle, the original `PrintVisitor` doesn't need to be separated in a class + interface,
although this changes the solution slightly. I think it's better to do it, it makes everything
more regular.

### Handling Extra Data

Currently, our visit methods can't manipulate extra data besides the `Base` instance.

Here is how an operation implemented by a visitor can "take a parameter" and "return" a value. We'll
keep the scenario with two independent C and D extensions.

```java
// Visitor with a parameter (base) and a result (result)

interface _AddRankVisitor extends Visitor
{
    int base();
    int result();
    void set_result (int result);

    @Override default void visit (A object) { set_result(base() + 1); }
    @Override default void visit (B object) { set_result(base() + 2); }
}

static class AddRankVisitor implements _AddRankVisitor
{
    private final int base;
    private int result;

    AddRankVisitor (int base) { this.base = base; }
    @Override public int base () { return base; }
    @Override public int result () { return result; }
    @Override public void set_result (int result) { this.result = result; }
}

// C & D extensions

interface _Add_RankVisitorC extends _AddRankVisitor, VisitorC {
    @Override default void visit (C object) { set_result(base() + 3); }
}

static class AddRankVisitorC
        extends AddRankVisitor implements _Add_RankVisitorC {
    AddRankVisitorC (int base) { super(base); }
}

interface _Add_RankVisitorD extends _AddRankVisitor, VisitorD {
    @Override default void visit (D object) { set_result(base() + 4); }
}

static class AddRankVisitorD
        extends AddRankVisitor implements _Add_RankVisitorD {
    AddRankVisitorD (int base) { super(base); }
}

// Composing C & D

interface _Add_RankVisitorCD extends _Add_RankVisitorC, _Add_RankVisitorD {}

static class AddRankVisitorCD extends AddRankVisitor implements _Add_RankVisitorCD {
    AddRankVisitorCD (int base) { super(base); }
}

// Calling the visitor

static class Example
{
    static int rank (Base v)
    {
        AddRankVisitor visitor = new AddRankVisitorCD(42);
        v.accept(visitor);
        return visitor.result();
    }
}
```

Notice how we only need to define the underlying data (`base` and `result`) once and then we can
inherit it.

**Full example**: You can consult the final full example (with `A`, `B`, `C`, `D`, `PrintVisitor`
and `AddRankVisitor`) in the [`examples`] source directory: [link to full example].

[`examples`]: /examples 
[link to full example]: /examples/Visitors.java

## Visitors in Autumn

The above pretty much explains how this all works in Autumn.

The visitor class that handles all built-in parsers is called [`ParserVisitor`]. 
Each sub-class of [`Parser`] must implement [`Parser#accept(ParserVisitor)`] as explained above.

An additional subtlety of [`ParserVisitor`] is that it includes a "catch-all" overload
`ParserVisitor#visit(Parser)`. If a custom parser is defined and does not override
[`Parser#accept(ParserVisitor)`] with a cast as shown above, it can still call into the catch-all
overload. This is not recommended but acts as a sort of graceful degradation. Within your visitor
implementations, you should implement this overload by making the most conservative assumptions
possible, whenever possible.

### Creating your Own Visitors

You can implement your own visitors. Use the above example as a guide.
Here is a recap of the guidelines I recommend you to follow:

- "Implement" [`ParserVisitor`] by extending it with an interface whose name starts with `_` which
overrides all of its methods. If you use libraries that provide custom parsers (or have implemented
custom parsers yourself) you should instead extend the interface that additionally comprises
overloads for the custom parsers.

- Also provide a class with the same name as that interface but without the `_`, which implements
the interface — so that the visitor can be instantiated. If the interface requires some data
storage, provide it. If that's the case it's important that the class isn't final, so that
extensions of the visitor to custom parsers can reuse the storage by extending the class.

### Visitors and Custom Parsers

If you implement custom parsers (cf. [B3. Writing Custom Parsers][B3]), you need to take care to
define a new visitor interface and perform the appropriate cast in the
[`Parser#accept(ParserVisitor)`] override (just like `VisitorC` and `C#accept` in our example
above).

If you use any visitor, you'll also need to extend them to support your custom parsers. That's what
we did above with `PrintVisitorC`.

[`ParserVisitor`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/ParserVisitor.html
[B3]: B3-custom-parsers.md
[`Parser#accept(ParserVisitor)`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Parser.html#accept-norswap.autumn.ParserVisitor-

## Built-in Visitors

Autumn comes bundled with three visitor implementations in the [`norswap.autumn.visitors`] package:

- [`_VisitorFirstParsers`] — to get the list of parsers that a parser can directly invoke at the
  same input position.
- [`_VisitorNullable`] — to find out whether a parser can successfully match without consuming any
  input.
- [`_VisitorNullableRepetition`] — to find out whether the parser is a repetition over a nullable
  parser, potentially leading to an infinite loop at parse time.

All these parsers are put to work in [`WellFormednessChecker`], which takes a
[`_VisitorFirstParsers`] and a [`_VisitorNullableRepetition`] ([`_VisitorFirstParsers`] does itself
take a [`_VisitorNullable`]).

A [`WellFormednessChecker`] checks if a grammar is well-formed, i.e. if it does not contain
unguarded (via [`left_recursive`]) left-recursion and nullable repetitions.

[`norswap.autumn.visitors`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/visitors/package-summary.html
[`_VisitorFirstParsers`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/visitors/_VisitorFirstParsers.html
[`_VisitorNullable`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/visitors/_VisitorNullable.html
[`_VisitorNullableRepetition`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/visitors/_VisitorNullableRepetition.html
[`WellFormednessChecker`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/visitors/WellFormednessChecker.html
[`left_recursive`]: A6-left-recursion-associativity.md#a-sub-optimal-solution-explicit-left-recursion-via-seed-growing 

## Parser Walkers

Visitors are all good and well, but often we'd like to pair this kind of per-parser behaviour with a
*walk* of the parse tree — meaning we want to visit a parser, then visit its children, and their
children, etc...

On the face of it, this should be pretty easy, but there are two pitfalls.

First, the parser graph (the graph formed by the relationship between parsers and their children) is
a *graph* meaning it can contain loops (parsers can be recursive!).

Second, since it's a graph, it means the same parser can be reached in multiple ways. Typically we
only want to visit each parser once.

To alleviate this issues and ease writing code that walks the parser graph, Autumn offers the
[`ParserWalker`] class. This is a simply an abstract class whose `work(Parser, State)`
method has to be implemented.

You start a walk with the `walk(Parser)` method. This will call `work(Parser, State)` on every
parser multiple times, with a different instance of `ParserWalker.State` depending on the context:

- `BEFORE` — before walking the children.
- `AFTER` — after walking the children.
- `RECURSE` — when the parser is hit via recursion (the recursion is cut-off so the children won't
  be walked again).
- `VISITED` — if the parser has been walked before.

This guarantees that each parser reachable from the parser passed to `walk` will have one (and
only one) `work` call with the `BEFORE` and `AFTER` states. It can have many `work` calls with
`RECURSE` and `VISITED` however.

A typical implementation of `work(Parse, State)` is guarded by an `if` statement that checks if
the state is the one we're interested in (often `BEFORE` or `AFTER`). It's also possible to define
different behaviour for different states.

Finally, a parser walker is not a visitor, but both can be combined. The class
[`WellFormednessChecker`] is the only built-in walker implementation and uses visitors within its
`work` method.

[`ParserWalker`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/ParserWalker.html

----
**Footnotes**

[*1]: #footnote1 
<h6 id="footnote1" display=none;></h6>

(*1) Type-safe solutions to the problem have been devised, but they come with important downsides.
You can find a detailed discussion of these solutions and their issues compared to the pattern we
advocate [here][exp-prob].

[exp-prob]: TODO