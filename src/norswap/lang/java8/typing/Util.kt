package norswap.lang.java8.typing
import norswap.lang.java8.ast.Expr
import norswap.whimsy.ReactorError
import norswap.whimsy.RuleInstance

// -------------------------------------------------------------------------------------------------

inline var Expr.atype: Type
    get()      = this["type"] as Type
    set(value) { this["type"] = value }

// -------------------------------------------------------------------------------------------------

fun RuleInstance<*>.error (err: ReactorError): Unit
    = reactor.report_error(err)

// -------------------------------------------------------------------------------------------------

/**
 * Promotes integer types to `int` if less wide, otherwise returns the type itself
 * (float, double, int, long).
 */
fun unary_promotion (type: NumericType): NumericType
{
    return when {
        type === TByte  -> TInt
        type === TChar  -> TInt
        type === TShort -> TInt
        else            -> type
    }
}

// -------------------------------------------------------------------------------------------------

/**
 * Returns the wider of the two numeric types, after [unary_promotion] for each.
 */
fun binary_promotion (lt: NumericType, rt: NumericType): NumericType
{
    return  when {
        lt === TDouble || rt === TDouble -> TDouble
        lt === TFloat  || rt === TFloat  -> TFloat
        lt === TLong   || rt === TLong   -> TLong
        else                             -> TInt
    }
}

// -------------------------------------------------------------------------------------------------

/**
 * If the type is a boxed numeric type, return the corresponding unboxed primitive type.
 */
val Type.unboxed: Type
    get() = when (this) {
        !is BoxedType -> this
        BByte         -> TByte
        BChar         -> TChar
        BInt          -> TInt
        BLong         -> TLong
        BFloat        -> TFloat
        BDouble       -> TDouble
        BBool         -> TBool
        else          -> this // unreachable
    }

// -------------------------------------------------------------------------------------------------