package norswap.uranium
import norswap.utils.Advice1
import norswap.utils.cast
import norswap.utils.maybe_list
import norswap.utils.nth_superclass_targ

// =================================================================================================

/**
 * An advice used to visit nodes within a [Reactor].
 */
interface NodeVisitor<N: Node>: Advice1<Node, Unit>
{
    // ---------------------------------------------------------------------------------------------

    /**
     * Casts [node] to [N] and invokes [visit]. Hence must only be called
     * when [node] has a class in [domain].
     */
    override operator fun invoke (node: Node, begin: Boolean): Unit
        = visit(node.cast<N>(), begin)

    // ---------------------------------------------------------------------------------------------

    /**
     * The actual visit logic. Override this instead of [invoke].
     */
    fun visit (node: N, begin: Boolean)

    // ---------------------------------------------------------------------------------------------

    /**
     * The classes of the nodes that visitor wants to visit.
     */
    val domain: List<Class<out Node>>

    // ---------------------------------------------------------------------------------------------

    /**
     * The reactor that initiated the visit.
     */
    val reactor: Reactor

    // ---------------------------------------------------------------------------------------------
}

// =================================================================================================

/**
 * A [NodeVisitor] that supplies a default implementation for:
 *
 * - [domain], that contains the class of [N] (found via reflection).
 * - [reactor], which is set by the reactor itself
 *
 * The [domain] implementation only works if the instantiated class subtypes a class whose first
 * parameter is [N] (such as [AbstractNodeVisitor] itself).
 *
 * The [reactor] implementation requires visitor to not be shared amongst reactors.
 */
abstract class AbstractNodeVisitor<N: Node>: NodeVisitor<N>
{
    // ---------------------------------------------------------------------------------------------

    override val domain: List<Class<out Node>>
        = maybe_list(nth_superclass_targ(this, 1)).cast()

    // ---------------------------------------------------------------------------------------------

    override lateinit var reactor: Reactor
        internal set

    // ---------------------------------------------------------------------------------------------
}

// =================================================================================================