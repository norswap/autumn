package norswap.autumn

// -------------------------------------------------------------------------------------------------

/**
 * A side effect is a function that modifies the parse state and returns a function that can undo
 * this modification.
 */
typealias SideEffect = (Grammar) -> UndoSideEffect

// -------------------------------------------------------------------------------------------------

/**
 * A function that reverts a change done by a instance of [SideEffect].
 */
typealias UndoSideEffect = (Grammar) -> Unit

// -------------------------------------------------------------------------------------------------

/**
 * Groups a [SideEffect] that has been applied to the parse state, and the corresponding [UndoSideEffect].
 */
class AppliedSideEffect (val side_effect: SideEffect, val undo: UndoSideEffect)

// -------------------------------------------------------------------------------------------------

/**
 * This helper function simply returns its parameter `undo`.
 *
 * Use it to help inference when returning an [UndoSideEffect] from a [SideEffect].
 * (Inference will usually fail if the returned function does not end with a Unit-valued expression.)
 */
fun undo (undo: UndoSideEffect): UndoSideEffect
    = undo

// -------------------------------------------------------------------------------------------------