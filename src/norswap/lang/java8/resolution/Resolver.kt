package norswap.lang.java8.resolution
import norswap.lang.java8.ast.TypeDecl
import norswap.lang.java8.ast.TypeDeclKind
import norswap.lang.java8.typing.ClassLike
import norswap.lang.java8.typing.RefType
import norswap.lang.java8.typing.TypeParameter
import norswap.utils.multimap.*
import norswap.utils.attempt
import norswap.utils.cast
import org.apache.bcel.Const
import org.apache.bcel.classfile.ClassParser
import org.apache.bcel.classfile.ConstantUtf8
import org.apache.bcel.classfile.Field as BField
import org.apache.bcel.classfile.InnerClass
import org.apache.bcel.classfile.InnerClasses
import org.apache.bcel.classfile.JavaClass
import org.apache.bcel.classfile.Signature
import java.lang.reflect.TypeVariable
import org.apache.bcel.classfile.Method as BMethod
import java.lang.reflect.Field as JField
import java.lang.reflect.Method as JMethod
import java.net.URL
import java.net.URLClassLoader

// =================================================================================================

interface MemberInfo
{
    val name: String
}

// -------------------------------------------------------------------------------------------------

abstract class MethodInfo: MemberInfo
{
    override fun toString() = name
}

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

abstract class FieldInfo: MemberInfo
{
    override fun toString() = name
}

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

// =================================================================================================

open class ClassFileInfo (val bclass: JavaClass): ClassLike
{
    override val name
        = bclass.className.substringAfterLast(".")

    override val full_name
        = bclass.className!!

    override val kind = when
    {
        bclass.isClass      -> TypeDeclKind.CLASS
        bclass.isInterface  -> TypeDeclKind.INTERFACE
        bclass.isEnum       -> TypeDeclKind.ENUM
        bclass.isAnnotation -> TypeDeclKind.ANNOTATION
        else -> throw Error("implementation error: unknown class kind")
    }

    override val super_type
        by lazy { Resolver.resolve_class(bclass.superclassName)!! }

    override val fields      by lazy { compute_fields() }
    override val methods     by lazy { compute_methods() }
    override val class_likes by lazy { compute_class_likes() }
    override val type_params by lazy { compute_type_params() }

    private fun compute_fields(): MutableMap<String, FieldInfo>
        = bclass.fields.associateTo(HashMap()) { it.name to BytecodeFieldInfo(it) }

    private fun compute_methods(): MutableMultiMap<String, MethodInfo>
        = bclass.methods.multi_assoc { it.name to BytecodeMethodInfo(it) as MethodInfo }

    private fun compute_class_likes(): MutableMap<String, ClassLike>
    {
        val attr = bclass.attributes
            .filterIsInstance<InnerClasses>()
            .firstOrNull()
            ?: return HashMap<String, ClassLike>()

        return attr.innerClasses.associateTo(HashMap()) {
            val name = nested_class_name(it)
             name to Resolver.resolve_nested_class(this, name)!!
        }
    }

    private fun compute_type_params(): MutableMap<String, TypeParameter>
    {
        val sig = (bclass.attributes.first { it is Signature } as Signature).signature
        if (sig[0] != '<') return HashMap()
        // TODO parse type signature
        // spec: https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.3.4
        // e.g. https://github.com/JetBrains/jdk8u_jdk/blob/master/src/share/classes/sun/reflect/generics/parser/SignatureParser.java
        // or: https://jboss-javassist.github.io/javassist/html/javassist/CtClass.html#getGenericSignature--
        return HashMap()
    }

    private fun nested_class_name (nested: InnerClass): String
    {
        val index = nested.innerNameIndex
        if (index == 0) return "" // anonymous nested class
        val const = bclass.constantPool.getConstant(index, Const.CONSTANT_Utf8)
        return (const as ConstantUtf8).bytes
    }

    override fun toString() = full_name
}

// -------------------------------------------------------------------------------------------------

open class ReflectionClassInfo (val klass: Class<*>): ClassLike
{
    override val name = klass.simpleName!!

    override val full_name = klass.canonicalName!!

    override val kind = when
    {
        klass.isInterface  -> TypeDeclKind.INTERFACE
        klass.isEnum       -> TypeDeclKind.ENUM
        klass.isAnnotation -> TypeDeclKind.ANNOTATION
        else -> TypeDeclKind.CLASS
    }

    override val super_type
        = klass.superclass ?. let { ReflectionClassInfo(it) }

    override val fields      by lazy { compute_fields()  }
    override val methods     by lazy { compute_methods() }
    override val class_likes by lazy { compute_class_likes() }
    override val type_params by lazy { compute_type_params() }

    private fun compute_fields(): MutableMap<String, FieldInfo>
        = klass.fields.associateTo(HashMap()) { it.name to ReflectionFieldInfo(it) }

    private fun compute_methods(): MutableMultiMap<String, MethodInfo>
        = klass.methods.multi_assoc { it.name to ReflectionMethodInfo(it) as MethodInfo }

    private fun compute_class_likes(): MutableMap<String, ClassLike>
        = klass.classes.associateTo(HashMap()) { it.name to ReflectionClassInfo(it) }

    private fun compute_type_params(): MutableMap<String, TypeParameter>
        = klass.typeParameters.associateTo(HashMap()) { it.name to ReflectionTypeParameter(it) }

    override fun toString() = full_name
}

// -------------------------------------------------------------------------------------------------

class SourceClassInfo (override val full_name: String, val decl: TypeDecl): ClassLike
{
    override val name = decl.name

    override val kind: TypeDeclKind
        get() = decl.kind

    override val super_type: ClassLike
        get() = decl["super_type"].cast()

    override val fields: MutableMap<String, FieldInfo>
        get() = decl["fields"].cast()

    override val methods: MutableMultiMap<String, MethodInfo>
        get() = decl["methods"].cast()

    override val class_likes: MutableMap<String, ClassLike>
        get() = decl["class_likes"].cast()

    override val type_params: MutableMap<String, TypeParameter>
        get() = decl["type_params"].cast()

    override fun toString() = full_name
}

// -------------------------------------------------------------------------------------------------

class BytecodeTypeParameter: TypeParameter
{
    override val name: String
        get() = TODO()

    override val upper_bound: RefType
        get() = TODO()
}

// -------------------------------------------------------------------------------------------------

class ReflectionTypeParameter (val typevar: TypeVariable<*>) : TypeParameter
{
    override val name: String
        get() = typevar.name

    override val upper_bound: RefType
        get() = TODO()
}

// -------------------------------------------------------------------------------------------------

class SourceTypeParameter: TypeParameter
{
    override val name: String
        get() = TODO()

    override val upper_bound: RefType
        get() = TODO()
}

// -------------------------------------------------------------------------------------------------

// =================================================================================================

object Resolver
{
    private val class_cache = HashMap<String, ClassLike?>()
    private val syscl = ClassLoader.getSystemClassLoader() as URLClassLoader

    val urls = syscl.urLs

    val loader = PathClassLoader(urls)

    fun resolve_class (full_name: String): ClassLike?
        = class_cache.getOrPut(full_name) { seek_class(full_name) }

    fun resolve_class_chain (chain: List<String>): ClassLike?
    {
        top@ for (i in chain.indices) {
            val prefix = chain.subList(0, chain.size - i).joinToString(".")
            var klass = resolve_class(prefix) ?: continue
            for (j in 1..i) {
                val name = chain[chain.size - i - 1 + j]
                klass = resolve_nested_class(klass, name) ?: continue@top
            }
            return klass
        }
        return null
    }

    fun resolve_nested_class (klass: ClassLike, name: String): ClassLike?
    {
        val nested = klass.class_likes[name] ?: return null
        return resolve_class(klass.full_name + "$" + nested.name)
    }

    private fun seek_class (full_name: String): ClassLike?
    {
        val class_url = loader.find_class_path(full_name)

        if (class_url != null) {
            val cparser = ClassParser(class_url.openStream(), class_url.toString())
            val bclass = cparser.parse()
            return ClassFileInfo(bclass)
        }

        // Some core classes have no associated .class files, search for those through reflection.
        // TODO: which? when?
        if (full_name.startsWith("java.") || full_name.startsWith("javax."))
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