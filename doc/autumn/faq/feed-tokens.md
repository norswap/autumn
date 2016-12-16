#FAQ: Can I feed tokens to Autumn instead of text?

This is possible, but hasn't really been tested so far.

The notion of *input position* (a number) is hardwired into the library. However nothing says
that the input position must refer to a position in some text. The only assumption is that the
input is linear: bigger input positions means further in the input.

To make this work, you'll need to subclass `Grammar` and supply your input to it in some way.
When invoking the `Grammar.parse` methods, just supply an empty textual input.

There are a few methods that deal with positions as textual positions, but you can ignore those
safely.

If you try to feed a custom token stream to Autumn, I'd love to [hear from you].

If you find somewhere in the library where I assume that the input position is a textual
representation, please [report it].

[hear from you]: mailto:norswap@gmail.com
[report it]: https://github.com/norswap/whimsy/issues

See also: [Is Autumn scannerless?](/doc/autumn/faq/scannerless.md)