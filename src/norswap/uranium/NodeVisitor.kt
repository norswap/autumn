package norswap.uranium
import norswap.utils.Advice1
import norswap.utils.cast

/**
 * An advice used to visit nodes within a [Reactor].
 */
interface NodeVisitor<N: Node>: Advice1<Node, Unit>
{
    // -------------------------------------------------------------------------------------------------

    /**
     * Casts [node] to [N] and invokes [visit]. Hence must only be called
     * when [node] has a class in [domain].
     */
    override operator fun invoke (node: Node, begin: Boolean): Unit
        = visit(node.cast<N>(), begin)

    // -------------------------------------------------------------------------------------------------

    /**
     * The actual visit logic. Override this instead of [invoke].
     */
    fun visit (node: N, begin: Boolean)

    // -------------------------------------------------------------------------------------------------

    /**
     * The classes of the nodes that visitor wants to visit.
     */
    val domain: List<Class<out Node>>

    // -------------------------------------------------------------------------------------------------
}