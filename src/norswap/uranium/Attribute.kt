package norswap.uranium

/**
 * A locator for a node attribute as a (node, name) pair.
 */
data class Attribute (val node: Node, val name: String)
{
    // ---------------------------------------------------------------------------------------------

    /**
     * Special value to be used when the value of an attribute is without object
     * (but we cannot assign null because null indicates the attribute wasn't set).
     */
    object None

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the attribute's value, or throws an exception if the attribute is not defined yet.
     */
    operator fun invoke(): Any = node[name]

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the attribute's value, or null if the attribute is not defined yet.
     * (Note the attribute value may be set to null, although that should be rare.)
     */
    fun get(): Any? = node.raw(name)

    // ---------------------------------------------------------------------------------------------

    override fun toString(): String
        = "$node[$name]"

    // ---------------------------------------------------------------------------------------------
}