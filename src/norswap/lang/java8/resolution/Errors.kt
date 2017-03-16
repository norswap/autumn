package norswap.lang.java8.resolution
import norswap.whimsy.Attribute
import norswap.whimsy.Node
import norswap.whimsy.Reaction
import norswap.whimsy.ReactorError

// =================================================================================================

abstract class ResolutionError (reac: Reaction<*>, node: Node) : ReactorError()
{
    override val reaction = reac
    override val affected = listOf(Attribute(node, "resolved"))
}

// =================================================================================================

class ClassNotFoundError (reac: Reaction<*>, node: Node) : ResolutionError(reac, node)
{
    override val msg = "Could not find class definition"
}

// =================================================================================================

class MemberNotFoundError (reac: Reaction<*>, node: Node) : ResolutionError(reac, node)
{
    override val msg = "Could not find member"
}

// =================================================================================================