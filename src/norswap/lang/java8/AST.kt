@file:Suppress("PackageDirectoryMismatch")
package norswap.lang.java8.ast
import norswap.whimsy.Node
import norswap.whimsy.ast_utils.*

// Annotations -------------------------------------------------------------------------------------

interface AnnotationElement

interface Annotation: AnnotationElement

data class AnnotationElementList(
    val elems: List<AnnotationElement>)
: Node(), AnnotationElement
{
    override fun children() = elems.nseq
}

data class NormalAnnotation (
    val name: List<String>,
    val elems: List<Pair<String, AnnotationElement>>)
: Node(), Annotation
{
    override fun children() = elems.nmap { it.second }
}

data class MarkerAnnotation (
    val name: List<String>)
: Node(), Annotation

data class SingleElementAnnotation (
    val name: List<String>,
    val elem: AnnotationElement)
: Node(), Annotation
{
    override fun children() = nseq(elem)
}

// Expressions -------------------------------------------------------------------------------------

interface Expr: AnnotationElement, Stmt

object Null
    : Node(), Expr

data class Literal (
    val value: Any)
    : Node(), Expr

// Types -------------------------------------------------------------------------------------------

abstract class TypeBound: Node()
{
    abstract val type: Type
    override fun children() = nseq(type)
}

data class SuperBound   (override val type: Type): TypeBound()
data class ExtendsBound (override val type: Type): TypeBound()

interface Type

data class PrimitiveType (
    val ann: List<Annotation>,
    val name: String)
    : Node(), Type
{
    override fun children() = ann.nseq
}

object Void
    : Node(), Type

interface RefType
    : Type

data class Wildcard (
    val ann: List<Annotation>,
    val bound: TypeBound?)
    : Node(), RefType
{
    override fun children() = ann.nseq + bound
}

data class ClassType (
    val parts: List<ClassTypePart>)
    : Node(), RefType
{
    override fun children() = parts.nseq
}

data class ClassTypePart (
    val ann: List<Annotation>,
    val name: String,
    val targs: List<Type>)
    : Node()
{
    override fun children() = ann.nseq + targs.nseq
}

data class ArrayType (
    val stem: Type,
    val dims: List<Dimension>)
    : Node(), RefType
{
    override fun children() = stem + dims.nseq
}

data class Dimension (
    val ann: List<Annotation>)
    : Node()
{
    override fun children() = ann.nseq
}

data class TypeParam (
    val ann: List<Annotation>,
    val name: String,
    val bounds: List<Type>)
    : Node()
{
    override fun children() = ann.nseq + bounds.nseq
}

// Expressions -------------------------------------------------------------------------------------

object Super
    : Node(), Expr

object This
    : Node(), Expr

data class SuperCall (
    val args: List<Expr>)
    : Node(), Expr
{
    override fun children() = args.nseq
}

data class ThisCall (
    val args: List<Expr>)
    : Node(), Expr
{
    override fun children() = args.nseq
}

data class Identifier (
    val name: String)
    : Node(), Expr

data class ClassExpr (
    val type: Type)
    : Node(), Expr
{
    override fun children() = nseq(type)
}

data class DimExpr (
    val ann: List<Annotation>,
    val expr: Expr)
    : Node()
{
    override fun children() = ann.nseq + expr
}

data class ArrayInit (
    val items: List<Expr>)
    : Node(), Expr
{
    override fun children() = items.nseq
}

data class ArrayCtorCall (
    val type: Type,
    val dim_exprs: List<DimExpr>,
    val dims: List<Dimension>,
    val init: Expr?)
    : Node(), Expr
{
    override fun children() = type + dim_exprs.nseq + dims.nseq + nseqN(init)
}

data class CtorCall (
    val targs: List<Type>,
    val type: Type,
    val args: List<Expr>,
    val body: List<Decl>?)
    : Node(), Expr
{
    override fun children() = targs.nseq + type + args.nseq + body.nseq
}

data class ParenExpr (
    val expr: Expr)
    : Node(), Expr
{
    override fun children() = nseq(expr)
}

abstract class UnaryExpression: Node(), Expr
{
    abstract val op: Expr
    override fun children() = nseq(op)
}

data class MethodCall (
    val op: Expr?,
    val targs: List<Type>,
    val name: String,
    val args: List<Expr>)
    : Node(), Expr
{
    override fun children() = nseqN(op) + targs.nseq + args.nseq
}

data class DotIden (
    override val op: Expr,
    val name: String)
    : UnaryExpression()

data class DotThis (
    override val op: Expr)
    : UnaryExpression()

data class DotSuper (
    override val op: Expr)
    :  UnaryExpression()

data class DotNew (
    override val op: Expr,
    val ctor: CtorCall)
    : UnaryExpression()
{
    override fun children() = nseq(op, ctor)
}

data class ArrayAccess (
    override val op: Expr,
    val index: Expr)
    : UnaryExpression()
{
    override fun children() = nseq(op, index)
}

data class PostIncrement (override val op: Expr): UnaryExpression()
data class PostDecrement (override val op: Expr): UnaryExpression()
data class PreIncrement  (override val op: Expr): UnaryExpression()
data class PreDecrement  (override val op: Expr): UnaryExpression()

data class MaybeBoundMethodReference (
    val type: Type,
    val targs: List<Type>,
    val name: String)
    : Node(), Expr
{
    override fun children() = type + targs.nseq
}

data class BoundMethodReference(
    val receiver: Expr,
    val targs: List<Type>,
    val name: String)
    : Node(), Expr
{
    override fun children() = receiver + targs.nseq
}

data class NewReference (
    val type: Type,
    val targs: List<Type>)
    : Node(), Expr
{
    override fun children() = type + targs.nseq
}

data class Cast (
    val types: List<Type>,
    override val op: Expr)
    : UnaryExpression()
{
    override fun children() = types.nseq + op
}

abstract class UnaryOp: UnaryExpression()

data class UnaryPlus    (override val op: Expr): UnaryOp()
data class UnaryMinus   (override val op: Expr): UnaryOp()
data class Complement   (override val op: Expr): UnaryOp()
data class Not          (override val op: Expr): UnaryOp()

abstract class BinaryOp: Node(), Expr
{
    abstract val left: Expr
    abstract val right: Expr
    override fun children() = nseq(left, right)
}

data class Product          (override val left: Expr, override val right: Expr): BinaryOp()
data class Division         (override val left: Expr, override val right: Expr): BinaryOp()
data class Remainder        (override val left: Expr, override val right: Expr): BinaryOp()
data class Sum              (override val left: Expr, override val right: Expr): BinaryOp()
data class Diff             (override val left: Expr, override val right: Expr): BinaryOp()
data class ShiftLeft        (override val left: Expr, override val right: Expr): BinaryOp()
data class ShiftRight       (override val left: Expr, override val right: Expr): BinaryOp()
data class BinaryShiftRight (override val left: Expr, override val right: Expr): BinaryOp()
data class Lower            (override val left: Expr, override val right: Expr): BinaryOp()
data class LowerEqual       (override val left: Expr, override val right: Expr): BinaryOp()
data class Greater          (override val left: Expr, override val right: Expr): BinaryOp()
data class GreaterEqual     (override val left: Expr, override val right: Expr): BinaryOp()
data class Equal            (override val left: Expr, override val right: Expr): BinaryOp()
data class NotEqual         (override val left: Expr, override val right: Expr): BinaryOp()
data class BinaryAnd        (override val left: Expr, override val right: Expr): BinaryOp()
data class Xor              (override val left: Expr, override val right: Expr): BinaryOp()
data class BinaryOr         (override val left: Expr, override val right: Expr): BinaryOp()
data class And              (override val left: Expr, override val right: Expr): BinaryOp()
data class Or               (override val left: Expr, override val right: Expr): BinaryOp()

data class Assign (
    override val left: Expr,
    override val right: Expr,
    val op: String)
    : BinaryOp()

data class Instanceof(
    val op: Expr,
    val type: Type)
    : Node(), Expr
{
    override fun children() = nseq(op, type)
}

data class Ternary (
    val cond: Expr,
    val ifPart: Expr,
    val elsePart: Expr)
    : Node(), Expr
{
    override fun children() = nseq(cond, ifPart, elsePart)
}

// NOTE: body can be an expression
data class Lambda (
    val params: Parameters,
    val body: Stmt)
    : Node(), Expr
{
    override fun children() = nseq(params, body)
}

// -------------------------------------------------------------------------------------------------
// -------------------------------------------------------------------------------------------------
// -------------------------------------------------------------------------------------------------

// Declarations  -----------------------------------------------------------------------------------

interface Modifier

enum class Keyword: Modifier {
    abstract,
    default,
    final,
    native,
    private,
    protected,
    public,
    static,
    synchronized,
    strictfp,
    transient,
    volatile
}

class Annnotation: Modifier

interface FormalParameter

data class IdenParameter (
        val mods: List<Modifier>,
        val type: Type,
        val name: String,
        val dims: List<Dimension>)
: FormalParameter

data class ThisParameter (
        val mods: List<Modifier>,
        val type: Type,
        val qualifier: List<String>)
: FormalParameter

data class VariadicParameter (
        val mods: List<Modifier>,
        val type: Type,
        val arrayMods: List<Annotation>,
        val name: String)
: FormalParameter

interface Parameters

data class FormalParameters (
        val params: List<FormalParameters>)
: Parameters

data class UntypedParameters (
        val params: List<String>)
: Parameters

////

data class Package (
        val ann: List<Annotation>,
        val name: List<String>)

data class Import (
        val static: Boolean,
        val name: List<String>,
        val wildcard: Boolean)

////

interface Decl: Stmt

data class EnumConstant (
        val ann: List<Annotation>,
        val name: String,
        val params: List<Expr>?,
        val body: List<Decl>?)

enum class TypeDeclKind { ANNOTATION, CLASS, ENUM, INTERFACE }

data class TypeDecl (
    val kind: TypeDeclKind,
    val mods: List<Remainder>,
    val name: String,
    val tparams: List<TypeParam>,
    val extends: List<Type>,
    val implements: List<Type>,
    val decls: List<Decl>)
: Decl

data class EnumDecl (
        val decl: TypeDecl,
        val constants: List<EnumConstant>)
: Decl

data class AnnotationElemDecl (
    val mods: List<Remainder>,
    val type: Type,
    val name: String,
    val dims: List<Dimension>,
    val value: AnnotationElement?)
: Decl

/*

AST concerns

- easy to build (?)

- syntactic fidelity
    - i.e. desugarings?

- DRY
    - class and interfaces almost identical in structure
        - common ancestor?      -> verbose
        - separate?             -> verbose, no common interface
        - enum discrimination?  -> unsafe accesses

- error handling
    - e.g. have an extends field for interfaces
        - can't possibly be right
        - but leads to better error reporting during validation
    - on the parsing side: vs additional parsing work

- extensibility
    - attributes (e.g. typing)
    - new computations (with caching in attributes?)

trade-offs

- validation vs unsafe accesses
    - two interfaces?   -> verbose

- uniformity (apply desugarings) vs syntactic fidelity
    - use (cached?) syntactic predicates

- validation vs nice interface
    - e.g. multiple vs single extends vs no extends, implements vs not
    - use an "implements" for interface extends and class implements?

--> language + code generation?

2.

- may need multiple levels of grammar
    - speed for correct files
    - details for incorrect ones

- I made arrayInit an expr, but not one that can be acquired via expr

 */

// Statements --------------------------------------------------------------------------------------

interface Stmt

data class Block (
        val stmts: List<Stmt>)
: Stmt

data class If (
        val cond: Expr,
        val ifPart: Stmt,
        val elsePart: Stmt?)
: Stmt

data class BasicFor (
        val init: List<Stmt>,
        val cond: Expr?,
        val iter: List<Stmt>,
        val body: Stmt)
: Stmt

data class EnhancedFor (
        val mods: List<Modifier>,
        val type: Type,
        val declarator: VarDeclaratorID,
        val iter: Expr,
        val body: Stmt)
: Stmt

data class WhileStmt (
        val cond: Expr,
        val body: Stmt)
: Stmt

data class DoWhileStmt (
        val body: Stmt,
        val cond: Expr)
: Stmt

data class SynchronizedStmt (
        val expr: Expr,
        val body: Block)
: Stmt

data class ReturnStmt (val expr: Expr?): Stmt
data class ThrowStmt  (val expr: Expr): Stmt

data class BreakStmt    (val label: String?): Stmt
data class ContinueStmt (val label: String?): Stmt

data class AssertStmt (
        val expr: Expr,
        val msg: Expr?)
: Stmt

data class LabelledStmt (
        val label: String,
        val stmt: Stmt)
: Stmt

data class CatchClause (
        val mods: List<Modifier>,
        val types: List<Type>,
        val id: VarDeclaratorID,
        val body: Block)

data class TryResource (
        val mods: List<Modifier>,
        val type: Type,
        val id: VarDeclaratorID,
        val value: Expr)

data class TryStmt (
        val resources: List<TryResource>,
        val body: Block,
        val catch: List<CatchClause>,
        val finally: Block?)
: Stmt

interface SwitchLabel

data class CaseLabel (
        val expr: Expr)
: SwitchLabel

object DefaultLabel
: SwitchLabel

data class SwitchClause (
        val label: SwitchLabel,
        val stmts: List<Stmt>)

data class SwitchStmt (
        val expr: Expr,
        val clauses: List<SwitchClause>)
: Stmt

data class VarDeclaratorID (
        val iden: String,
        val dims: List<Dimension>)

data class VarDeclarator (
        val id: VarDeclaratorID,
        val init: Expr?)

data class VarDecl (
        val mods: List<Modifier>,
        val type: Type,
        val declarators: List<VarDeclarator>)
: Decl

object SemiStmt: Stmt

data class MethodDecl (
    val mods: List<Remainder>,
    val tparams: List<TypeParam>,
    val retType: Type,
    val name: String,
    val params: FormalParameters,
    val dims: List<Dimension>,
    val throwing: List<Type>,
    val body: Block?)
: Decl

data class InitBlock (
        val static: Boolean,
        val block: Block)
: Decl

data class ConstructorDecl (
    val mods: List<Remainder>,
    val tparams: List<TypeParam>,
    val name: String,
    val params: FormalParameters,
    val throwing: List<Type>,
    val body: Block)
: Decl

// Root --------------------------------------------------------------------------------------------

data class File (
        val pkg: Package?,
        val imports: List<Import>,
        val typeDecls: List<Decl>)
