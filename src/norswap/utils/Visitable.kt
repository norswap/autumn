package norswap.utils

// -------------------------------------------------------------------------------------------------

/**
 * Represents an homogeneous tree of items that can be visited. Here, homogeneous means
 * "with common superclass [Self]".
 *
 * You can visit the tree using the [visit_pre] and [visit_post] methods.
 */
interface Visitable<Self: Visitable<Self>>
{
    fun children(): Sequence<Self>
}

// -------------------------------------------------------------------------------------------------