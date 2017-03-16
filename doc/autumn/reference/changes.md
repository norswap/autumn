# Changes API Reference

TODO more context, reference tutorial

This page documents a few types and utilities related to "changes", i.e. side-effect tracking.

In particular, the types and functions referenced on this page are defined in [Change.kt].
We will also reference a few functions that are actually documented in the [`Grammar` API
Reference].

[Change.kt]: /norswap/autumn/Change.kt
[`Grammar` API Reference]: grammar.md

### `Change`

    typealias Change = (Grammar) -> UndoChange

A change is a function that modifies the context and returns a function that can undo
this modification.

### `UndoChange`

    typealias UndoChange = (Grammar) -> Unit

A function that cancels a change done by a instance of `Change`

### `AppliedChange`

    class AppliedChange (val change: Change, val undo: UndoChange)

Groups a `Change` that has been applied to the context, and the corresponding `UndoChange`.

### `undo`

    fun undo (undo: UndoChange): UndoChange
    
This helper function simply returns its parameter `undo`.
Use it to help inference when returning an `UndoChange` from a `Change`.
(Inference will usually fail if the returned function does not end with a Unit-valued expression.)

### Usage

- [`apply`] to apply a change to the context.
- [`diff`] to get a list from of change between the current state and some older state.
- [`merge`] to merge multiple changes into the current state.
- [`undo`] to restore some earlier state.

[`apply`]: grammar.md#apply
[`diff`]:  grammar.md#diff
[`merge`]: grammar.md#merge
[`undo`]:  grammar.md#undo