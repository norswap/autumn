package norswap.utils.poly

/**
 * A map from `Class<T>` to `V`.
 *
 * Bindings are inserted with the [bind] methods, accessed with the [for_instance] and [for_class]
 * methods. The methods attempt to return the value associated to the supplied class, or return
 * the [default] value if the binding doesn't exist. The default value can be null, in which
 * case an exceptiton will be thrown instead.
 *
 * If [inheriting] is true and there is no bindings for a given class, the value bound to a
 * superclass can be returned instead.
 */
open class Specialized <T: Any, V: Any> (val inheriting: Boolean = true)
{
    // ---------------------------------------------------------------------------------------------

    /**
     * The default value returned whenever the map contains no binding for a class.
     */
    var default: V? = null

    // ---------------------------------------------------------------------------------------------

    protected val specializations = HashMap<Class<out T>, V>()

    // ---------------------------------------------------------------------------------------------

    /**
     * Inserts a `(klass, value)` pair into the map
     * (erasing the previous binding for the class, if any).
     *
     * The class is provided by the reified type parameter.
     */
    inline fun <reified Case: T> bind (value: V) {
        bind(Case::class.java, value)
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Inserts the `(klass, value)` pair into the map
     * (erasing the previous binding for the class, if any).
     */
    open fun bind (klass: Class<out T>, value: V) {
        specializations[klass] = value
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Like [bind], but only if the type parameter isn't bound already.
     */
    inline fun <reified Case: T> bind_once (value: V) {
        bind_once(Case::class.java, value)
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Like [bind], but only if [klass] isn't bound already.
     */
    @Suppress("NOTHING_TO_INLINE")
    inline fun bind_once (klass: Class<out T>, value: V) {
        if (for_class_raw(klass) == null)
            bind(klass, value)
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Removes the (klass, value) pair for the given class.
     */
    open fun remove (klass: Class<out T>) {
        specializations.remove(klass)
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the value associated with the given class, or null if there is no associated value
     * (does not attempt to return the default value, or to throw an exception).
     */
    open fun for_class_raw (klass: Class<out T>): V?
    {
        if (!inheriting) return specializations[klass]

        var value: V? = null
        var c: Class<*>? = klass
        while (value == null && c != null) {
            value = specializations[c]
            c = c.superclass
        }
        return value
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the value associated with the given class if it exists,
     * or the default value if defined,
     * or throws an exception.
     */
    open fun for_class (klass: Class<out T>): V
        = for_class_raw(klass)
        ?: default
        ?: throw IllegalAccessException("No specialization for class $klass.")

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the value associated to the class of [item] if it exists,
     * or the default value if defined,
     * or throws an exception.
     */
    open fun for_instance (item: T): V
        = for_class(item::class.java)

    // ---------------------------------------------------------------------------------------------
}