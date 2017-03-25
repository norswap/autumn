package norswap.lang.java8.resolution
import norswap.uranium.Attribute
import norswap.uranium.Node
import norswap.uranium.Reaction
import norswap.uranium.ReactorError

// =================================================================================================

abstract class ResolutionError (reac: Reaction<*>, node: Node) : ReactorError()
{
    override val reaction = reac
    override val affected = listOf(Attribute(node, "resolved"))
}

// =================================================================================================

abstract class ScopeError: ReactorError()
{
    override val reaction = null
}

// =================================================================================================

class ClassNotFoundScopeError: ScopeError()
{
    override val msg = "Could not find class definition"
}

// =================================================================================================

class ClassNotFoundResolutionError (reac: Reaction<*>, node: Node) : ResolutionError(reac, node)
{
    override val msg = "Could not find class definition"
}

// =================================================================================================

class MemberNotFoundScopeError: ScopeError()
{
    override val msg = "Could not find member"
}

// =================================================================================================

class MemberNotFoundResolutionError (reac: Reaction<*>, node: Node) : ResolutionError(reac, node)
{
    override val msg = "Could not find member"
}

// =================================================================================================

class ExtendingNonClass (override val reaction: Reaction<*>, node: Node) : ReactorError()
{
    override val affected = listOf(Attribute(node, "super_type"))
    override val msg = "Class extends a non-class type"
}

// =================================================================================================