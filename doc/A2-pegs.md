# A2. Parsing Expression Grammars (PEGs)

Parsing Expression Grammars is a *grammar formalism*. It's a way to write grammars that describe
languages. Here is a simple PEG grammar for arithmetic expressions:

    Sum     ::= Product '+' Sum | Product '-' Sum | Product
    Product ::= Number '*' Product | Number '/' Product | Number
    Number  ::= [0-9]+
    
A "number" is one or more digits, a "product" is a number followed by a multplication or division
symbol then by another product. It may also simply be a number. `1`, `1*2` and `1*2/3` are all valid
products. A "sum" is a product followed by an addition or subtraction symbol then by another sum, or
simply a product. `1`, `1*2`, `1+2`, and `1+2*3-4*5` are all valid sums.

PEG grammars are made of *rules* whose left-hand side is a name called a *non-terminal*; and whose
right-hand side consists of a *parsing expression*  made of non-terminals, primitive expressions,
and operators. In the most common scenario, the input the a parser will be a character string.
Hence, primitive expressions will be single characters such as `'a'` or `'0'`.

TODO: this goes more properly in B1

The notation we used for the grammar above is the classical notation you will find used
online and in papers. In Autumn, it looks a bit different:

```java
import norswap.autumn.DSL;
public final class Grammar extends DSL
{
    rule number = range('0', '9').at_least(1);
    rule product = choice(
        seq(number, '*', lazy(() -> this.product)),
        seq(number, '/', lazy(() -> this.product)),
        number);
    rule sum = choice(
        seq(product, '+', lazy(() -> this.sum)),
        seq(product, '-', lazy(() -> this.sum)),
        product);
}
```