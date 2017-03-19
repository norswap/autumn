package norswap.lang.java8.resolution
import norswap.lang.java8.ast.ClassType
import norswap.lang.java8.ast.Import
import norswap.lang.java8.ast.Package
import norswap.lang.java8.ast.TypeDecl
import norswap.utils.except
import norswap.uranium.Attribute
import norswap.uranium.Node
import norswap.uranium.NodeVisitor
import norswap.uranium.Reactor
import norswap.uranium.Rule
import norswap.uranium.Reaction
import kotlin.collections.listOf as list

// -------------------------------------------------------------------------------------------------

fun Reactor.install_java8_resolution_rules()
{
    val scope = ScopeBuilder()
    add_visitor(ClassTypeRule(scope))
}

// -------------------------------------------------------------------------------------------------

abstract class ResolutionRule <N: Node>: Rule<N>()
{
    override fun provided (node: N)
        = list(Attribute(node, "resolved"))

    fun Reaction<N>.resolve_class (full_name: String): ClassInfo?
    {
        val klass = Resolver.resolve_class(full_name)
        if (klass == null)
            report(::ClassNotFoundError)
        return klass
    }

    fun Reaction<N>.resolve_members (full_name: String, member: String): List<MemberInfo>
    {
        val klass = resolve_class(full_name) ?: return emptyList()
        val members = klass.members(member)
        if (members.isEmpty())
            report(::MemberNotFoundError)
        return members
    }
}

// -------------------------------------------------------------------------------------------------

class PackageRule (val scope: ScopeBuilder): NodeVisitor<Package>
{
    override val domain = list(Package::class.java)

    override fun visit (node: Package, begin: Boolean)
    {
        scope.current = PackageScope(node.name.joinToString("."))
    }
}

// -------------------------------------------------------------------------------------------------

class ImportRule (val scope: ScopeBuilder): ResolutionRule<Import>()
{
    override val domain = list(Import::class.java)

    override fun Reaction<Import>.compute()
    {
        if (node.static)
        {
            val full_name = node.name.except().joinToString(".")
            val members = resolve_members(full_name, node.name.last())
            members.forEach { scope.put_member(it.name, it) }
        }

        else if (node.wildcard)
        {
            val full_name = node.name.joinToString(".")
            val klass = Resolver.resolve_class(full_name)
            klass?.members?.forEach { scope.put_member(it.name, it) }
        }
        else
        {
            val full_name = node.name.joinToString(".")
            val klass = resolve_class(full_name) ?: return
            scope.put_type(node.name.last(), klass)
        }
    }
}

// -------------------------------------------------------------------------------------------------

class TypeDeclRule (val scope: ScopeBuilder): ResolutionRule<TypeDecl>()
{
    override val domain = list(TypeDecl::class.java)

    override fun Reaction<TypeDecl>.compute()
    {
        // TODO
        // scope.put_type(node.name, node)
    }
}

// -------------------------------------------------------------------------------------------------

/**
 * Simplified version for now, to enable working with already compiled fully qualified classes.
 */
class ClassTypeRule (val scope: ScopeBuilder): ResolutionRule<ClassType>()
{
    override val domain = list(ClassType::class.java)

    override fun Reaction<ClassType>.compute()
    {
        val class_name = node.parts.map { it.name }.joinToString(".")
        val resolved = Resolver.resolve_class(class_name)
        if (resolved == null)
            report(::ClassNotFoundError)
        else
            node["resolved"] = resolved
    }
}

// -------------------------------------------------------------------------------------------------


interface Annotation

interface TypeParameter {
    val name: String
    val upper_bounds: List<TypeUse>
    val lower_bounds: List<TypeUse>
}

interface TypeDefinition {
    val name: List<String>
    val parent: TypeDefinition
    val parameters: List<TypeParameter>
}

interface TypeUse {
    val annotations: List<Annotation>
    val stem: TypeDefinition
    val arguments: List<TypeUse>
}