# A2. Parsing Expression Grammars (PEGs)

Parsing Expression Grammars is a *grammar formalism*. It's a way to write grammars that describe
languages. Here is a simple PEG grammar for arithmetic expressions:

    Sum     ::= Product '+' Sum | Product '-' Sum | Product
    Product ::= Number '*' Product | Number '/' Product | Number
    Number  ::= [0-9]+
    
A number is one or more digits, a "product" is a number followed by multplication or division symbol
then by another product. It may also simply be a number. `1`, `1*2` and `1*2/3` are all valid
products. A "sum" is a product followed by an addition or subtraction symbol then by another sum, or
simply a product. `1`, `1*2`, `1+2`, and `1+2*3-4*5` are all valid sums.