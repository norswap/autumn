package norswap.whimsy
import norswap.utils.append
import java.util.ArrayDeque
import java.util.ArrayList
import java.util.HashMap

/**
 * The reactor orchestrates the attribution of an AST.
 *
 * A reactor usually works in three steps.
 *
 * - [Rule]s are added to the reactor.
 *
 * - An AST is visited through the [visit] method and the rules are matched against its node
 *   in order to create [RuleInstance]s.
 *
 * - The reactor is started through [start], and starts applying ready [RuleInstance]s. At first,
 *   the only ready rule instances are those that do not require the availability of any attribute.
 *   Then, as attributes become available, other instances may become ready. This continues until
 *   no rule instance is ready to fire, at which point either the AST is completely attributed,
 *   or some errors occured, or more information is required from the AST.
 */
class Reactor
{
    // ---------------------------------------------------------------------------------------------

    private val rules = HashMap<Node.Class, ArrayList<Rule<*>>>()

    // ---------------------------------------------------------------------------------------------

    private val queue = ArrayDeque<RuleInstance<*>>()

    // ---------------------------------------------------------------------------------------------

    val errors = ArrayList<ReactorError>()

    // ---------------------------------------------------------------------------------------------

    private val broken = HashMap<Attribute, ReactorError>()

    // ---------------------------------------------------------------------------------------------

    /**
     * Add a rule to the reactor.
     */
    fun add_rule (rule: Rule<*>)
    {
        rule.triggers.forEach { rules.append(it, rule) }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Enqueue a rule instance, which will be applied after all rules that precede it have been
     * applied.
     */
    internal fun enqueue (rule_instance: RuleInstance<*>)
    {
        queue.add(rule_instance)
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Register an error emanating from a rule instance application.
     * This methods records the attributes that cannot be derived due to the error.
     */
    fun report_error (error: ReactorError)
    {
        errors.add(error)
        error.affected.forEach { broken.put(it, error) }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Visit [node], trying to instantiate the rules registered in this reactor as we go.
     */
    fun visit (node: Node)
    {
        var klass: Node.Class? = node.javaClass

        while (klass != null)
        {
            rules[klass]?.forEach { it.instantiate(this, node) }
            @Suppress("UNCHECKED_CAST")
            klass = klass.superclass as Node.Class?
        }

        node.children().forEach { visit(it) }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Start the reactor, applying rule instances from this processor's queue until it is empty.
     */
    fun start()
    {
        while (queue.isNotEmpty())
        {
            queue.remove().apply()
        }
    }

    // ---------------------------------------------------------------------------------------------
}