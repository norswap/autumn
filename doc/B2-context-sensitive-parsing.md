# B2. Context-Sensititive (Stateful) Parsing

What is context sensitivity? There are different way to approach that question, but the intuitive
way I like to think about it is that it's about *recalling data derived from previous parsing
decisions*.

A prototypal example would be a pair of parser, the first of which matches a string and saves
it, while the second will match the same as the first string.

````