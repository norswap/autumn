# `UndoList`

    class UndoList<T> (val grammar: Grammar): AbstractList<T>()

An array list implementing the immutable [`List`] interface,
whose mutations cause [`SideEffect`]s to be applied to `grammar`.

Also features stack-like methods.

[`List`]: https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/
[`SideEffect`]: ../side-effects.md#sideeffect
    
    fun peek(): T
    
Looks at the item at the end of the array.
Throws an exception if empty.

    fun push (item: T)

Pushes an item at the end of the array, registering a [`SideEffect`] on `grammar`.

    fun _push (item: T)

Pushes an item at the end of the array, **without** registering a [SideEffect]. Useful when
an array has to be initialized with a part that never changes.

    fun pop(): T
     
Pops an item at the end of the array, registering a [`SideEffect`] on `grammar`.
Throws an exception if empty.

    fun _pop(): T

Pops an item at the end of the array, **without** registering a [SideEffect].
Throws an exception if empty.
     
    operator fun set (i: Int, item: T)
    
Sets the item at index [i] to [item], registering a [`SideEffect`] on `grammar`.