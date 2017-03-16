package norswap.lang.java8.typing

// -------------------------------------------------------------------------------------------------

interface TType

// -------------------------------------------------------------------------------------------------

open class PrimitiveType:   TType

open class NumericType:     PrimitiveType()
open class IntegerType:     NumericType()
open class FloatingType:    NumericType()

val TVoid   = PrimitiveType()
val TBool   = PrimitiveType()
val TByte   = IntegerType()
val TChar   = IntegerType()
val TShort  = IntegerType()
val TInt    = IntegerType()
val TLong   = IntegerType()
val TFloat  = FloatingType()
val TDouble = FloatingType()

// -------------------------------------------------------------------------------------------------

interface RefType: TType
{
    val super_interfaces: List<RefType>
        get() = emptyList()

    val super_types: List<RefType>
        get() = super_interfaces

    val ancestors: List<RefType>
        get() {
            val list = ArrayList(super_types)
            var next = 0
            while (next != list.size) {
                val end = list.size
                for (i in next..(end-1))
                    list.addAll(list[i].super_types)
                next = end
            }
            return list.distinct()
        }

    val erasure: RefType
        get() = this
}

// -------------------------------------------------------------------------------------------------

interface InstantiableType: RefType
{
    val super_type: RefType
        get() = TObject

    override val super_types: List<RefType>
        get() = super_interfaces + super_type
}

// -------------------------------------------------------------------------------------------------

interface IntersectionType: RefType
{
    val members: List<RefType> // not intersection themsevles
}

// -------------------------------------------------------------------------------------------------

interface NestedType: RefType
{
    val types: List<RefType> // not nested themselves
}

// -------------------------------------------------------------------------------------------------

interface ParameterizedType: RefType
{
    val raw: RefType
    val type_args: List<RefType>

    override val erasure: RefType
        get() = raw
}

// -------------------------------------------------------------------------------------------------

interface WildcardType: RefType
{
    val upper_bounds: List<RefType>
    val lower_bounds: List<RefType>
}

// -------------------------------------------------------------------------------------------------

interface TypeParameter: RefType
{
    val upper_bound: RefType
}

// -------------------------------------------------------------------------------------------------

interface ArrayType: InstantiableType
{
    val component: TType

    override val super_interfaces: List<RefType>
        get() = listOf(TSerializable, TCloneable)
}

// -------------------------------------------------------------------------------------------------

class TNull: RefType

// -------------------------------------------------------------------------------------------------

class BoxedType: RefType

// -------------------------------------------------------------------------------------------------

val BVoid   = BoxedType()
val BBool   = BoxedType()
val BByte   = BoxedType()
val BChar   = BoxedType()
val BShort  = BoxedType()
val BInt    = BoxedType()
val BLong   = BoxedType()
val BFloat  = BoxedType()
val BDouble = BoxedType()

// -------------------------------------------------------------------------------------------------

data class Class (
    val name: List<String>
): InstantiableType

// -------------------------------------------------------------------------------------------------

val TSerializable = Class(listOf("java", "lang", "Serializable"))
val TCloneable = Class(listOf("java", "lang", "Cloneable"))
val TObject = Class(listOf("java", "lang", "Object"))
val TString = Class(listOf("java", "lang", "String"))

// -------------------------------------------------------------------------------------------------