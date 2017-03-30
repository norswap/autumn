# Side Effect API Reference

This page documents types and utilities related to side effect tracking. These are implemented in
[SideEffects.kt]. At the end of the page, we also reference few functions that are documented in
the [`Grammar` API Reference].

[SideEffects.kt]: /norswap/autumn/SideEffects.kt
[`Grammar` API Reference]: grammar.md

Most of the concepts are explained in the [Transactionality] and [Handling Side Effects] sections of
the user guide. Be sure to read them.

[Transactionality]: ../guide/2-transactionality.md
[Handling Side Effects]: ../guide/7-side-effects.md

### `SideEffect`

    typealias SideEffect = (Grammar) -> UndoSideEffect

A side effect is a function that modifies the context and returns a function that can undo
this modification.

### `UndoSideEffect`

    typealias UndoSideEffect = (Grammar) -> Unit

A function that reverts a change done by a instance of `SideEffect`

### `AppliedSideEffect`

    class AppliedSideEffect (val side_effect: SideEffect, val undo: UndoSideEffect)

Groups a `SideEffect` that has been applied to the context, and the corresponding `UndoSideEffect`.

### `undo`

    fun undo (undo: UndoSideEffect): UndoSideEffect
    
This helper function simply returns its parameter `undo`.
Use it to help inference when returning an `UndoSideEffect` from a `SideEffect`.

(Inference will usually fail if the returned function does not end with a Unit-valued expression.)

### Usage

- [`apply`] to apply a side effect to the parse state.
- [`diff`] to get a list from of side effects applied between the current state and some older state.
- [`merge`] to merge multiple side effects into the current state.
- [`undo`] to restore some earlier state.

[`apply`]: grammar.md#apply
[`diff`]:  grammar.md#diff
[`merge`]: grammar.md#merge
[`undo`]:  grammar.md#undo