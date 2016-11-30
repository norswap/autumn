package norswap.utils

// =================================================================================================

/**
 * Represents a polymorphic operation over an input type [In], producing a value of type [Out].
 *
 * The operation is specialized by type of input, using the [on] method.
 *
 * This offers a solution to the expression problem: one can add new operations (new instances of
 * Polymorphic) and new datatypes (subclasses of In).
 *
 * When adding a new operation, one must supply specializations of that operation for the existing
 * subclasses of In.
 *
 * When adding a new datatype, one must supply specializations of existing operations for that
 * datatype.
 *
 * To mitigate the fact that this is potentially unsafe if one does not supply specialization
 * for a datatype, you
 *
 * This is potentially unsafe: if you forget to supply the specialization for a datatype, then
 * proceed to call the operation on that datatype, an [IllegalStateException] will be thrown.
 * To avoid this, you can supply a default specialization by assigning to the [default] field.
 */
class Polymorphic <In: Any, Out>: (In) -> Out
{
    // ---------------------------------------------------------------------------------------------

    /** See [Polymorphic] */
    companion object {
        /** See [Polymorphic] */
        operator fun <In: Any, Out>
            invoke (init: Polymorphic<In, Out>.() -> Unit): Polymorphic<In, Out>
        {
            val poly = Polymorphic<In, Out>()
            poly.init()
            return poly
        }
    }

    // ---------------------------------------------------------------------------------------------

    var default: ((In) -> Out)? = null

    // ---------------------------------------------------------------------------------------------

    val specializations = HashMap<Class<out In>, (In) -> Out>()

    // ---------------------------------------------------------------------------------------------

    inline fun <reified Case: In> on (noinline specialization: (Case) -> Out)
    {
        @Suppress("UNCHECKED_CAST")
        specializations[Case::class.java] = specialization as (In) -> Out
    }

    // ---------------------------------------------------------------------------------------------

    fun <Case: In> on (klass: Class<Case>, specialization: (Case) -> Out)
    {
        @Suppress("UNCHECKED_CAST")
        specializations[klass] = specialization as (In) -> Out
    }

    // ---------------------------------------------------------------------------------------------

    override fun invoke (input: In): Out
    {
        val proc = specializations[input.javaClass] ?: default

        if (proc == null)
            throw IllegalAccessException("No specialization for class ${input.javaClass}.")
        else
            return proc.invoke(input)
    }
}

// =================================================================================================

/**
 * Visits the visible [input] with the receiver operation, using pre-order.
 */
fun <In: Visitable<In>, Out> Polymorphic<In, Out>.visit_pre(input: In): Out
{
    val out = invoke(input)
    input.children().forEach { visit_pre(it) }
    return out
}

// =================================================================================================

/**
 * Visits the visitable [input] with the receiver operation, using post-order.
 */
fun <In: Visitable<In>, Out> Polymorphic<In, Out>.visit_post(input: In): Out
{
    input.children().forEach { visit_post(it) }
    return invoke(input)
}

// =================================================================================================