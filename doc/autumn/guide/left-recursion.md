# Handling Left-Recursion

Left-recursion occurs when a parsers calls itself recursively before consuming any input.

In Autumn, rules can normally not be left-recursive, as this causes an infinite loop:

```kotlin
fun looping(): Boolean {
    looping()
    // ...
}
```

This is an exemple of *direct* left-recursion, because the recursive call occurs within
the parser itself. More insidious is *indirect* left-recursion: when a parser calls another
parser, which ultimately calls the first parser (all the while consuming no input).

In Autumn, naive direct and indirect left-recursion like shown above is illegal.
Nevertheless, there is a mechanism to allow left-recursion to work safely, with minimal overheads.
Most of the time, however, left-recursion can be avoided completely.

### Detection

The best way to catch illegal left-recursion is to [test your grammar properly][gtest].

In theory, detecting left-recursion is possible, but it requires building a graph representing the
grammar (something Autumn can't do currently), and for each parser to specify which of its
sub-parsers may be invoked without consuming any input (something that rather increases the ceremony
involved in defining a new parser).

[gtest]: ../advanced/test.md

### Avoidance

Left-recursion is regularly used to achieved two effects:

- Matchings repetitions of an item.

  `>>` Use [`repeat0`] or [`repeat1`] instead.
  
[`repeat0`]: ../reference/parsers/sequential.md#repeat0
[`repeat1`]: ../reference/parsers/sequential.md#repeat1
  
- Matching a left-associative structure.
    
  `>>` Use [`PrecedenceLeft`] instead.
  
[`PrecedenceLeft`]: TODO

### Safe Left-Recursion

If the alternatives won't do, you can handle left-recursion safely using the [`leftrec`] parser.

[`leftrec`]: ../reference/parsers/leftrec.md#leftrec

The signature is as follows:

    inline fun Grammar.leftrec (crossinline p: Grammar.(self: Parser) -> Boolean)
    
It looks very much like a combinator, except that instead of taking a `Parser` (an alias for
`Grammar.() -> Boolean`) as parameter, the function has an additional `self` parameter.

`self` is a parser, and can be invoked to perform safe recursion:

```kotlin
val safe_leftrec = leftrec { self ->
    self()
    // ...
}
```

The code above will not loop forever.

Read the [reference page][`leftrec`] for more details.