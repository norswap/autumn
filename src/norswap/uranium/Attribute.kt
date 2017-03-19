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
    operator fun invoke(): Any = node[name]

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the attribute's value, or null if the attribute is not defined yet.
     */
    fun get(): Any? = node.raw(name)

    // ---------------------------------------------------------------------------------------------

    override fun toString(): String
        = "$node[$name]"

    // ---------------------------------------------------------------------------------------------
}