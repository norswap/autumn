package norswap.autumn.undoable
import norswap.autumn.Grammar
import norswap.autumn.SideEffect
import norswap.autumn.undo
import java.util.HashMap

/**
 * A map whose mutations cause [SideEffect]s to be applied to [grammar].
 */
class UndoMap<K, V> (val grammar: Grammar, val map: HashMap<K, V> = HashMap()): Map<K, V> by map
{
    // ---------------------------------------------------------------------------------------------

    /**
     * Associates [value] to [key], returning the old value, if any, and null otherwise.
     */
    fun put (key: K, value: V): V?
    {
        val old = map[key]
        grammar.apply {
            map.put(key, value)
            undo { if (old == null) map.remove(key) else map.put(key, old) }
        }
        return old
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Remove the key and its associated value, if any, from the map.
     * Returns the old value, if any, and null otherwise.
     */
    fun remove (key: K): V?
    {
        val old = map[key]
        grammar.apply {
            map.remove(key)
            undo { if (old != null) map.put(key, old) }
        }
        return old
    }

    // ---------------------------------------------------------------------------------------------
}