# Side-Effecting Data Structure Implementations

These are pre-defined data structures whose mutations register
[`SideEffect`]s with a [`Grammar`] instance.

[`SideEffect`]: ../side-effects.md#sideeffect
[`Grammar`]: ../grammar.md

- [Undo Reference]( undo-ref.md)
    - [`UndoRef`](  undo-ref.md#undoref)
    - [`undo_ref`]( undo-ref.md#undo_ref)
    - [`UndoSlot`]( undo-ref.md#undoslot)
    - [`undo_slot`](undo-ref.md#undo_slot)
- [`UndoList`](     undo-list.md)
- [`UndoMap`](      undo-ref.md)