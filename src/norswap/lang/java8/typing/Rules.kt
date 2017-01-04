package norswap.lang.java8.typing
import norswap.whimsy.*
import kotlin.collections.listOf as list
import norswap.lang.java8.ast.*

// =================================================================================================

fun Reactor.install_java8_typing_rules()
{
    add_rule(LiteralRule)
    add_rule(NotRule)
    add_rule(ComplementRule)
    add_rule(UnaryArithRule)
    add_rule(BinaryArithRule)
    add_rule(ShiftRule)
    add_rule(OrderingRule)
    add_rule(InstanceofRule)
    add_rule(EqualRule)
    add_rule(BitwiseRule)
    add_rule(LogicalRule)
}

// =================================================================================================

abstract class TypingRule <N: Node>: Rule<N>()
{
    override fun supplies (node: N)
        = kotlin.collections.listOf(node("type"))

    inline fun RuleInstance<N>.report (mk: (RuleInstance<*>, Node) -> ReactorError): Unit
        = reactor.report_error(mk(this, trigger))
}

// -------------------------------------------------------------------------------------------------

abstract class UnaryTypingRule <N: UnaryOp> : TypingRule<N>()
{
    override fun consumes (node: N) = list(node.op("type"))
}

// -------------------------------------------------------------------------------------------------

abstract class BinaryOpRule: TypingRule<BinaryOp>()
{
    override fun consumes(node: BinaryOp) = list(
        node.left("type"),
        node.right("type"))
}

// =================================================================================================


object LiteralRule: TypingRule<Literal>()
{
    override val triggers
        = list(Literal::class.java)

    override fun RuleInstance<Literal>.compute()
    {
        trigger.atype = when (trigger.value) {
            is String   -> TString
            is Int      -> TInt
            is Long     -> TLong
            is Float    -> TFloat
            is Double   -> TDouble
            is Char     -> TChar
            is Boolean  -> TBool
            else        -> throw Error("unknown literal type")
        }
    }
}

// -------------------------------------------------------------------------------------------------

object NotRule: UnaryTypingRule<Not>()
{
    override val triggers = list(Not::class.java)

    override fun RuleInstance<Not>.compute()
    {
        val ot = trigger.op.atype.unboxed
        if (ot === TBool)
            trigger.atype = TBool
        else
            report(::NotTypeError)
    }
}

// -------------------------------------------------------------------------------------------------

object ComplementRule: UnaryTypingRule<Complement>()
{
    override val triggers = list(Complement::class.java)

    override fun RuleInstance<Complement>.compute()
    {
        val ot = trigger.op.atype.unboxed
        if (ot !is IntegerType)
            report(::ComplementTypeError)
        else
            trigger.atype = unary_promotion(ot)
    }
}

// -------------------------------------------------------------------------------------------------

object UnaryArithRule: UnaryTypingRule<UnaryOp>()
{
    override val triggers = list(
        UnaryPlus    ::class.java,
        UnaryMinus   ::class.java)

    override fun RuleInstance<UnaryOp>.compute()
    {
        val ot = trigger.op.atype.unboxed
        if (ot !is NumericType)
            report(::UnaryArithTypeError)
        else
            trigger.atype = unary_promotion(ot)
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
        Diff            ::class.java)

    override fun RuleInstance<BinaryOp>.compute()
    {
        val lt = trigger.left .atype.unboxed
        val rt = trigger.right.atype.unboxed

        if (trigger is Sum && (lt === TString || rt === TString))
            return run { trigger.atype = TString }

        if (lt !is NumericType || rt !is NumericType)
            return report(::BinaryArithTypeError)

        trigger.atype = binary_promotion(lt, rt)
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
        val lt = trigger.left .atype.unboxed
        val rt = trigger.right.atype.unboxed

        if (lt !is IntegerType || rt !is IntegerType)
             report(::ShiftTypeError)
        else
            trigger.atype = unary_promotion(lt)
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
        val lt = trigger.left .atype.unboxed
        val rt = trigger.right.atype.unboxed

        if (lt !is NumericType || rt !is NumericType)
            report(::OrderingTypeError)
        else
            trigger.atype = TBool
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

    override fun consumes(node: Instanceof) = list(node.op("type"), node.type("resolved"))

    override fun RuleInstance<Instanceof>.compute()
    {
        val lt = trigger.op.atype
        val r = trigger.type // a Type node

        if (lt !is RefType)
            return report(::InstanceofOperandError)

        // TODO
        // - must check that `r` is reifiable
        // - must resolve `r` to a type instance

        if (cast_compatible(lt, lt)) // TODO (lt twice)
            trigger["type"] = TBool
        else
            report(::InstanceofCompatError)
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
        val lt = trigger.left .atype
        val rt = trigger.right.atype
        val ltu = lt.unboxed
        val rtu = rt.unboxed

        if (ltu is NumericType && rtu is NumericType)
            return run { trigger.atype = TBool }

        if (ltu === TBool && rtu === TBool)
            return run { trigger.atype = TBool }

        if (ltu is PrimitiveType && rtu is PrimitiveType)
            return report(::EqualNumBoolError)

        if (lt is PrimitiveType || rt is PrimitiveType)
            return report(::EqualPrimRefError)

        if (cast_compatible(lt, rt))
            trigger.atype = TBool
        else
            report(::EqualCompatError)
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
        val lt = trigger.left .atype.unboxed
        val rt = trigger.right.atype.unboxed

        if (lt === TBool && rt === TBool)
            return run { trigger.atype = TBool }

        if (lt === TBool || rt === TBool)
            return report(::BitwiseMixedError)

        if (lt !is IntegerType || rt !is IntegerType)
            return report(::BitwiseRefError)

        trigger.atype = binary_promotion(lt, rt)
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
        val lt = trigger.left .atype.unboxed
        val rt = trigger.right.atype.unboxed

        if (lt !== TBool || rt !== TBool)
            report(::LogicalTypeError)
        else
            trigger.atype = TBool
    }
}

// -------------------------------------------------------------------------------------------------