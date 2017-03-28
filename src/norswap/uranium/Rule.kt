package norswap.uranium

/**
 * A reactive rule, i.e. a [NodeVisitor] whose [invoke] method registers a [RuleReaction] with a
 * [Reactor].
 *
 * In particular, given a domain node, the rule defines [consumed] attributes and [provided]
 * attributes, as well as how to derived the later ([compute]). It is a blueprint for [Reaction]s,
 * that are creating when visiting an AST.
 */
abstract class Rule <N: Node>: AbstractNodeVisitor<N>()
{
    // ---------------------------------------------------------------------------------------------

    override fun visit (node: N, begin: Boolean)
    {
        if (begin)   return

        val reaction = Reaction(node) {
            _consumed = consumed(node)
            _provided = provided(node)
            _trigger  = { compute() }
        }

        if (reaction.consumed.isEmpty()) reactor.enqueue(reaction)
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Given a domain node, returns the attributes required for a [RuleReaction] create by
     * this rule to trigger.
     *
     * The default implementation returns an empty list.
     */
    open fun consumed (node: N) = emptyList<Attribute>()

    // ---------------------------------------------------------------------------------------------

    /**
     * Given a domain node, returns the attributes provided by a [RuleReaction] created by this
     * rule.
     */
    abstract fun provided (node: N): List<Attribute>

    // ---------------------------------------------------------------------------------------------

    /**
     * Derive the provided attributes: implementation for [RuleReaction.trigger].
     */
    abstract fun Reaction<N>.compute()

    // ---------------------------------------------------------------------------------------------

    /**
     * An utility function for reporting errors using an [ErrorConstructor].
     */
    inline fun Reaction<N>.report (mk: ErrorConstructor): Unit
        = reactor.register_error(mk(this, node))

    // ---------------------------------------------------------------------------------------------
}