# FAQ: Why are items in sequences / choices separated by `||` / `&&` ?

A sequence may look something like this:

    seq { parser1() && parser2() && parser3() }
    
while a choice may look something like this:

    choice { parser1() || parser2() || parser3() }
    
Why do they use this rather unusual syntax? Two reasons:

1. Because the alternative is ugly.
2. Because `seq` and `choice` are special in a way that makes it possible to use this special syntax.

## The Ugly Alternative

    seq ({ parser1() } , { parser2() } , { parser3() })
    choice ({ parser1() } , { parser2() } , { parser3() })
    
You'll notice that's exactly how the syntax of `longest` and `around0` work, for instance.
It's not exactly the prettiest part of the DSL.

## Using Logical Connectives

A sequence invokes its sub-parsers one after the other, until one fails or they all succeed.
If they all succeed, nothing needs to be done. If one fails, the state must be restored to its
initial condition (upon invocation of the sequence parser).

Short-circuit `&&` helps with that: sub-parsers are invoked from left to right until one fails.
And the result of the expression tells us if the sequence was successful and hence whether
we need to restore the state or not.

A choice invokes its sub-parsers at the initial input position, one after the other until
one succeeds. In any case, nothing needs to be done: parser must clean up after themselves if
they fail, and if one succeeds, this is the state we want to be in.

Short-circuit `||` ensures that we invoke sub-parser from left to right until one succeeds.

## Operand Evaluation

`&&` and `||` have a dirty secret: they are the only operators besides `?:` that do not
systematically evaluate all their operands.

If we could create such operators, designing the Autumn DSL would be much easier. Alas it is not
possible and so we have to fall back on delaying evaluation by transforming expression into
anonymous functions.

Controlling evaluation is precisely something macros - and meta-programming in general - excel at.
And as you might know, [Whimsy] is all about meta-programming.

[Whimsy]: https://github.com/norswap/whimsy