package norswap.lang.java8.resolution
import norswap.lang.java8.typing.ClassLike
import norswap.lang.java8.typing.RefType
import norswap.lang.java8.typing.TType
import norswap.lang.java8.typing.TypeParameter
import norswap.utils.multimap.*
import java.util.Collections

// -------------------------------------------------------------------------------------------------

interface Scope
{
    // TODO delegate rather than object, because there can be multiple parents
    val parent: Scope?
        get() = null

    val fields  : MutableMap<String, FieldInfo>

    // also has <init>, <clinit>
    val methods : MutableMultiMap<String, MethodInfo>

    val class_likes: MutableMap<String, ClassLike>

    val type_params: MutableMap<String, TypeParameter>

    fun type (name: String): RefType?
        = type_params[name] ?: class_likes[name]

    fun full_name (name: String): String
        = name
}

// -------------------------------------------------------------------------------------------------

abstract class ScopeBase: Scope
{
    override val fields      = HashMap<String, FieldInfo>()
    override val methods     = HashMultiMap<String, MethodInfo>()
    override val class_likes = HashMap<String, ClassLike>()
    override val type_params = HashMap<String, TypeParameter>()

    override fun full_name (name: String): String
        = parent ?. full_name(name) ?: throw Error("can't determine full name")
}

// -------------------------------------------------------------------------------------------------

object EmptyScope: Scope
{
    override val fields      : MutableMap<String, FieldInfo>        = Collections.emptyMap()
    override val methods     : MutableMultiMap<String, MethodInfo>  = Collections.emptyMap()
    override val class_likes : MutableMap<String, ClassLike>        = Collections.emptyMap()
    override val type_params : MutableMap<String, TypeParameter>    = Collections.emptyMap()
}

// -------------------------------------------------------------------------------------------------

class PackageScope (val name: String): ScopeBase()
{
    override fun full_name (name: String): String
        = if (this.name == "") name else "${this.name}.$name"
}

// -------------------------------------------------------------------------------------------------

class ScopeBuilder
{
    var current = PackageScope("")

    fun field (name: String): FieldInfo?
        = current.fields[name]

    fun method (name: String): List<Any>?
        = current.methods[name]

    fun type (name: String): TType?
        = current.class_likes[name]

    fun put_member (name: String, value: MemberInfo) {
        when (value) {
            is FieldInfo        -> put_field  (name, value)
            is MethodInfo       -> put_method (name, value)
            // TODO
            //is NestedClassInfo  -> put_type   (name, value)
        }
    }

    fun put_field (name: String, value: FieldInfo) {
        current.fields[name] = value
    }

    fun put_method (name: String, value: MethodInfo) {
        current.methods.append(name, value)
    }

    fun put_type (name: String, value: ClassLike) {
        current.class_likes[name] = value
    }

    fun full_name (name: String): String
        = current.full_name(name)

    fun type_chain (chain: List<String>): String
    {
        // TODO
        TODO()
//        chain.fold(current) { scope, name ->
//            val type = scope.types[name]
//            type?.scope ?: throw Error("Cannot resolve symbol: $name")
//        }
    }
}

// -------------------------------------------------------------------------------------------------

