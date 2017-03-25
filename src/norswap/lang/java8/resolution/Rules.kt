package norswap.lang.java8.resolution
import norswap.lang.java8.ast.ClassType
import norswap.lang.java8.ast.Import
import norswap.lang.java8.ast.Package
import norswap.lang.java8.ast.TypeDecl
import norswap.lang.java8.ast.TypeDeclKind.*
import norswap.lang.java8.typing.ClassLike
import norswap.lang.java8.typing.TObject
import norswap.uranium.AbstractNodeVisitor
import norswap.utils.except
import norswap.uranium.Attribute
import norswap.uranium.Node
import norswap.uranium.Reactor
import norswap.uranium.Rule
import norswap.uranium.Reaction
import kotlin.collections.listOf as list

// -------------------------------------------------------------------------------------------------

fun Reactor.install_java8_resolution_rules()
{
    val scope = ScopeBuilder()
    add_visitor(PackageRule(scope))
    add_visitor(ImportRule(scope))
    add_visitor(ClassTypeRule(scope))
    add_visitor(TypeDeclRule(scope))
}

// -------------------------------------------------------------------------------------------------

abstract class ResolutionRule <N: Node>: Rule<N>()
{
    override fun provided (node: N)
        = list(Attribute(node, "resolved"))

    fun Reaction<N>.resolve_class (full_name: String): ClassLike?
    {
        val klass = Resolver.resolve_class(full_name)
        if (klass == null)
            report(::ClassNotFoundResolutionError)
        return klass
    }

    fun Reaction<N>.resolve_members (full_name: String, member: String): List<MemberInfo>
    {
        val klass = resolve_class(full_name) ?: return emptyList()
        val members = klass.members(member)
        if (members.isEmpty())
            report(::MemberNotFoundResolutionError)
        return members
    }
}

// -------------------------------------------------------------------------------------------------

abstract class ScopeContributor<N: Node> (val scope: ScopeBuilder): AbstractNodeVisitor<N>()
{
    fun resolve_class (full_name: String): ClassLike?
    {
        val klass = Resolver.resolve_class(full_name)
        if (klass == null)
            reactor.register_error(ClassNotFoundScopeError())
        return klass
    }

    fun resolve_members (full_name: String, member: String): List<MemberInfo>
    {
        val klass = resolve_class(full_name) ?: return emptyList()
        val members = klass.members(member)
        if (members.isEmpty())
            reactor.register_error(MemberNotFoundScopeError())
        return members
    }
}

// -------------------------------------------------------------------------------------------------

abstract class ScopeMaker<N: Node> (scope: ScopeBuilder): ScopeContributor<N>(scope)
{
    abstract fun scope (node: N): Scope

    override fun visit (node: N, begin: Boolean)
    {
        if (begin)
            scope.push(scope(node))
        else
            scope.pop()
    }
}

// -------------------------------------------------------------------------------------------------

class PackageRule (scope: ScopeBuilder): ScopeMaker<Package>(scope)
{
    override fun scope (node: Package)
        = PackageScope(node.name.joinToString("."))
}

// -------------------------------------------------------------------------------------------------

class ImportRule (scope: ScopeBuilder): ScopeContributor<Import>(scope)
{
    override fun visit (node: Import, begin: Boolean)
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
            klass?.members()?.forEach { scope.put_member(it.name, it) }
        }
        else
        {
            val full_name = node.name.joinToString(".")
            val klass = resolve_class(full_name) ?: return
            scope.put_class_like(node.name.last(), klass)
        }
    }
}

// -------------------------------------------------------------------------------------------------

// TODO: most lookup rules will need a dependency on this

class SuperclassRule (val scope: ScopeBuilder): Rule<TypeDecl>()
{
    override fun provided (node: TypeDecl)
        = list(Attribute(node, "super_type"))

    override fun Reaction<TypeDecl>.compute()
    {
        when (node.kind) {
            ENUM        -> node["super_type"] = TObject
            INTERFACE   -> node["super_type"] = null
            ANNOTATION  -> node["super_type"] = null
            else        -> Unit
        }

        if (node.kind != CLASS) return

        if (node.extends.isEmpty()) {
            node["super_type"] = TObject
            return
        }

        val super_type = node.extends[0]
        if (super_type !is ClassType) {
            report(::ExtendingNonClass)
            return
        }

        val super_name = super_type.parts.map { it.name }
        val superclass = scope.type_chain(super_name)
        if (superclass == null)
            // TODO: super_type is not resolved attribute
            report(::ClassNotFoundResolutionError)
        else
            node["super_type"] = superclass
    }
}

// -------------------------------------------------------------------------------------------------

class TypeDeclRule (val scope: ScopeBuilder): ResolutionRule<TypeDecl>()
{
    override val domain = list(TypeDecl::class.java)

    override fun Reaction<TypeDecl>.compute()
    {
        val info = SourceClassInfo(scope.full_name(node.name), node)
        node["resolved"] = info
        scope.put_class_like(node.name, info)
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
            report(::ClassNotFoundResolutionError)
        else
            node["resolved"] = resolved
    }
}

// -------------------------------------------------------------------------------------------------