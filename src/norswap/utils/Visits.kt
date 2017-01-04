package norswap.utils

// =================================================================================================

/**
 * A one-parameter advice.
 *
 * An advice is a function meant to be called both before and after another function.
 * When called before/after, the final boolean parameter will be `true`/`false`.
 *
 * @see visit_around
 */
typealias Advice1 <In, Out>
    = (In, Boolean) -> Out

// -------------------------------------------------------------------------------------------------

/**
 * A reducer is a function meant for reducing trees.
 *
 * It should be called after visiting all the children of a node, supplying the node as first
 * parameter, and an array containing the reduced value for each child as second parameter.
 *
 * @see visit_reduce
 */
typealias Reducer <Node, Out>
    = (Node, Array<Out>) -> Out

// -------------------------------------------------------------------------------------------------

/**
 * A reducer advice is a [Reducer] that behaves like an [Advice1].
 *
 * It should be called before visiting the children of a node, using `null` as second parameter,
 * and after visiting the children, supplying an array of the reduced value for each child
 * as second parameter. The node itself serves as first parameter.
 *
 * @see visit_reduce_around
 */
typealias ReducerAdvice <Node, Out>
    = (Node, Array<Out>?) -> Out

// =================================================================================================

/**
 * Visits the visitable [input] with the receiver operation, using pre-order.
 */
fun <In: Visitable<In>> ((In) -> Any).visit_pre(input: In)
{
    invoke(input)
    input.children().forEach { visit_pre(it) }
}

// -------------------------------------------------------------------------------------------------

/**
 * Visits the visitable [input] with the receiver operation, using post-order.
 */
fun <In: Visitable<In>, Out> ((In) -> Out).visit_post(input: In)
{
    input.children().forEach { visit_post(it) }
    invoke(input)
}

// -------------------------------------------------------------------------------------------------

/**
 * Visits the visitable [input] with the receiver advice, calling it both before and after
 * visiting its children.
 */
fun <In: Visitable<In>, Out> Advice1<In, Out>.visit_around(input: In)
{
    invoke(input, true)
    input.children().forEach { visit_around(it) }
    invoke(input, false)
}

// -------------------------------------------------------------------------------------------------

/**
 * Visits the visitable [input], reducing it to a single value along the way
 * (using the [Reducer] receiver).
 */
fun <In: Visitable<In>, Out> Reducer<In, Out>.visit_reduce(input: In): Out
{
    return invoke(input, input.children().mapToArray { visit_reduce(it) })
}

// -------------------------------------------------------------------------------------------------

/**
 * Visits the visitable [input], reducing it to a single value along the way
 * (using the [ReducerAdvice] receiver).
 */
fun <In: Visitable<In>, Out> ReducerAdvice<In, Out>.visit_reduce_around(input: In): Out
{
    invoke(input, null)
    return invoke(input, input.children().mapToArray { visit_reduce_around(it) })
}

// =================================================================================================