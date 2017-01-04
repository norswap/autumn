package norswap.utils

/**
 * A variant of [Specialized] that is inheritance-aware.
 *
 * If there is no bindings for a given class, the value for a superclass can be returned instead.
 */
open class InheritingSpecialized<T: Any, V: Any>: Specialized<T, V>()
{
    override fun for_class_raw (klass: Class<out T>): V?
    {
        var value: V? = null
        var c: Class<*>? = klass
        while (value == null && c != null)
        {
            value = specializations[c]
            c = c.superclass
        }
        return value
    }
}