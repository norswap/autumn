package norswap.lang.java8.resolution
import org.apache.bcel.Const
import org.apache.bcel.classfile.ClassParser
import org.apache.bcel.classfile.ConstantPool
import org.apache.bcel.classfile.ConstantUtf8
import org.apache.bcel.classfile.Field
import org.apache.bcel.classfile.InnerClass
import org.apache.bcel.classfile.InnerClasses
import org.apache.bcel.classfile.JavaClass
import org.apache.bcel.classfile.Method
import java.net.URL
import java.net.URLClassLoader
import java.util.Arrays

// TODO currently lookup is per class, but in the future can optimize (list per package?)
// TODO load classes without .class file: some in java.lang (use reflection - see javasymbolresolver)

// =================================================================================================

abstract class MemberInfo
{
    abstract val name: String
}

// -------------------------------------------------------------------------------------------------

class InnerClassInfo (val binner: InnerClass, val pool: ConstantPool): MemberInfo()
{
    override val name: String
        get() {
            if (binner.innerNameIndex != 0) {
                val const = pool.getConstant(binner.innerNameIndex, Const.CONSTANT_Utf8)
                return (const as ConstantUtf8).bytes
            } else {
                return "(anonymous)"
            }
        }
}

// -------------------------------------------------------------------------------------------------

class MethodInfo (val bmethod: Method): MemberInfo()
{
    override val name: String = bmethod.name
}

// -------------------------------------------------------------------------------------------------

class FieldInfo (val bfield: Field): MemberInfo()
{
    override val name: String = bfield.name
}

// =================================================================================================

class ClassInfo (val bclass: JavaClass)
{
    val inner: List<InnerClassInfo> by lazy { inner_classes() }

    val methods: List<MethodInfo> by lazy { bclass.methods.map(::MethodInfo) }

    val fields: List<FieldInfo> by lazy { bclass.fields.map(::FieldInfo) }

    val members: List<MemberInfo> by lazy { inner + methods + fields }

    fun members (name: String): List<MemberInfo>
        = members.filter { it.name == name }

    private fun inner_classes(): List<InnerClassInfo>
    {
        val attr = bclass.fields
            .filterIsInstance<InnerClasses>()
            .firstOrNull()
            ?: return emptyList()

        return attr.innerClasses.map { InnerClassInfo(it, attr.constantPool) }
    }
}

// =================================================================================================

object Resolver
{
    private val class_cache = HashMap<String, ClassInfo?>()

    val urls = (ClassLoader.getSystemClassLoader() as URLClassLoader).urLs

    val loader = PathClassLoader(urls)

    fun resolve_class (full_name: String): ClassInfo?
    {
        return class_cache.getOrPut(full_name)
        {
            val class_url = loader.find_class_path(full_name) ?: return null
            val cparser = ClassParser(class_url.openStream(), class_url.toString())
            val bclass = cparser.parse()
            ClassInfo(bclass)
        }
    }
}

// =================================================================================================

class PathClassLoader (urls: Array<URL>): URLClassLoader(urls)
{
    fun find_class_path (name: String): URL?
        = findResource(name.replace('.', '/') + ".class")
}

// =================================================================================================

fun main (args: Array<String>)
{
    println(Arrays.toString(Resolver.urls))
    //Resolver.resolve_members("norswap.lang.java8.resolution.Test", "Zor")
}