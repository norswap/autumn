## 1.2.0

**Features / Breaking Changes**
- Removed `norswap.autumn.util.TestFixture` in favour of `norswap.utils.TestFixture`.
  The former was a copy of the later, only included because I couldn't refer to the Javadoc of
  the original. This is now fixed, so the copy has to go.
- Renamed `norswap.autumn.TestFixture` to `norswap.autumn.AutumnTestFixture`, to reduce confusion.
- All the `prefixExpect` and `successExpect` methods in `AutumnTestFixture` now expect the value
  stack to have a size of exactly 1.
  
  (This is generally what you want, but in the case where you really need to deal with a larger
  stack, you can always use something like `rule  = ruleToTest.push($ -> $.get(-1));` to only
  preserve the top of the stack. If you want to compare the whole stack, use `rule =
  ruleToTest.push($ -> $.$list());`.)

## 1.1.0

**Features**
- Let `LineMap` carry the input name (file name), and refactor existing functions that used to
  take a separate file name accordingly.
- Made `TestFixture#map` visible and user-assignable.
- Introduce `LineMap#stringWithName(int offset)` and a static version thereof.
- Introduce instance methods of existing static `LineMap#string` methods.
- Added `Span#startString()`
- `Grammar#makeRuleNames()` is now public and now also set names for parsers in parent classes.

**Breaking Changes**
- The constructors for `LineMapString` and `LineMapTokens` take an extra string (the input name)
  as first argument.
- The `filePath` argument was removed from every function formatting function that had it in
  `ParseResult`.
- Renamed `TestFixture#filePath` to `inputName`.
- Remove `Grammar#makeRuleNames(Class)`.

## 1.0.7

**Features**
- `StringChoice` parser to optimize the reserved word / identifier system.

## 1.0.6

**Bugfixes**
-  `TestFixture` used with `rule` would not trigger automatic rule name assignment.
- Fix a bug in `LineMapString#lineSnippet` when using hard tabs.

**Features**
- Define overloads for `LeftExpressionBuilder#{infix, suffix}` and `RightExpressionBuilder#{infix,
  prefix}` which do not take a stack action.
- Added `ActionContext#get(int)` and `ActionContext#pushAll(Object...)`.
- Add a new reserved word / identifier system: `Grammar#id_part`, `Grammar#reserved`,
`Grammar#identifier`.

**Misc**
- Updated to `norswap.utils` v2.1.2

## 1.0.4

- Initial public release.