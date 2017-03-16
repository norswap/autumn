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
 * Visits the receiver with [visitor], using pre-order.
 */
fun <In: Visitable<In>> In.visit_pre (visitor: (In) -> Any)
{
    visitor(this)
    children().forEach { it.visit_pre(visitor) }
}

// -------------------------------------------------------------------------------------------------

/**
 * Visits the receiver with [visitor], using post-order.
 *
 * Returns the result of the root's visit.
 */
fun <In: Visitable<In>, Out> In.visit_post(visitor: (In) -> Out): Out
{
    children().forEach { it.visit_post(visitor) }
    return visitor(this)
}

// -------------------------------------------------------------------------------------------------

/**
 * Visits the receiver with [advice], calling it both before and after visiting each node's children.
 */
fun <In: Visitable<In>, Out> In.visit_around(advice: Advice1<In, Out>)
{
    advice(this, true)
    children().forEach { it.visit_around(advice) }
    advice(this, false)
}

// -------------------------------------------------------------------------------------------------

/**
 * Visits the receiver, reducing it to a single value using [reducer].
 */
fun <In: Visitable<In>, Out> In.visit_reduce(reducer: Reducer<In, Out>): Out
{
    return reducer(this, children().mapToArray { it.visit_reduce(reducer) })
}

// -------------------------------------------------------------------------------------------------

/**
 * Visits the receiver, reducing to a single value using [reducer], calling it both before
 * and after visiting each node's children.
 */
fun <In: Visitable<In>, Out> In.visit_reduce_around(reducer: ReducerAdvice<In, Out>): Out
{
    reducer(this, null)
    return reducer(this, children().mapToArray { it.visit_reduce_around(reducer) })
}

// =================================================================================================