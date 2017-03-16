package norswap.whimsy

// =================================================================================================

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
abstract class Reaction <N: Node>
{
    // ---------------------------------------------------------------------------------------------

    /**
     * The node associated with this reaction (see [Reaction]).
     */
    abstract val node: N

    // ---------------------------------------------------------------------------------------------

    /**
     * The rule will be triggered when these attributes are available.
     */
   abstract val consumed: List<Attribute>

    // ---------------------------------------------------------------------------------------------

    /**
     * Once the rule is triggered, these attributes will be made available.
     */
    abstract val provided: List<Attribute>

    // ---------------------------------------------------------------------------------------------

    /**
     * Reactor to which this instance is associated.
     */
    open val reactor: Reactor = ReactorContext.reactor

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

    /**
     * Trigger the reaction in order to derive the supplied attributes.
     */
    abstract fun trigger()

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

// =================================================================================================

/**
 * Create an anonymous subclass of [Reaction] by passing it its members
 * and the implementation of [Reaction.trigger] explicitly.
 */
inline fun <N: Node> Reaction (
    substance: N,
    consumes: List<Attribute>,
    supplies: List<Attribute>,
    reactor: Reactor = ReactorContext.reactor,
    crossinline trigger: () -> Unit)

= object: Reaction<N>()
{
    override val node = substance
    override val consumed = consumes
    override val provided = supplies
    override val reactor = reactor
    override fun trigger() = trigger()
}

// =================================================================================================