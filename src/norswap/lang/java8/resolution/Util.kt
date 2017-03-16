package norswap.lang.java8.resolution
import norswap.lang.java8.ast.Type
import norswap.lang.java8.typing.TType

/**
 * Accessor for the "resolved" property.
 */
inline var Type.resolved: TType
    get()      = this["resolved"] as TType
    set(value) { this["resolved"] = value }