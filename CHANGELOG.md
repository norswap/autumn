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