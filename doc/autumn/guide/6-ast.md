# Building an AST

Being able to recognize languages is good and well, but usually we want to map an input
to a data structure that we can analyze and transform. This data structure is called
an *abstract syntax tree* (AST for short).

An AST is not the same as a *syntax tree*. A syntax tree is a tree that strictly follows the
structure of the grammar, typically with a node per matched rule. An AST, on the other hand,
matches the structure of the language more closely, and doesn't need to match the grammar exactly.
  
Many parsing tools only generate a parse tree and enthrust you the responsibility of transforming it
into an AST. Autumn, on the other hand, doesn't generate a parse tree, but lets you build an AST
during the parse.

## The Value Stack

In Autumn, ASTs are built by pushing and popping values from a *value stack*. Typically, you
push nodes on the stack, then later you pop them, add them as children of another node, then push
that node on the stack.

Let's see an example:

```kotlin
interface Expression
class Integer (val value: Int): Expression
class Product (val left: Expression, val right: Expression): Expression

class ProductGrammar: Grammar() {

    fun integer() = build_str(
        syntax = { repeat1 { digit() } },
        value  = { Integer(it.toInt()) })

    fun product(): Boolean = build(
        syntax = { integer() && string("*") && product() },
        effect = { Product(it(0), it(1)) })

    override fun root() = product()
}
```

This grammar defines the language of products of integers. It is able to match strings such as
`1*2*3`. For this input, it will generate the tree:
 
    Product(Integer(1),
            Product(Integer(2),
                    Integer(3)))
                    
`Expression`, `Integer` and `Product` are the definitions of the node in our AST. We use two new
parser combinators to build our tree: [`build_str`] and [`build`].

[`build_str`]: ../API/parsers/stack.md#build_str
[`build`]: ../API/parsers/stack.md#build

[`build_str`] passes the input string matched by its `syntax` parameter (a parser) to its `value`
parameter (a function). The value returned by `value` is then pushed on the value stack.

[`build`] works a bit differently: it pops all values that were pushed on the stack during the
invocation of its `syntax` parameter (a parser), collects them in an array, and passes this array to
its `effect` parameter (a function). The return value of the function is itself pushed on the
stack.

Note that we access the array items with `it(index)` rather than `it[index]`: the first form calls
`Array#invoke`, which we defined to cast the array item to the target type, when called inside a
`Grammar`. The problem is that many values can be pushed on the value stack, so when getting a value
from the stack, it is only bounded by the `Any` type. In this case we know that the stack values
will always be instances of `Expression` and so we use `it(index)` to avoid the verbosity of
explicit casts.

There are other useful stack-manipulations functions, which are described on the
[Manipulating the Value Stack] reference page.

[Manipulating the Value Stack]: ../API/parsers/stack.md

**The Value Stack and Backtracking**

You might wonder what happens to the values already pushed on the stack when there is backtracking
(backtracking is when a parser fails, causing the next alternative of a choice combinator to be
tried). This is handled automatically by Autumn, which will simply pop the values pushed by the
failing parser from the stack.

This is in fact an example of a more general principle for handling side-effects during parsing,
which we introduced in the [Transactionality] section and will elaborate in [the next section].

[Transactionality]: 2-transactionality.md
[the next section]: 7-side-effects.md