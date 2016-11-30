package norswap.whimsy
import norswap.utils.Visitable
import norswap.utils.append
import java.util.ArrayList
import java.util.HashMap

/**
 * An AST node, which is a container for attributes.
 */
abstract class Node: Visitable<Node>
{
    // ---------------------------------------------------------------------------------------------

    /**
     * A typealias for the type of subclasses of [Node].
     */
    typealias Class = java.lang.Class<out Node>

    // ---------------------------------------------------------------------------------------------

    internal val attrs     = HashMap<String, Any>()
    internal val consumers = HashMap<String, ArrayList<RuleInstance<*>>>()
    internal val suppliers = HashMap<String, ArrayList<RuleInstance<*>>>()

    // ---------------------------------------------------------------------------------------------

    /**
     * Retrieve the value of the attribute with the given name on this node.
     * If the attribute does not exist, throws an exception.
     */
    operator fun get (name: String): Any
         = attrs[name] ?: throw Exception("Attribute not defined yet")

    // ---------------------------------------------------------------------------------------------

    /**
     * Sets the value of the attribute with the name on this node.
     * If the attribute is already defined, throws an exception.
     *
     * This will trigger any consumer waiting for the definition of this attribute.
     */
    operator fun set (name: String, value: Any)
    {
        val old = attrs.put(name, value)
        if (old != null)
            throw Exception("Redefining attribute value")

        (consumers[name] ?: emptyList<RuleInstance<*>>())
            .forEach { it.satisfy(this(name)) }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Syntactic sugar for [Attribute]`(this, name)`
     */
    operator fun invoke (name: String)
        = Attribute(this, name)

    // ---------------------------------------------------------------------------------------------

    /**
     * Register a rule instance as a supplier for the given attribute name. This indicates that the
     * value for the named attribute can be supplied by that rule instance.
     */
    internal fun add_supplier (name: String, rule: RuleInstance<*>)
    {
        suppliers.append(name, rule)
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Register a rule instance as a consumer for the given attribute name. Whenever the named
     * attribute becomes available, the consumer will be triggered.
     */
    internal fun add_consumer (attr: String, rule: RuleInstance<*>)
    {
        consumers.append(attr, rule)
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the children of this node.
     *
     * The default implementation returns an empty sequence, override to supply the correct
     * behaviour.
     */
    override fun children() = emptySequence<Node>()

    // ---------------------------------------------------------------------------------------------
}