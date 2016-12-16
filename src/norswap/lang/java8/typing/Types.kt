package norswap.lang.java8.typing

// -------------------------------------------------------------------------------------------------

interface Type

// -------------------------------------------------------------------------------------------------

open class PrimitiveType: Type
open class NumericType: PrimitiveType()
open class IntegerType: NumericType()
open class FloatingType: NumericType()

// -------------------------------------------------------------------------------------------------

interface RefType: Type

// -------------------------------------------------------------------------------------------------

class BoxedType: RefType

// -------------------------------------------------------------------------------------------------

val TBool   = PrimitiveType()
val TByte   = IntegerType()
val TChar   = IntegerType()
val TShort  = IntegerType()
val TInt    = IntegerType()
val TLong   = IntegerType()
val TFloat  = FloatingType()
val TDouble = FloatingType()

// -------------------------------------------------------------------------------------------------

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
): RefType

// -------------------------------------------------------------------------------------------------

val TString = Class(listOf("java", "lang", "String"))

// -------------------------------------------------------------------------------------------------