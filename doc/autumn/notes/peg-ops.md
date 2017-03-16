# PEG Operators

Here is an explanation all the [Parsing Expression Grammars][PEG] operators, which I lifted from a
[nice introductory paper][redziejowski].

[PEG]: /autumn/peg.md
[redziejowski]: http://www.romanredz.se/papers/FI2007.pdf

- `A / ... / Z`

Ordered choice: Apply expressions `A`, ..., `Z`, in this order, to the text
ahead, until one of them succeeds and possibly consumes some text.
Indicate success if one of expressions succeeded. Otherwise do not
consume any text and indicate failure.

- `A / ... / Z`

Sequence: Apply expressions `A`, ..., `Z`, in this order, to consume
consecutive portions of the text ahead, as long as they succeed. Indicate
success if all succeeded. Otherwise do not consume any text and indicate
failure.

- `&E`

And predicate: Indicate success if expression E matches the text ahead;
otherwise indicate failure. Do not consume any text.

- `!E`

Not predicate: Indicate failure if expression E matches the text ahead;
otherwise indicate success. Do not consume any text.

- `E+`

One or more: Apply expression E repeatedly to match the text ahead, as
long as it succeeds. Consume the matched text (if any) and indicate
success if there was at least one match. Otherwise indicate failure.

- `E*`

Zero or more: Apply expression E repeatedly to match the text ahead, as
long as it succeeds. Consume the matched text (if any). Always indicate
success.

- `E?`

Zero or one: If expression E matches the text ahead, consume it. Always
indicate success.

- `[s]`

Character class: If the character ahead appears in the string s, consume
it and indicate success. Otherwise indicate failure.

- `[c-d]`

Character range: If the character ahead is one from the range c through d, consume it and indicate
success. Otherwise indicate failure.

- `"s"`

String: If the text ahead is the string s, consume it and indicate success.
Otherwise indicate failure.

- `.`

Any character: If there is a character ahead, consume it and indicate
success. Otherwise (that is, at the end of input) indicate failure.