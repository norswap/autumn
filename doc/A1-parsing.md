# A1. What is Parsing?

Given a sequence of symbols as *input* (for instance a character string), parsing is the
combination of two tasks, performed by a program called *parser*.

1. The parser checks the input against a definition of a *language* (usually a *grammar*), to know
   whether the input is a valid *sentence* of the language.
   
   Consider, for instance, the language of simple arithmetic expressions such as `1+2` or `3*4`. A
   parser for this language is able to determine that a string such as `3+4` is a valid arithmetic
   expression (it is a valid sentence of the language of arithmetic expressions).
   
   A degenerate parser that only performs this task is called a *recognizer*.
   
2. Assuming the input is a valid sentence of the language, the parser builds an *abstract syntax
   tree* (AST) that highlights the structure of the input, with regard to the definition of the
   language.
   
   For instance, for the arithmetic expression `3+4`, a parser could build a tree that looks like
   this:
   
   ```
     +
    / \ 
   3   4
   ```
   
   The root of the tree is a node containing the operator `+`, and it has two children, nodes
   containing the numbers `3` and `4`.
   
   Of course, trees can easily become more complex. Consider for instance a tree for the
   expression `1*2+3`
   
   ```
       +
      / \ 
     *   3
    / \
   1   2
   ```
   
And that is what parsing is, in a nutshell.

In the rest of the documentation, we will explore how to define languages, and how to generate
parse trees.