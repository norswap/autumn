package norswap.utils

// -------------------------------------------------------------------------------------------------

typealias MultiMap<K, V>        = Map<K, List<V>>
typealias HashMultiMap<K, V>    = HashMap<K, ArrayList<V>>

// -------------------------------------------------------------------------------------------------

/**
 * If the key doesn't have a value (list) yet, inserts a list with the value, otherwise appends
 * the value to the list.
 */
fun <K, V> HashMultiMap<K, V>.append (k: K, v: V)
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
 * Returns the value (list) associated to the key, or associate it an empty list and return it.
 */
fun <K, V> HashMultiMap<K, V>.get_or_create(k: K): ArrayList<V>
    = getOrPut(k) { ArrayList() }

// -------------------------------------------------------------------------------------------------

/**
 * Returns the value (list) associated with the key, or an empty list (which is not added to the
 * map! -- for that use [get_or_create]).
 */
fun <K, V> HashMultiMap<K, V>.get_or_empty(k: K): List<V>
    = get(k) ?: emptyList()

// -------------------------------------------------------------------------------------------------

/**
 * Remove the given key value pair from the map. Returns true if the item was contained in the map.
 */
fun <K, V> HashMultiMap<K, V>.remove (k: K, v: V): Boolean
    = get(k)?.remove(v) ?: false

// -------------------------------------------------------------------------------------------------