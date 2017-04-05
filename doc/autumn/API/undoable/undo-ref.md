# Undo References

## `UndoRef`

    abstract class UndoRef<T>

An instance of this class represents a pseudo-variable whose mutation
causes [`SideEffect`]s to be applied to its `grammar` field.

You can either subclass it, or instantiate it with [`undo_ref`].

[`SideEffect`]: ../side-effects.md#sideeffect
[`undo_ref`]: #undo_ref

### `grammar`

    abstract val grammar: Grammar

The grammar to whom [`SideEffect`] will be applied upon mutation.

### `get`

    abstract fun get(): T

Get the variable's value.

    operator fun invoke() = get()

Syntactic sugar for [get]: `undo_ref()` means `undo_ref.get()`

### `set`

    fun set (item: T)

Set the variable's value, registering a [`SideEffect`] on `grammar`.

    operator fun plusAssign (item: T) = set(item)

Syntactic sugar for `set`: `undo_ref += item` means `undo_ref.set(item)`.

### `_set`

    abstract fun _set (item: T)

Set the variable's value, **without** registering a [`SideEffect`].

---

### `undo_ref`

    inline fun <T> Grammar.undo_ref (
        crossinline getter: () -> T,
        crossinline setter: (T) -> Unit
    ): UndoRef<T>

Creates an [`UndoRef`] from the given getter and setter.

[`UndoRef`]: #undoref

---

### `UndoSlot`

    class UndoSlot<T> (override val grammar: Grammar, var item: T): UndoRef<T>()

An [`UndoRef`] backed by an actual variable (`item`) contained within the object.

---

### `undo_slot`

    fun <T> Grammar.undo_slot (item: T): UndoRef<T>

Syntactic sugar for the [`UndoSlot`] constructor.

[`UndoSlot`]: #undoslot

---