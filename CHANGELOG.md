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