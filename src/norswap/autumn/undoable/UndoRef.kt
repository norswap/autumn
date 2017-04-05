package norswap.autumn.undoable
import norswap.autumn.SideEffect
import norswap.autumn.Grammar
import norswap.autumn.undo

// -------------------------------------------------------------------------------------------------

/**
 * An instance of this class represents a pseudo-variable whose mutation
 * causes [SideEffect]s to be applied to [grammar].
 */
abstract class UndoRef<T>
{
    /**
     * The grammar to whom [SideEffect] will be applied upon mutation.
     */
    abstract val grammar: Grammar

    /**
     * Get the variable's value.
     */
    abstract fun get(): T

    /**
     * Set the variable's value, **without** registering a [SideEffect].
     */
    abstract fun _set (item: T)

    /**
     * Set the variable's value.
     */
    fun set (item: T) {
        grammar.apply {
            val old = get()
            _set(item)
            undo { _set(old) }
        }
    }

    /**
     * Syntactic sugar for [set]: `undo_ref += item` means `undo_ref.set(item)`.
     */
    operator fun plusAssign (item: T) = set(item)

    /**
     * Syntactic sugar for [get]: `undo_ref()` means `undo_ref.get()`.
     */
    operator fun invoke() = get()
}

// -------------------------------------------------------------------------------------------------

/**
 * Creates an [UndoRef] from the given getter and setter.
 */
inline fun <T> Grammar.undo_ref (
    crossinline getter: () -> T,
    crossinline setter: (T) -> Unit
): UndoRef<T>
{
    return object: UndoRef<T>() {
        override val grammar = this@undo_ref
        override fun _set(item: T) = setter(item)
        override fun get() = getter()
    }
}

// -------------------------------------------------------------------------------------------------

/**
 * An [UndoRef] backed by an actual variable contained within the object.
 */
class UndoSlot<T> (override val grammar: Grammar, var item: T): UndoRef<T>()
{
    override fun _set (item: T) { this.item = item }
    override fun get() = item
}

// -------------------------------------------------------------------------------------------------

/**
 * Syntactic sugar for the [UndoSlot] constructor.
 */
fun <T> Grammar.undo_slot (item: T): UndoRef<T>
{
    return UndoSlot(this, item)
}

// -------------------------------------------------------------------------------------------------