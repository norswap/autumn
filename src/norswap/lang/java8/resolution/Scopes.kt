package norswap.lang.java8.resolution
import norswap.whimsy.Node
import norswap.whimsy.NodeVisitor

// -------------------------------------------------------------------------------------------------

abstract class Scope
{
    private val map = HashMap<String, Any>()

    abstract val parent: Scope

    fun put (name: String, item: Any) {
        map[name] = item
    }
}

// -------------------------------------------------------------------------------------------------

class ScopeI (override val parent: Scope): Scope()

// -------------------------------------------------------------------------------------------------

object NoScope: Scope()
{
    override val parent = throw Error("Trying to access parent of root scope.")
}

// -------------------------------------------------------------------------------------------------

class PackageScope (val name: String): Scope()
{
    override val parent = NoScope
}

// -------------------------------------------------------------------------------------------------

class ScopeVisitor: NodeVisitor<Node>
{
    var current = PackageScope("(default)")

    override fun visit (node: Node, begin: Boolean)
    {
        TODO()
    }

    override val domain: List<Class<out Node>>
        get() = TODO()
}

// -------------------------------------------------------------------------------------------------

