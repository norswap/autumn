@file:Suppress("PackageDirectoryMismatch")
package norswap.utils.multimap

// -------------------------------------------------------------------------------------------------

typealias MultiMap<K, V>        = Map<K, List<V>>
typealias MutableMultiMap<K, V> = MutableMap<K, ArrayList<V>>
typealias HashMultiMap<K, V>    = HashMap<K, ArrayList<V>>

// -------------------------------------------------------------------------------------------------

/**
 * Returns the value (list) associated with the key, or an empty list (which is not added to the
 * map! -- for that use [get_or_create]).
 */
fun <K, V> MultiMap<K, V>.get_or_empty(k: K): List<V>
    = get(k) ?: emptyList()

// -------------------------------------------------------------------------------------------------

/**
 * If the key doesn't have a value (list) yet, inserts a list with the value, otherwise appends
 * the value to the list.
 */
fun <K, V> MutableMultiMap<K, V>.append (k: K, v: V)
{
    var array = this[k]

    if (array == null) {
        array = ArrayList()
        put(k, array)
    }

    array.add(v)
}

// -------------------------------------------------------------------------------------------------

/**
 * Associates all values in [vs] with the key [k].
 */
fun <K, V> MutableMultiMap<K, V>.append (k: K, vs: Iterable<V>) {
    get_or_create(k).addAll(vs)
}

// -------------------------------------------------------------------------------------------------

/**
 * Returns the value (list) associated to the key, or associate it an empty list and return it.
 */
fun <K, V> MutableMultiMap<K, V>.get_or_create(k: K): ArrayList<V>
    = getOrPut(k) { ArrayList() }

// -------------------------------------------------------------------------------------------------

/**
 * Remove the given key value pair from the map. Returns true if the item was contained in the map.
 */
fun <K, V> MutableMultiMap<K, V>.remove (k: K, v: V): Boolean
    = get(k)?.remove(v) ?: false

// -------------------------------------------------------------------------------------------------

/**
 * Adds all key value pairs from [map] into this multi-map.
 */
fun <K, V> MutableMultiMap<K, V>.putSingles (map: Map<K, V>) {
    map.forEach { k, v -> append(k, v) }
}

// -------------------------------------------------------------------------------------------------

/**
 * Adds the key-value pairs of [other] into this multi-map.
 */
fun <K, V> MutableMultiMap<K, V>.putAll (other: MultiMap<K, V>) {
    other.forEach { k, vs -> append(k, vs) }
}

// -------------------------------------------------------------------------------------------------

/**
 * Add all key value pairs derived from the iterable by [f] to this multi-map.
 */
fun <X, K, V> Iterable<X>.multi_assoc (f: (X) -> Pair<K, V>): HashMultiMap<K, V>
{
    val out = HashMultiMap<K, V>()
    forEach { val (k, v) = f(it) ; out.append(k, v) }
    return out
}

// -------------------------------------------------------------------------------------------------

/**
 * Add all key value pairs derived from the array by [f] to this multi-map.
 */
fun <X, K, V> Array<X>.multi_assoc (f: (X) -> Pair<K, V>): HashMultiMap<K, V>
{
    val out = HashMultiMap<K, V>()
    forEach { val (k, v) = f(it) ; out.append(k, v) }
    return out
}

// -------------------------------------------------------------------------------------------------