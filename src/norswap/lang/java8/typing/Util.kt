package norswap.lang.java8.typing
import norswap.lang.java8.ast.Expr
import norswap.whimsy.Attribute
import norswap.whimsy.ast_utils.node

// -------------------------------------------------------------------------------------------------

inline var Expr.type: Type
    get()      = node["type"] as Type
    set(value) { node["type"] = value }

// -------------------------------------------------------------------------------------------------

@Suppress("NOTHING_TO_INLINE")
inline operator fun Expr.invoke (attr: String): Attribute
    = node(attr)

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
        else          -> this // unreachable
    }

// -------------------------------------------------------------------------------------------------