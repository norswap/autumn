package norswap.lang.java8.typing
import norswap.whimsy.ReactorError
import norswap.whimsy.Node
import norswap.whimsy.RuleInstance

// -------------------------------------------------------------------------------------------------

abstract class TypeError (ri: RuleInstance<*>, node: Node) : ReactorError()
{
    override val rule_instance = ri
    override val affected = arrayOf(node("type"))
}

// -------------------------------------------------------------------------------------------------

class NotTypeError (ri: RuleInstance<*>, node: Node) : TypeError(ri, node)
{
    override val msg = "Applying '!' on a non-boolean type."
}

// -------------------------------------------------------------------------------------------------

class ComplementTypeError (ri: RuleInstance<*>, node: Node) : TypeError(ri, node)
{
    override val msg = "Applying '~' on a non-integral type."
}

// -------------------------------------------------------------------------------------------------

class UnaryArithTypeError (ri: RuleInstance<*>, node: Node) : TypeError(ri, node)
{
    override val msg = "Applying an unary arithmetic operation on a non-numeric type."
}

// -------------------------------------------------------------------------------------------------

class BinaryArithTypeError (ri: RuleInstance<*>, node: Node) : TypeError(ri, node)
{
    override val msg = "Using a non-numeric value in an arithmetic expression."
}

// -------------------------------------------------------------------------------------------------

class ShiftTypeError (ri: RuleInstance<*>, node: Node) : TypeError(ri, node)
{
    override val msg = "Using a non-integral value in a shift expression."
}

// -------------------------------------------------------------------------------------------------

class OrderingTypeError (ri: RuleInstance<*>, node: Node) : TypeError(ri, node)
{
    override val msg = "Using a non-numeric value in a relational expression."
}

// -------------------------------------------------------------------------------------------------

class InstanceofOperandError (ri: RuleInstance<*>, node: Node) : TypeError(ri, node)
{
    override val msg = "Operand of instanceof operator is not a reference type."
}

// -------------------------------------------------------------------------------------------------

class InstanceofCompatError (ri: RuleInstance<*>, node: Node) : TypeError(ri, node)
{
    override val msg = "Instanceof expression with incompatible operand and type."
}

// -------------------------------------------------------------------------------------------------

class EqualNumBoolError (ri: RuleInstance<*>, node: Node) : TypeError(ri, node)
{
    override val msg = "Attempting to compare a numeric type with a boolean type."
}

// -------------------------------------------------------------------------------------------------

class EqualPrimRefError (ri: RuleInstance<*>, node: Node) : TypeError(ri, node)
{
    override val msg = "Attempting to compare a primitive type with a reference type."
}

// -------------------------------------------------------------------------------------------------

class EqualCompatError (ri: RuleInstance<*>, node: Node) : TypeError(ri, node)
{
    override val msg = "Trying to compare two incompatible reference types."
}

// -------------------------------------------------------------------------------------------------

class BitwiseMixedError (ri: RuleInstance<*>, node: Node) : TypeError(ri, node)
{
    override val msg = "Binary bitwise operator has a boolean and a non-boolean operand."
}

// -------------------------------------------------------------------------------------------------

class BitwiseRefError (ri: RuleInstance<*>, node: Node) : TypeError(ri, node)
{
    override val msg = "Using a non-integral or boolean value in a binary bitwise expression."
}

// -------------------------------------------------------------------------------------------------

class LogicalTypeError (ri: RuleInstance<*>, node: Node) : TypeError(ri, node)
{
    override val msg = "Using a non-boolean expression in a logical expression."
}