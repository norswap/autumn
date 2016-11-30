package norswap.autumn

// -------------------------------------------------------------------------------------------------

/**
 * A change is a function that modifies the context and returns a function that can undo
 * this modification.
 */
typealias Change = (Grammar) -> UndoChange

// -------------------------------------------------------------------------------------------------

/**
 * A function that cancels a change done by a instance of [Change].
 */
typealias UndoChange = (Grammar) -> Unit

// -------------------------------------------------------------------------------------------------

/**
 * Groups a [Change] that has been applied to the context, and the corresponding [UndoChange].
 */
class AppliedChange (val change: Change, val undo: UndoChange)

// -------------------------------------------------------------------------------------------------

/**
 * Use this helper function to help inference when returning an [UndoChange] from a [Change].
 * (Inference will usually fail if the returned function does not end with a Unit-valued expression.)
 */
fun undo (undo: UndoChange)
    = undo

// -------------------------------------------------------------------------------------------------