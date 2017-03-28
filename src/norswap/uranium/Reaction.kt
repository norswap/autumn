package norswap.uranium

/**
 * A reaction is a procedure that consumes some attributes ([consumed]) in order to derive other
 * attributes ([provided]).
 *
 * A reaction is typically associated with a particular [node] -- usually providing attributes of
 * this node. It may also be the node that caused the reactions' creation. The reaction keeps
 * a reference to this node that can be used in the implementation of [trigger], the method that
 * computes the provided attributes.
 *
 * The most common form of reaction is [RuleReaction] which is created by [Rule].
 */
class Reaction <N: Node> internal constructor (node: N)
{
    // ---------------------------------------------------------------------------------------------

    constructor (node: N, init: Reaction<N>.() -> Unit): this(node) {
        init()
        // Register the attributes consumed and provided by this reaction with [node].
        consumed.forEach { (node, attr) -> node.add_consumer(attr, this) }
        provided.forEach { (node, attr) -> node.add_supplier(attr, this) }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * The node associated with this reaction.
     */
    val node: N = node

    // ---------------------------------------------------------------------------------------------

    /**
     * The rule will be triggered when these attributes are available.
     */
    val consumed get() = _consumed
    lateinit var _consumed: List<Attribute>

    // ---------------------------------------------------------------------------------------------

    /**
     * Trigger the reaction in order to derive the supplied attributes.
     */
    val trigger get() = _trigger
    lateinit var _trigger: () -> Unit

    // ---------------------------------------------------------------------------------------------

    /**
     * Once the rule is triggered, these attributes will be made available.
     */
    val provided get() = _provided
    lateinit var _provided: List<Attribute>

    // ---------------------------------------------------------------------------------------------

    /**
     * Reactor to which this instance is associated.
     */
    val reactor get() = _reactor
    var _reactor: Reactor = ReactorContext.reactor

    // ---------------------------------------------------------------------------------------------

    /**
     * The reaction that continues this one (if any), or null.
     */
    var continued_in: Reaction<*>? = null

    // ---------------------------------------------------------------------------------------------

    /**
     * The reaction that this reaction is continued from (if any), or null.
     */
    var continued_from: Reaction<*>? = null

    // ---------------------------------------------------------------------------------------------

    private var deps_count = 0

    // ---------------------------------------------------------------------------------------------

    /**
     * Whether the rule has been triggered or not.
     *
     * This is set to true by the reactor before running [trigger], so will be true even if
     * [trigger] throws an exception.
     */
    var triggered = false
        internal set

    // ---------------------------------------------------------------------------------------------

    @Suppress("UNUSED_PARAMETER")
    internal fun satisfy (attr: Attribute)
    {
        if (++ deps_count == consumed.size)
            reactor.enqueue(this)
    }

    // ---------------------------------------------------------------------------------------------

    override fun toString() = "$consumed -> $provided"

    // ---------------------------------------------------------------------------------------------
}