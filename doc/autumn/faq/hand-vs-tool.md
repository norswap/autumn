# FAQ: Hand-Written Parsers vs Parsing Tools

## Issues with hand-written parsers

When writing a parser by hand, we typically adopt the "top-down recursive-descent" style.
For instance, we'll write a function to parse a file, which will repeatedly call a function
to parse a file component, which will call functions to match different types of components
(e.g. variable declarations or function declarations), only one of which will match. And so on, and
so forth, all the way down.

While this works reasonably well, this style has a few issues:

- **Lots of repetitions**

  As you write parsers, there are patterns that will pop again and again: matching one thing and
  then another, matching a single thing out of a few possibilities, optionally matching something,
  matching one or more repetitions of something, ...
  
  In parsing libraries, these patterns are often abstracted as *parser combinator*
  (hence *parser combinator libraries*).
  
- **No support for left-recursion**

  Because our parser is just calling functions, left-recursion is not supported. If we are ever caught
  in a loop where a function A calls a function B which itselfs calls A, all without advancing the
  input position, we will loop forever (and run out of stack space).
  
- **Unsafe state handling**

  A hand-written parser can manipulate any state during the parse, and even use this state to
  steer the parse (hence achieving context-sensitive parsing). However, in the presence of
  backtracking, great care has to be taken in order to undo the changes
  applied by the parser invocations being backtracked over.

## Advantages of hand-written parsers

- **Less restrictions / more control than parsing tools**

  Most parsing tools come with restrictions:
  
  - LL/LR/LALR parsers (e.g. yacc) impose restrictions on the grammar to avoid shift-reduce conflicts,
    too much lookahead, etc
  - Most parsers can't handle context-sensitive parsing
  - Many parsers don't let you define how the AST is built
  - etc

- **Efficiency**

  Parser combinator libraries almost always induce quite a bit of overhead.
  This is much less the case with parser generators.
  In any case, hand-written parsers let you optimize to your heart's content, and so can
  potentially always beat parsing tools.
  
- **Easier to debug**

   This one is not obvious, but it is my experience (and that of many other people) that hand-written
   parsers are much easier to debug than parsing tools.
   
   The automatically generated error messages are generally confusing, and you'll often be better
   off with your hand-rolled error reporting. This is not incompetence: the problem is fundamentally
   hard.
   
   Regarding tooling, it is much easier to debug code you wrote than a grammar fed to a tool.
   Something as simple as a stack trace is not always provided, nevermind a debugger.
   There are however a few exceptions. Notably, ANTLR has a great IntelliJ plugin.

## Advantages of parsing tools (in general)

Most advantages of using parsing tools are the 

- **More declarative / clean than hand-written parsers**

    Which would you rather write?

      Decls ::= Prefix Decl+
    
    or
    
      fun parse_decls (ctx: Context): Boolean
      {
          val pos0 = ctx.pos
          if (!parse_prefix())
              return false
          if (!parse_decl()) {
              ctx.pos = pos0
              return false
          }
          while (parse_decl()) ;
          return true
      }

     (This is a nice one: it gets much, much worse.)

- **Abstracts away code that you would have to write anyway**

  This is really the converse of the *lots of repetitions* issue of hand-written parsers.
  Similarly, one can count left-recursion support as an upside of many parsing tools.

## And Autumn in all this?

Autumn attempts to combine the advantages of hand-written parsers and parsing tools, without
incurring the downsides.

- **Flexible**
 
  - Parsers can be written with regular code
  - Sensible support for context-sensitive parsing
  
- **Abstraction & Reuse**

  - Autumn comes bundled with pre-defined parsers and parser-combinators covering the most common
    use cases.
  - Abstractions are provided to work with state, backtracking, memoization, tokenization, ...
  
- **Reasonably efficient**

  Currently 3x slower than ANTLR. I'm working on improving this.
  
- **Left-recursion is supported or even made unecessary**

---

See also: [Why use Autumn?](why.md)