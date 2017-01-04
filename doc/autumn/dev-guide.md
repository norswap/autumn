# Autumn Developer Guide

This page details the file structure of Autumn's source code, and how everything
fits together including the parts that are not user-facing.

**Directory Structure**

```
├─ src/norswap/autumn/ 
│  ├── parsers/
│  ├── test/
│  ├── undoable/
├─ test/norswap/autumn/test/
│  ├── parsers/
```

### [`src/norswap/autumn`](/src/norswap/autumn/)

Generally speaking, holds all source files that do not belong in any of the sub-packages.

- [Grammar.kt](/src/norswap/autumn/Grammar.kt)

  The abstract base class for all Grammars.
  
- [TokenGrammar.kt](/src/norswap/autumn/TokenGrammar.kt)

   The abstract base class for Grammars with tokenization support.
   
- [Parser.kt](/src/norswap/autumn/Parser.kt)
 
   The type alias defining the `Parser` type.
   
- [ParseInput.kt](/src/norswap/autumn/ParseInput.kt)

    Encapsulate some textual input, adding a null terminator, performing tab-expansion,
    and enabling to track (line, column) positions within the input.
    
    All parses run over a `ParseInput`, although one may be automatically constructed
    form a string.
    
- [Conf.kt](/src/norswap/autumn/Conf.kt)
    
    A few global settings. Currently they only affect how `ParseInput` instances are created
    by default.
    
- [Change.kt](/src/norswap/autumn/Change.kt)

    Definitions related to the concept of *parse change*. A parse change is a reversible change to
    the parse state that has to be saved so that backtracking may undo the change. Parse changes
    are also useful for memoization and left-recursion handling.
    
- [Failures.kt](/src/norswap/autumn/Failures.kt)

    Defines the `Failure` type used to report parse errors.
    
    Also includes the definition of all failures that can be emitted by the parsers
    bundled with Autumn.
    
### [`src/norswap/autumh/parsers/`](/src/norswap/autumn/parsers/)

Contains the definition of all parsers bundled with Autumn.

- [Brackets.kt](/src/norswap/autumn/parsers/Brackets.kt)

    Parsers that match bracketed content and comma-separated lists.

- [Chars.kt](/src/norswap/autumn/parsers/Chars.kt)

     Parsers that match at the character level.
     
- [Choice.kt](/src/norswap/autumn/parsers/Choice.kt)

    Defines the `choice` parser that invokes its sub-parsers in order until one matches,
    and the `longest` parser that invokes all its sub-parsers at the same position,
    and retains the result of the one that matched the most input.

- [Leftrec.kt](/src/norswap/autumn/parsers/Leftrec.kt)

    Defines the `leftrec` parser that enables the creation of left-recursive parsers.

- [Lookahead.kt](/src/norswap/autumn/parsers/Lookahead.kt)

    Defines the `ahead` parser that enables lookahead and the `not` parser that succeeds only
    if some lookahead fails.

- [Misc.kt](/src/norswap/autumn/parsers/Misc.kt)

    Miscelleaneous parsers, some of which are related to failure-handling.

- [Operators.kt](/src/norswap/autumn/parsers/Operators.kt)

    Parsers that enable the definition of left- and right-associative "operator levels" in which
    multiple operators sharing the same precedence can be defined. Operator levels can be chained
    in order to enforce precedence.
    
    This is useful for binary operators, but suffix operators can be defined as well. Prefix
    operators do not make sense in the context of operator levels.

- [Sequential.kt](/src/norswap/autumn/parsers/Sequential.kt)

    Parsers to match sub-parsers sequentially. Also enables optional matching.
    Includes `seq`, `opt`, `repeat0`, `repeat1`, ...

- [Stack.kt](/src/norswap/autumn/parsers/Stack.kt)

    Parsers used to manipulate the value stack.

- [Until.kt](/src/norswap/autumn/parsers/Until.kt)

    Defines parser that repeatedly match their first sub-parsers until their second sub-parser
    is matched.
    
### [`src/norswap/autumh/test/`](/src/norswap/autumn/test/)

Contains the file [`GrammarFixture.kt`](/src/norswap/autumn/test/GrammarFixture.kt) which defines an
abstract base class for test classes to extend. The base class provides facilities to easily setup
grammar tests.

See the tests for the Java grammar as an example.

### [`src/norswap/autumn/undoable`](/src/norswap/autumn/undoable/)

Contains the definition of data structures whose modifications automatically create a `Change` and
register it with a grammar supplied at creation time.

The content of [`UndoList.kt`](/src/norswap/autumn/undoable/UndoList.kt) and
[`UndoMap.kt`](/src/norswap/autumn/undoable/) should be obvious.
[`UndoRef.kt`](/src/norswap/autumn/undoable/UndoRef.kt) provides the means to create simple slots
whose mutation generate `Change` objects. These slots may be backed by an existing memory location.
   
# TODO

- interior links
- link Grammar concept
- link tokenization concept
- link parser concept
- link tab expansion concept
- link change concept