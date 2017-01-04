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