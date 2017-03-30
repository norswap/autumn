package norswap.autumn.undoable
import norswap.autumn.SideEffect
import norswap.autumn.Grammar
import norswap.autumn.undo
import norswap.utils.arrayOfSize

/**
 * An array list whose mutations cause [SideEffect]s to be applied to [grammar].
 * Also features stack-like methods.
 */
class UndoList<T> (val grammar: Grammar): AbstractList<T>()
{
    // ---------------------------------------------------------------------------------------------

    private var array = arrayOfSize<T?>(4)
    private var length = 0

    // ---------------------------------------------------------------------------------------------

    override val size: Int
        get() = length

    // ---------------------------------------------------------------------------------------------

    /**
     * Get the item at index [index].
     */
    override operator fun get (index: Int): T {
        if (index > length)
            throw IndexOutOfBoundsException("$index / $length")
        return array[index]!!
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Looks at the item at the end of the array.
     * Throws an exception if empty.
     */
    fun peek(): T
    {
        if (length == 0)
            throw IllegalStateException("empty stack")
        return array[length - 1] as T
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Pushes an item at the end of the array, **without** registering a [SideEffect]. Useful when
     * an array has to be initialized with a part that never changes.
     */
    fun _push (item: T)
    {
        if (array.size == length)
            array = array.copyOf(size * 2)

        array[length++] = item
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Pushes an item at the end of the array.
     */
    fun push (item: T)
    {
        grammar.apply {
            _push(item)
            undo { _pop() }
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Pops an item at the end of the array, **without** registering a [SideEffect].
     * Throws an exception if empty.
     */
    fun _pop(): T
    {
        if (length == 0)
            throw IllegalStateException("empty stack")
        val item = array[--length]
        array[length] = null
        return item as T
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Pops an item at the end of the array.
     * Throws an exception if empty.
     */
    fun pop(): T
    {
        val out = peek()
        grammar.apply {
            val item = _pop()
            undo { _push(item) }
        }
        return out
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Sets the item at index [i] to [item].
     */
    operator fun set (i: Int, item: T)
    {
        val old = get(i)
        grammar.apply {
            array[i] = item
            undo { array[i] = old }
        }
    }

    // ---------------------------------------------------------------------------------------------
}