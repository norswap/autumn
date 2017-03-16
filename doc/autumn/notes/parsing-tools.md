# List of Interesting Parsing Tools

**Disclaimer**: This list is biased. First, towards towards JVM
 languages, and of course, towards work I'm familiar with and find significant.

## "PEG" Combinator libraries

PEG is quoted here because these libraries typically go beyond the [PEG formalism](peg.md) and allow
writing custom combinators.

- [Autumn]: By yours truly. [It's the best, beats the rest!](../faq/why.md) (*coughs*)

- [scala-parser-combinators]: The original Scala parser combinator library. Slow, according to most
  reports.

- [fastparse]: A fast Scala parser combinator library.

- [Papa Carlo]: A Scala combinator library with support for incremental parsing.

- [parboiled2]: A fast Scala parser combinator library.

- [parboiled]: A popular Java/Scala parser combinator library.

- [Mouse]: A very simple simple Java parser combinator library. Clean code and great for
  learning the basics.

- [PEG.js]: Seems to be the most popular Javascript PEG library.

- [ohm]: A Javascript combinator library with a neat [interactive editor]. It is a descendant of
  [OMeta], the first parsing tool to support left-recursion in PEGs.

  [interactive editor]: https://ohmlang.github.io/editor/

- [Bennu]: A Javascript combinator with a lot of gusto.
  "Non-Stop Erotic Javascript Combinatory Parsing" and a heron logo. Yes!
  
- [lpeg]: A popular Lua parser combinator library.

- [nom]: A Rust byte-oriented combinator library. Close to the metal, and probably the fastest
  library on this list.
  
- [parsec]: The first popular parser combinator library, which predates the introduction of PEGs.
  (The idea of parser combinators itself is even much older.) Written in Haskell.
  
- [attoparsec]: A Haskell parser combinator libraries that drops some of parsec's niceties
  (error messages, monad transformer, genericity) in exchange for improved performance and
  incremental parsing ([comparison here]).
  
  [comparison here]: http://stackoverflow.com/a/19213247/298664
  
- [parsimonious]: A Python parser combinator library.

- [PetitParser]: A Smalltalk combinator library. Java and Dart versions [also available].

  [also available]: https://github.com/petitparser
  
## GLL Combinator Library

- [gll-combinators]: The original GLL combinator library.

- [packrattle]: Polished Javascript GLL combinator library.

- [Meerkat]: A GLL combinator library with data-dependent capabilities
  (a form of context-sensitive parsing) which are notably used to provide disambiguation filters,
  and some other capabilities to ease common parsing pain points.

## Parser Generators

- [ANTLR4]: The star of parser generators. Uses Context Free Grammars (CFG). Emits
  Java, C#, Python2|3, JavaScript, Go, C++ and Swift.
  
- [Rats!]: A fast PEG parser generator that performs many optimizations.

- [peg/leg]: A C PEG parser generator.

- [Canopy]: A Javascript PEG parser generators that can emit Javascript, Java, Ruby or Python.

- [waxeye]: A Racket PEG parser generators. Emits C, Java, Javascript, Python, Ruby and Scheme.

- [parsequery]: A scala library that looks like a combinator library, but uses [Lightweight Modular
  Staging] (LMS) behind the scenes to partially evaluate the parser and get a performance increase
  through the elimination of [megamorphic call sites]. Emits Scala.
  
  [Lightweight Modular Staging]: https://scala-lms.github.io/
  [megamorphic call sites]: TODO

- [nearley.js]: A Javascript CFG parser generator using the Earley algorithm. Has a fancy online
 [visualization tool].

[nearley.js]: http://nearley.js.org/
[visualization tool]: https://omrelli.ug/nearley-playground/

## Other

- [Colm]: Actually a language for tree operations, with parsing capabilities. The only other
  system besides Autumn which can handle context-sensitivity safely. Currently being worked into
  a system targeting computer networking.
  
- [DCG]: Definite Clause Grammars is a language feature of logic languages such as Prolog and
  Mercury that enables defining grammars. It is very close to PEG, but integrated within the
  language, and enables context-sensitive parsing.

[Autumn]: https://github.com/norswap/autumn
[scala-parser-combinators]: https://github.com/scala/scala-parser-combinators
[fastparse]: https://github.com/lihaoyi/fastparse
[Papa Carlo]: https://github.com/Eliah-Lakhin/papa-carlo
[parboiled2]: https://github.com/sirthias/parboiled2
[parboiled]: https://github.com/sirthias/parboiled
[Mouse]: http://mousepeg.sourceforge.net/
[PEG.js]: https://github.com/pegjs/pegjs
[ohm]: https://github.com/harc/ohm
[OMeta]: http://www.tinlizzie.org/ometa/
[Bennu]: https://github.com/mattbierner/bennu
[lpeg]: http://www.inf.puc-rio.br/~roberto/lpeg/
[nom]: https://github.com/Geal/nom
[parsec]: https://github.com/aslatter/parsec
[attoparsec]: https://github.com/bos/attoparsec
[parsimonious]: https://github.com/erikrose/parsimonious
[PetitParser]: http://scg.unibe.ch/research/helvetia/petitparser
[gll-combinators]: https://github.com/djspiewak/gll-combinators
[packrattle]: https://github.com/robey/packrattle
[Meerkat]: https://github.com/meerkat-parser/Meerkat
[peg/leg]: https://github.com/gpakosz/peg
[ANTLR4]: https://github.com/antlr/antlr4
[Rats!]: https://cs.nyu.edu/rgrimm/xtc/rats-intro.html
[Canopy]: https://github.com/jcoglan/canopy
[waxeye]: https://github.com/orlandohill/waxeye
[parsequery]: https://github.com/manojo/parsequery
[Colm]: http://www.colm.net/open-source/colm/
[DCG]: https://en.wikipedia.org/wiki/Definite_clause_grammar