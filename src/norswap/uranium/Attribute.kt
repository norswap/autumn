package norswap.uranium

/**
 * A locator for a node attribute as a (node, name) pair.
 */
data class Attribute (val node: Node, val name: String)
{
    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the attribute's value, or throws an exception if the attribute is not defined yet.
     */
    operator fun invoke(): Any? = node[name]

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