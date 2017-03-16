package norswap.lang.java8.typing
import norswap.lang.java8.ast.Expr
import norswap.utils.proclaim

// -------------------------------------------------------------------------------------------------

/**
 * Accessor for the "type" property.
 */
inline var Expr.ptype: TType
    get()      = this["type"] as TType
    set(value) { this["type"] = value }

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
val TType.unboxed: TType
    get() = when (this) {
        !is BoxedType -> this
        BByte         -> TByte
        BChar         -> TChar
        BInt          -> TInt
        BLong         -> TLong
        BFloat        -> TFloat
        BDouble       -> TDouble
        BBool         -> TBool
        BVoid         -> TVoid
        else          -> this // unreachable
    }

// -------------------------------------------------------------------------------------------------

/**
 * Returns the unboxed primitive type corresponding to this boxed type.
 */
val BoxedType.unboxed: PrimitiveType
    get() = (this as TType).unboxed as PrimitiveType

// -------------------------------------------------------------------------------------------------

/**
 * Returns the boxed type corresponding to this primitive type.
 */
val PrimitiveType.boxed: RefType
    get() = when (this) {
        TByte         -> BByte
        TChar         -> BChar
        TInt          -> BInt
        TLong         -> BLong
        TFloat        -> BFloat
        TDouble       -> BDouble
        TBool         -> BBool
        TVoid         -> BVoid
        else          -> throw Error() // unreachable if no new primitive types
    }

// -------------------------------------------------------------------------------------------------

/**
 * True if the type is reifiable (available for reflection at run-time).
 */
fun TType.reifiable(): Boolean
{
    return if (this !is PrimitiveType) true
    else   if (this is ArrayType) component.reifiable()
    else   if (this is NestedType) types.all { it.reifiable() }
    else   if (this !is ParameterizedType) true
    else   type_args.all { it is WildcardType }
}

// -------------------------------------------------------------------------------------------------

/**
 * True if the receiver is a subclass of `other`.
 */
infix fun RefType.sub (other: RefType): Boolean
{
    // TODO: not that simple: wildcards
    return ancestors.contains(other)
}

// -------------------------------------------------------------------------------------------------

/**
 * True if the receiver is a superclass of `other`.
 */
infix fun RefType.sup (other: RefType): Boolean
{
    return other.sub(this)
}

// -------------------------------------------------------------------------------------------------

fun primitive_rank (type: PrimitiveType): Int
    = when (type) {
        TByte   -> 0
        TChar   -> 1
        TShort  -> 1
        TInt    -> 2
        TLong   -> 3
        TFloat  -> 4
        TDouble -> 5
        else    -> -1
    }

// -------------------------------------------------------------------------------------------------

/**
 * True if a value of type [source] can be converted to type [target] via a widening
 * or primitive conversion, or via identity conversion.
 */
fun prim_widening_conversion (source: PrimitiveType, target: PrimitiveType): Boolean
{
    val srank = primitive_rank(source)
    val trank = primitive_rank(target)
    return srank >= 0 && trank >= 0 && trank >= srank
}

// -------------------------------------------------------------------------------------------------

/**
 * True if a value of type [source] can be converted to type [target] via a narrowing
 * or primitive conversion, or via identity conversion.
 */
fun prim_narrowing_conversion (source: PrimitiveType, target: PrimitiveType): Boolean
    = prim_widening_conversion(target, source)

// -------------------------------------------------------------------------------------------------

// TODO
fun ref_widening_conversion (source: RefType, target: RefType): Boolean
    = source sub target

// TODO
fun ref_narrowing_conversion (source: RefType, target: RefType): Boolean
    = source sup target

// -------------------------------------------------------------------------------------------------

/**
 * Returns the first detected conflicting parameterization: a pair of distinct types, an ancestor
 * of t1 and an ancestor of t2, that have the same erasure.
 */
fun find_conflicting_parameterization (t1: RefType, t2: RefType): Pair<RefType, RefType>?
{
    val t1_ancestors = t1.ancestors.filterIsInstance<ParameterizedType>()
    val t2_ancestors = t2.ancestors.filterIsInstance<ParameterizedType>()

    for (o1 in t1_ancestors)
        for (o2 in t2_ancestors)
            if (o1.raw == o2.raw && o1.type_args != o2.type_args)
                return o1 to o2

    return null
}

// -------------------------------------------------------------------------------------------------

/**
 * True of [source] and [target] are cast-compatible: a cast between from a value with type
 * [source] to type [target] may potentially succeed.
 *
 * See http://docs.oracle.com/javase/specs/jls/se8/html/jls-5.html#jls-5.5
 */
fun cast_compatible (source: TType, target: TType): Boolean
{
    if (source == target)
        return true

    if (source is IntersectionType)
        return source.members.all { cast_compatible(it, target) }

    if (target is IntersectionType)
        return target.members.all { cast_compatible(source, it) }

    if (source is TypeParameter)
        return cast_compatible(source.upper_bound, target)

    if (target is TypeParameter)
        return cast_compatible(source, target.upper_bound)

    if (source is PrimitiveType) {
        if (target is PrimitiveType)
            return source is NumericType && target is NumericType
        else
            return ref_widening_conversion(source.boxed, target as RefType)
    }

    proclaim(source as RefType)

    if (target is PrimitiveType) {
        if (source is BoxedType)
            return prim_widening_conversion(source.unboxed, target)
        else
            return ref_narrowing_conversion(source, target.boxed)
    }

    proclaim(target as RefType)

    if (source is ArrayType) {
        if (target is ArrayType) {
            val sc = source.component
            val tc = target.component
            if (sc is RefType && tc is RefType)
                return cast_compatible(sc, tc)
            else
                return sc == tc // same primitive type
        }
        else
            // target must be Object, Serializable or Cloneable
            return source sub target
    }

    if (target is ArrayType)
        // source must be Object, Serializable or Cloneable
        return target sub source

    val conflict = find_conflicting_parameterization(source, target)

    return conflict != null
        || ref_narrowing_conversion (source.erasure, target.erasure)
        || ref_widening_conversion  (source.erasure, target.erasure)
}

// -------------------------------------------------------------------------------------------------