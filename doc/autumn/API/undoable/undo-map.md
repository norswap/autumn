# `UndoMap`

    class UndoMap<K, V> (val grammar: Grammar, val map: HashMap<K, V> = HashMap()): Map<K, V> by map

An mutable map implementing the immutable [`Map`] interface,
whose mutations cause [`SideEffect`]s to be applied to `grammar`.

[`Map`]: https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/
[`SideEffect`]: ../side-effects.md#sideeffect

### `put`

    fun put (key: K, value: V): V?

Associates `value` to `key`, returning the old value, if any, and null otherwise.
Registers a [`SideEffect`] on `grammar`.

### `remove`

    fun remove (key: K): V?
    
Remove the key and its associated value, if any, from the map.
Returns the old value, if any, and null otherwise.
Registers a [`SideEffect`] on `grammar`.