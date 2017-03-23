package norswap.lang.java8.typing
import norswap.lang.java8.ast.TypeDeclKind
import norswap.lang.java8.resolution.EmptyScope
import norswap.lang.java8.resolution.FieldInfo
import norswap.lang.java8.resolution.MemberInfo
import norswap.lang.java8.resolution.MethodInfo
import norswap.lang.java8.resolution.Resolver
import norswap.lang.java8.resolution.Scope
import norswap.utils.multimap.*
import norswap.utils.cast
import norswap.utils.maybe_list

// -------------------------------------------------------------------------------------------------

interface TType
{
    val name: String

    val scope: Scope
        get() = EmptyScope
}

// -------------------------------------------------------------------------------------------------

abstract class PrimitiveType (override val name: String): TType

abstract class NumericType  (name: String) : PrimitiveType(name)
abstract class IntegerType  (name: String) : NumericType(name)
abstract class FloatingType (name: String) : NumericType(name)

object TVoid   : PrimitiveType("void")
object TBool   : PrimitiveType("boolean")
object TByte   : IntegerType("byte")
object TChar   : IntegerType("char")
object TShort  : IntegerType("short")
object TInt    : IntegerType("int")
object TLong   : IntegerType("long")
object TFloat  : FloatingType("float")
object TDouble : FloatingType("double")

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

object TNull: RefType
{
    override val name = "null"
}

// -------------------------------------------------------------------------------------------------

interface ClassLike: InstantiableType, Scope, MemberInfo
{
    val full_name: String

    val kind: TypeDeclKind

    fun members (name: String): List<MemberInfo>
        = ( maybe_list(fields[name])
        +   maybe_list(methods[name])
        +   maybe_list(class_likes[name]))
        .cast()

    fun members(): List<MemberInfo>
        = (fields.values + methods.flat_values() + class_likes.values).cast()
}

// -------------------------------------------------------------------------------------------------

val TObject         : ClassLike = Resolver.resolve_class("java.lang.Object")!!
val TString         : ClassLike = Resolver.resolve_class("java.lang.String")!!
val TSerializable   : ClassLike = Resolver.resolve_class("java.io.Serializable")!!
val TCloneable      : ClassLike = Resolver.resolve_class("java.lang.Cloneable")!!

// -------------------------------------------------------------------------------------------------

abstract class BoxedType (full_name: String): ClassLike
{
    val loaded =  Resolver.resolve_class(full_name)!!

    // Delegation doesn't work because of a compiler bug.
    override val name: String
        get() = loaded.name
    override val full_name: String
        get() = loaded.full_name
    override val kind: TypeDeclKind
        get() = loaded.kind
    override val fields: MutableMap<String, FieldInfo>
        get() = loaded.fields
    override val methods: MutableMultiMap<String, MethodInfo>
        get() = loaded.methods
    override val class_likes: MutableMap<String, ClassLike>
        get() = loaded.class_likes
    override val type_params: MutableMap<String, TypeParameter>
        get() = loaded.type_params
}

// -------------------------------------------------------------------------------------------------

object BVoid   : BoxedType("java.lang.Void")
object BBool   : BoxedType("java.lang.Boolean")
object BByte   : BoxedType("java.lang.Bytes")
object BChar   : BoxedType("java.lang.Character")
object BShort  : BoxedType("java.lang.Short")
object BInt    : BoxedType("java.lang.Integer")
object BLong   : BoxedType("java.lang.Long")
object BFloat  : BoxedType("java.lang.Float")
object BDouble : BoxedType("java.lang.Double")

// -------------------------------------------------------------------------------------------------