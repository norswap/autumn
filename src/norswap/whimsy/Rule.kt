package norswap.whimsy

// =================================================================================================

// TODO the word "trigger" features twice with different meanings

/**
 * An instance of this class represents an inductive rule, i.e. a
 * `(node, name)* -> (node, name)+` pattern.
 *
 * This pattern represents the idea that given the available of some input attributes, we
 * can derive some output attributes.
 *
 * A rule can be instantiated over input and output nodes to yield a [RuleInstance] object
 * that performs the actual attribute derivation.
 */
abstract class Rule <N: Node>
{
    // ---------------------------------------------------------------------------------------------

    /**
     * A list of node classes on which we want to examine the node tree to determine wether
     * this rule should be instantiated.
     */
    abstract val triggers: List<Node.Class>

    // ---------------------------------------------------------------------------------------------

    /**
     * This method inspects a triggering node and must determine whether we should instantiate
     * the rule for this node (by returning true) or not.
     *
     * The default implementation accepts all triggering nodes.
     */
    open protected fun filter (node: N) = true

    // ---------------------------------------------------------------------------------------------

    /**
     * Given a triggering node that was not filtered out, this method must return
     * all required attributes for the rule instance to be applied.
     *
     * The default implementation returns an empty list.
     */
    open protected fun consumes (node: N) = emptyList<Attribute>()

    // ---------------------------------------------------------------------------------------------

    /**
     * Given a triggering node that was not filtered out, this method must return
     * all attributes that will be supplied once the rule instance has been applied.
     *
     * The default implementation returns an empty list.
     */
    abstract protected fun supplies (node: N): List<Attribute>

    // ---------------------------------------------------------------------------------------------

    /**
     * This method is run at rule instance application time, and can be used to prevent the rule
     * from applying (if it returns false), depending on some condition other than attribute
     * availability.
     *
     * The default implementation lets the rule instance apply.
     *
     * NOTE(norswap): Is this really necessary?
     */
    open fun guard() = true

    // ---------------------------------------------------------------------------------------------

    /**
     * This method performs the actual attribute computation. It **must** set the attributes
     * returned by the [supplies] method.
     */
    abstract fun RuleInstance<N>.compute()

    // ---------------------------------------------------------------------------------------------

    /**
     * Given a triggering node, push it through the process of filtering and potentially
     * instantiation. Notably, this ensures that the generated rule instance will be registered
     * all the nodes from which it consumes attribute, or for which it supplies attributes.
     *
     * If the rule instance does not consume any attributes, enqueues it with the reactor
     * immediately.
     */
    internal fun instantiate (reactor: Reactor, node: Node)
    {
        @Suppress("UNCHECKED_CAST")
        (norswap.utils.proclaim(node as N))

        if (!filter(node)) return

        val consumes = consumes(node)
        val supplies = supplies(node)

        val application = RuleInstance(reactor, this, node, consumes, supplies)

        consumes.forEach { (node, attr) -> node.add_consumer(attr, application) }
        supplies.forEach { (node, attr) -> node.add_supplier(attr, application) }

        if (consumes.isEmpty()) reactor.enqueue(application)
    }
}

// =================================================================================================

/**
 * The instantiation of a [Rule] over a set of nodes.
 */
class RuleInstance <N: Node>
(
    // ---------------------------------------------------------------------------------------------

    /**
     * Reactor to which this instance is associated.
     */
    val reactor: Reactor,

    // ---------------------------------------------------------------------------------------------

    /**
     * The rule being instantiated.
     */
    val rule: Rule<N>,

    // ---------------------------------------------------------------------------------------------

    /**
     * The node that trigerred the instantiation of the rule.
     */
    val trigger: N,

    // ---------------------------------------------------------------------------------------------

    /**
     * The rule will be applied when these attributes are available.
     */
    val consumes: List<Attribute>,

    // ---------------------------------------------------------------------------------------------

    /**
     * Once the rule applies, these attributes will be made available.
     */
    val supplies: List<Attribute>)

{   // ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo

    private var deps_count = 0

    // ---------------------------------------------------------------------------------------------

    /**
     * Apply the rule instance to derives new attributes.
     */
    fun apply()
        = rule.apply { this@RuleInstance.compute() }

    // ---------------------------------------------------------------------------------------------

    /**
     * Signal that the given attribute is available. Once all attributes in [consumes]
     * are available, the rule instance will be enqueued in the reactor.
     */
    fun satisfy (attr: Attribute)
    {
        if (CHECK && !consumes.contains(attr))
            throw Exception("Trying to satisfy a non-dependency")

        if (++ deps_count == consumes.size && rule.guard())
            reactor.enqueue(this)
    }
}

// =================================================================================================