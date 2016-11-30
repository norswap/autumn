package norswap.lang.java8.typing
import norswap.whimsy.*
import kotlin.collections.listOf as list
import norswap.lang.java8.ast.*

// TODO missing requires (instanceof, and more)

// -------------------------------------------------------------------------------------------------

fun Reactor.install_java8_typing_rules()
{
    add_rule(LiteralRule)
    add_rule(BinaryArithRule)
    add_rule(ShiftRule)
    add_rule(BitwiseRule)
    add_rule(EqualRule)
    add_rule(LogicalRule)
}

// -------------------------------------------------------------------------------------------------

abstract class TypingRule <N: Node>: Rule<N>()
{
    override fun supplies (node: N)
        = kotlin.collections.listOf(node("type"))

    fun RuleInstance<N>.type_error (msg: String): Unit
        = reactor.report_error(Error(this, msg, trigger("type")))
}

// -------------------------------------------------------------------------------------------------

object LiteralRule: TypingRule<Literal>()
{
    override val triggers
        = list(Literal::class.java)

    override fun RuleInstance<Literal>.compute()
    {
        trigger.type = when (trigger.value) {
            is String   -> TString
            is Int      -> TInt
            is Long     -> TLong
            is Float    -> TFloat
            is Double   -> TDouble
            is Char     -> TChar
            else        -> throw Error("unknown literal type")
        }
    }
}

// -------------------------------------------------------------------------------------------------

object NotRule: TypingRule<Not>()
{
    override val triggers = list(Not::class.java)

    override fun RuleInstance<Not>.compute()
    {
        val ot = trigger.op.type.unboxed
        if (ot === TBool)
            trigger.type = TBool
        else
            type_error("Applying '!' on a non-boolean type.")
    }
}

// -------------------------------------------------------------------------------------------------

fun unary_promotion (type: PrimitiveType): PrimitiveType
{
    return when {
        type === TByte  -> TInt
        type === TChar  -> TInt
        type === TShort -> TInt
        else            -> type
    }
}

// -------------------------------------------------------------------------------------------------

object ComplementRule: TypingRule<Complement>()
{
    override val triggers = list(Complement::class.java)

    override fun RuleInstance<Complement>.compute()
    {
        val ot = trigger.op.type.unboxed
        if (ot !is IntegerType)
            type_error("Applying '~' on a non-integral type.")
        else
            trigger.type = unary_promotion(ot)
    }
}

// -------------------------------------------------------------------------------------------------

object UnaryArithmeticOpRule: TypingRule<UnaryOp>()
{
    override val triggers = list(
        UnaryPlus    ::class.java,
        UnaryMinus   ::class.java)

    override fun consumes(node: UnaryOp)
        = list(node.op("type"))

    override fun RuleInstance<UnaryOp>.compute()
    {
        val ot = trigger.op.type.unboxed
        if (ot !is PrimitiveType)
            type_error("Applying an unary arithmetic operation on a reference type.")
        else
            trigger.type = unary_promotion(ot)
    }
}

// -------------------------------------------------------------------------------------------------

abstract class BinaryOpRule : TypingRule<BinaryOp>()
{
    override fun consumes(node: BinaryOp) = list(
        node.left("type"),
        node.right("type"))
}

// -------------------------------------------------------------------------------------------------

fun binary_promotion (lt: PrimitiveType, rt: PrimitiveType): PrimitiveType
{
    return  when {
        lt === TDouble || rt === TDouble -> TDouble
        lt === TFloat  || rt === TFloat  -> TFloat
        lt === TLong   || rt === TLong   -> TLong
        else                             -> TInt
    }
}

// -------------------------------------------------------------------------------------------------

object BinaryArithRule: BinaryOpRule()
{
    override val triggers = list(
        Product         ::class.java,
        Division        ::class.java,
        Remainder       ::class.java,
        Sum             ::class.java,
        Diff            ::class.java,
        Lower           ::class.java,
        LowerEqual      ::class.java,
        Greater         ::class.java,
        GreaterEqual    ::class.java)

    override fun RuleInstance<BinaryOp>.compute()
    {
        val lt = trigger.left .type.unboxed
        val rt = trigger.right.type.unboxed

        if (trigger is Sum && (lt === TString || rt === TString))
            return run { trigger.type = TString }

        if (lt !is NumericType || rt !is NumericType)
            return type_error("Using a non-numeric value in an arithmetic expression.")

        trigger.type = binary_promotion(lt, rt)
    }
}

// -------------------------------------------------------------------------------------------------

object ShiftRule: BinaryOpRule()
{
    override val triggers = list(
        ShiftLeft           ::class.java,
        ShiftRight          ::class.java,
        BinaryShiftRight    ::class.java)

    override fun RuleInstance<BinaryOp>.compute()
    {
        val lt = trigger.left .type.unboxed
        val rt = trigger.right.type.unboxed

        if (lt !is IntegerType || rt !is IntegerType)
             type_error("Using a non-integral value in a shift expression.")

        trigger.type = unary_promotion(lt as PrimitiveType)
    }
}

// -------------------------------------------------------------------------------------------------

object OrderingRule: BinaryOpRule()
{
    override val triggers = list(
        Greater         ::class.java,
        GreaterEqual    ::class.java,
        Lower           ::class.java,
        LowerEqual      ::class.java)

    override fun RuleInstance<BinaryOp>.compute()
    {
        val lt = trigger.left .type.unboxed
        val rt = trigger.right.type.unboxed

        if (lt !is NumericType || rt !is NumericType)
            type_error("Using a non-numeric value in a relational expression.")
        else
            trigger.type = TBool
    }
}

// -------------------------------------------------------------------------------------------------

// TODO
infix fun Type.subclasses (other: Type): Boolean
{
    return true
}

// TODO
infix fun Type.superclasses (other: Type): Boolean
{
    return true
}

// TODO
fun cast_compatible (t1: Type, t2: Type): Boolean
{
    return true
}

// -------------------------------------------------------------------------------------------------

object InstanceofRule: TypingRule<Instanceof>()
{
    override val triggers = list(Instanceof::class.java)

    override fun RuleInstance<Instanceof>.compute()
    {
        val lt = trigger.op.type
        val r = trigger.type // a Type node

        if (lt !is RefType)
            return type_error("Operand of instanceof operator is not a reference type.")

        // TODO
        // - must check that `r` is reifiable
        // - must resolve `r` to a type instance

        if (cast_compatible(lt, lt)) // TODO (lt twice)
            trigger["type"] = TBool
        else
            type_error("Instanceof expression with incompatible operand and type.")
    }
}

// -------------------------------------------------------------------------------------------------

object EqualRule: BinaryOpRule()
{
    override val triggers = list(
        Equal       ::class.java,
        NotEqual    ::class.java)

    override fun RuleInstance<BinaryOp>.compute()
    {
        val lt = trigger.left .type
        val rt = trigger.right.type
        val ltu = lt.unboxed
        val rtu = rt.unboxed

        if (ltu is NumericType && rtu is NumericType)
            return run { trigger.type = TBool }

        if (ltu === TBool && rtu === TBool)
            return run { trigger.type = TBool }

        if (ltu is PrimitiveType || rtu is PrimitiveType)
            return type_error("Attempting to compare a numeric type with a boolean type.")

        if (lt is PrimitiveType || rt is PrimitiveType)
            return type_error("Attempting to compare a primitive type with a reference type.")

        if (cast_compatible(lt, rt))
            trigger.type = TBool
        else
            type_error("Trying to compare two incompatible reference types.")
    }
}

// -------------------------------------------------------------------------------------------------

object BitwiseRule: BinaryOpRule()
{
    override val triggers = list(
        BinaryAnd   ::class.java,
        Xor         ::class.java,
        BinaryOr    ::class.java)

    override fun RuleInstance<BinaryOp>.compute()
    {
        val lt = trigger.left .type.unboxed
        val rt = trigger.right.type.unboxed

        if (lt === TBool && rt === TBool)
            return run { trigger.type = TBool }

        if (lt === TBool || rt === TBool)
            return type_error("Binary bitwise operator has a boolean and a non-boolean operand.")

        if (lt !is IntegerType || rt !is IntegerType)
            return type_error("Using a non-integral or boolean value in a binary bitwise expression.")

        trigger.type = binary_promotion(lt, rt)
    }
}

// -------------------------------------------------------------------------------------------------

object LogicalRule: BinaryOpRule()
{
    override val triggers = list(
        And         ::class.java,
        Or          ::class.java)

    override fun RuleInstance<BinaryOp>.compute()
    {
        val lt = trigger.left .type.unboxed
        val rt = trigger.right.type.unboxed

        if (lt !== TBool || rt !== TBool)
            type_error("Using a non-boolean expression in a logical expression.")
        else
            trigger.type = TBool
    }
}

// -------------------------------------------------------------------------------------------------