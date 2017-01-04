package norswap.whimsy
import norswap.utils.*
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
class Reactor: PolyAdvice<Node, Unit>()
{
    // ---------------------------------------------------------------------------------------------

    /**
     * Maps node classes to rules to instantiate for instances of these classes
     * when visiting a tree.
     *
     * In other terms, maps rule triggers to rules.
     */
    private val rules = HashMultiMap<NodeClass, Rule<*>>()

    // ---------------------------------------------------------------------------------------------

    /**
     * A queue of rules that are ready to be applied, during the execution of the reactor.
     */
    private val queue = ArrayDeque<RuleInstance<*>>()

    // ---------------------------------------------------------------------------------------------

    /**
     * Maps attributes that couldn't be derived to the error that caused things to be so.
     */
    private val broken = HashMap<Attribute, ReactorError>()

    // ---------------------------------------------------------------------------------------------

    /**
     * A list of errors that occured during the execution of the reactor.
     */
    val errors = ArrayList<ReactorError>()

    // ---------------------------------------------------------------------------------------------

    /**
     * Defines an advice to apply around the visit of a particular node class.
     *
     * The advice is automatically augmented with code that instantiates the rules registered
     * for that node class.
     */
    override fun bind (klass: NodeClass, value: Advice1<Node, Unit>)
    {
        // Create the rule set for the node class if it doesn't exist already.
        val ruleset = rules.getOrPut(klass) { ArrayList() }

        // Register the advice, augmented with rule instantiation.
        super.bind(klass) { node, begin ->
            value(node, begin)
            if (!begin) ruleset.forEach(this::add_rule)
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Add a rule to the reactor: this rule will be instantiated for its triggers
     * when visiting a tree.
     */
    fun add_rule (rule: Rule<*>)
    {
        rule.triggers.forEach {
            rules.append(it, rule)

            // Registers an empty advice for the node class,
            // to ensure the rules will be instantiated.
            if (for_class_raw(it) == null)
                on(it) { _,_ -> Unit }
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Enqueue a rule instance, which will be applied after all rules that precede it in the queue
     * have been applied.
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
        visit_around(node)
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