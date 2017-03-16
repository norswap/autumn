package norswap.lang.java8.typing
import norswap.whimsy.Attribute
import norswap.whimsy.ReactorError
import norswap.whimsy.Node
import norswap.whimsy.Reaction

// =================================================================================================

abstract class TypeError (reac: Reaction<*>, node: Node) : ReactorError()
{
    override val reaction = reac
    override val affected = listOf(Attribute(node, "type"))
}

// =================================================================================================

class NotTypeError (reac: Reaction<*>, node: Node) : TypeError(reac, node)
{
    override val msg = "Applying '!' on a non-boolean type."
}

// -------------------------------------------------------------------------------------------------

class ComplementTypeError (reac: Reaction<*>, node: Node) : TypeError(reac, node)
{
    override val msg = "Applying '~' on a non-integral type."
}

// -------------------------------------------------------------------------------------------------

class UnaryArithTypeError (reac: Reaction<*>, node: Node) : TypeError(reac, node)
{
    override val msg = "Applying an unary arithmetic operation on a non-numeric type."
}

// -------------------------------------------------------------------------------------------------

class BinaryArithTypeError (reac: Reaction<*>, node: Node) : TypeError(reac, node)
{
    override val msg = "Using a non-numeric value in an arithmetic expression."
}

// -------------------------------------------------------------------------------------------------

class ShiftTypeError (reac: Reaction<*>, node: Node) : TypeError(reac, node)
{
    override val msg = "Using a non-integral value in a shift expression."
}

// -------------------------------------------------------------------------------------------------

class OrderingTypeError (reac: Reaction<*>, node: Node) : TypeError(reac, node)
{
    override val msg = "Using a non-numeric value in a relational expression."
}

// -------------------------------------------------------------------------------------------------

class InstanceofValueError (reac: Reaction<*>, node: Node) : TypeError(reac, node)
{
    override val msg = "Operand of instanceof operator does not have a reference type."
}

// -------------------------------------------------------------------------------------------------

class InstanceofTypeError (reac: Reaction<*>, node: Node) : TypeError(reac, node)
{
    override val msg = "Type operand of instanceof operator is not a reference type."
}

// -------------------------------------------------------------------------------------------------

class InstanceofReifiableError (reac: Reaction<*>, node: Node) : TypeError(reac, node)
{
    override val msg = "Type operand of instanceof operator is not a reifiable type."
}

// -------------------------------------------------------------------------------------------------

class InstanceofCompatError (reac: Reaction<*>, node: Node) : TypeError(reac, node)
{
    override val msg = "Instanceof expression with incompatible operand and type."
}

// -------------------------------------------------------------------------------------------------

class EqualNumBoolError (reac: Reaction<*>, node: Node) : TypeError(reac, node)
{
    override val msg = "Attempting to compare a numeric type with a boolean type."
}

// -------------------------------------------------------------------------------------------------

class EqualPrimRefError (reac: Reaction<*>, node: Node) : TypeError(reac, node)
{
    override val msg = "Attempting to compare a primitive type with a reference type."
}

// -------------------------------------------------------------------------------------------------

class EqualCompatError (reac: Reaction<*>, node: Node) : TypeError(reac, node)
{
    override val msg = "Trying to compare two incompatible reference types."
}

// -------------------------------------------------------------------------------------------------

class BitwiseMixedError (reac: Reaction<*>, node: Node) : TypeError(reac, node)
{
    override val msg = "Binary bitwise operator has a boolean and a non-boolean operand."
}

// -------------------------------------------------------------------------------------------------

class BitwiseRefError (reac: Reaction<*>, node: Node) : TypeError(reac, node)
{
    override val msg = "Using a non-integral or boolean value in a binary bitwise expression."
}

// -------------------------------------------------------------------------------------------------

class LogicalTypeError (reac: Reaction<*>, node: Node) : TypeError(reac, node)
{
    override val msg = "Using a non-boolean expression in a logical expression."
}

// -------------------------------------------------------------------------------------------------