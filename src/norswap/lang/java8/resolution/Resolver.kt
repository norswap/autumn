package norswap.lang.java8.resolution
import norswap.utils.attempt
import norswap.utils.str
import org.apache.bcel.Const
import org.apache.bcel.classfile.ClassParser
import org.apache.bcel.classfile.ConstantPool
import org.apache.bcel.classfile.ConstantUtf8
import org.apache.bcel.classfile.Field as BField
import org.apache.bcel.classfile.InnerClass
import org.apache.bcel.classfile.InnerClasses
import org.apache.bcel.classfile.JavaClass
import org.apache.bcel.classfile.Method as BMethod
import java.lang.reflect.Field as JField
import java.lang.reflect.Method as JMethod
import java.net.URL
import java.net.URLClassLoader

// =================================================================================================

abstract class MemberInfo
{
    abstract val name: String
    override fun toString() = name
}

// -------------------------------------------------------------------------------------------------

abstract class MethodInfo: MemberInfo()

// -------------------------------------------------------------------------------------------------

class BytecodeMethodInfo (val method: BMethod): MethodInfo()
{
    override val name: String = method.name
}

// -------------------------------------------------------------------------------------------------

class ReflectionMethodInfo (val method: JMethod): MethodInfo()
{
    override val name = method.name
}

// -------------------------------------------------------------------------------------------------

abstract class FieldInfo: MemberInfo()

// -------------------------------------------------------------------------------------------------

class BytecodeFieldInfo (val field: BField): FieldInfo()
{
    override val name: String = field.name
}

// -------------------------------------------------------------------------------------------------

class ReflectionFieldInfo (val field: JField): FieldInfo()
{
    override val name: String = field.name
}

// -------------------------------------------------------------------------------------------------

abstract class NestedClassInfo : MemberInfo()

// -------------------------------------------------------------------------------------------------

class BytecodeNestedClassInfo(val nested: InnerClass, val pool: ConstantPool): NestedClassInfo()
{
    // Note the BCel terminology is wrong. These are really nested classes.
    // (Inner classes are non-static nested classes.)

    override val name: String
        get() {
            if (nested.innerNameIndex != 0) {
                val const = pool.getConstant(nested.innerNameIndex, Const.CONSTANT_Utf8)
                return (const as ConstantUtf8).bytes
            } else {
                return "(anonymous)"
            }
        }
}

// -------------------------------------------------------------------------------------------------

class ReflectionNestedClassInfo(val nested: Class<*>): NestedClassInfo()
{
    override val name = nested.name
}

// =================================================================================================

abstract class ClassInfo
{
    abstract val nested: List<NestedClassInfo>

    // also has <init>
    abstract val methods: List<MethodInfo>

    abstract val fields: List<FieldInfo>

    abstract val full_name: String

    val members: List<MemberInfo> by lazy {
        val list = ArrayList<MemberInfo>()
        list.addAll(nested)
        list.addAll(methods)
        list.addAll(fields)
        list
    }

    fun members (name: String): List<MemberInfo>
        = members.filter { it.name == name }
}

// -------------------------------------------------------------------------------------------------

class ClassFileInfo (val bclass: JavaClass): ClassInfo()
{
    override val nested  by lazy { nested_classes() }
    override val methods by lazy { bclass.methods.map(::BytecodeMethodInfo) }
    override val fields  by lazy { bclass.fields.map(::BytecodeFieldInfo) }

    private fun nested_classes(): List<NestedClassInfo>
    {
        val attr = bclass.attributes
            .filterIsInstance<InnerClasses>()
            .firstOrNull()
            ?: return emptyList()

        return attr.innerClasses.map { BytecodeNestedClassInfo(it, attr.constantPool) }
    }
}

// -------------------------------------------------------------------------------------------------

class ReflectionClassInfo (val klass: Class<*>): ClassInfo()
{
    override val nested  by lazy { klass.classes.map(::ReflectionNestedClassInfo) }
    override val methods by lazy { klass.methods.map(::ReflectionMethodInfo) }
    override val fields  by lazy { klass.fields.map(::ReflectionFieldInfo) }
}

// =================================================================================================

object Resolver
{
    private val class_cache = HashMap<String, ClassInfo?>()
    private val syscl = ClassLoader.getSystemClassLoader() as URLClassLoader

    val urls = syscl.urLs

    val loader = PathClassLoader(urls)

    fun resolve_class (full_name: String): ClassInfo?
        = class_cache.getOrPut(full_name) { seek_class(full_name) }

    fun resolve_class_chain (chain: List<String>): ClassInfo?
    {
        for (i in chain.indices) {
            val prefix = chain.subList(0, chain.size - i).joinToString(".")
            var klass = resolve_class(prefix) ?: continue
            for (j in 0..i) {
                klass = klass.
            }
        }
    }

    fun resolve_nested_class (klass: ClassInfo, name: String): ClassInfo?
    {
        val nested = klass.nested.find { it.name == name } ?: return null
        return resolve_class(klass.full_name + "")
    }

    private fun seek_class (full_name: String): ClassInfo?
    {
        val class_url = loader.find_class_path(full_name)

        if (class_url != null) {
            val cparser = ClassParser(class_url.openStream(), class_url.toString())
            val bclass = cparser.parse()
            return ClassFileInfo(bclass)
        }

        // Some core classes have no associated .class files, search for those through reflection.
        if (!full_name.startsWith("java.") && !full_name.startsWith("javax."))
            return attempt { syscl.loadClass(full_name) } ?. let(::ReflectionClassInfo)

        return null
    }
}

// =================================================================================================

class PathClassLoader (urls: Array<URL>): URLClassLoader(urls)
{
    fun find_class_path (full_name: String): URL?
        = findResource(full_name.replace('.', '/') + ".class")

    // Lesson learned from playing with JAR files: many JAR files do not have entries
    // for their directories, only for their files.
}

// =================================================================================================

fun main (args: Array<String>)
{
    println(Test::class.java.classes.str)

//    val kinfo = Resolver.resolve_class("java.lang.String")
//    println(kinfo?.members("getBytes"))

    val klass = Resolver.resolve_class("norswap.lang.java8.resolution.Test")
    println(klass?.members)
    println(klass?.nested)
    println(klass?.members("Zor"))
}