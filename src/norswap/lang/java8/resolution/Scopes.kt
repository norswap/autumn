package norswap.lang.java8.resolution
import norswap.utils.multimap.*

// -------------------------------------------------------------------------------------------------

open class Scope (val parent: Scope?)
{
    val types   = HashMap<String, Any>()
    val methods = HashMultiMap<String, Any>()
    val fields  = HashMap<String, Any>()
}

// -------------------------------------------------------------------------------------------------

class PackageScope (val name: String): Scope(null)

// -------------------------------------------------------------------------------------------------

class ScopeBuilder
{
    var current = PackageScope("(default)")

    fun field (name: String)
        = current.fields[name]

    fun method (name: String): List<Any>?
        = current.methods[name]

    fun type (name: String)
        = current.types[name]

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

    fun put_type (name: String, value: ClassInfo) {
        current.types[name] = value
    }
}

// -------------------------------------------------------------------------------------------------

