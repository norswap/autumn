@file:Suppress("PackageDirectoryMismatch")
package norswap.lang.java4.ast
import norswap.whimsy.Node
import norswap.whimsy.ast_utils.*
import kotlin.sequences.sequenceOf as seq

// Expressions -------------------------------------------------------------------------------------

interface Expr: Stmt

object Null
: Node(), Expr

data class Literal (
    val value: Any)
: Node(), Expr

// Types -------------------------------------------------------------------------------------------

interface Type

data class PrimitiveType (
    val name: String)
    : Node(), Type

object Void
    : Node(), Type

interface RefType
    : Type

data class ClassType (
    val parts: List<String>)
    : Node(), RefType

data class ArrayType (
    val stem: Type,
    val dims: Int)
    : Node(), RefType
{
    override fun children() = nseq(stem)
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

data class ArrayInit (
    val items: List<Expr>)
    : Node(), Expr
{
    override fun children() = items.nseq
}

data class ArrayCtorCall (
    val type: Type,
    val dim_exprs: List<Expr>,
    val dims: Int,
    val init: Expr?)
    : Node(), Expr
{
    override fun children() = type + dim_exprs.nseq + nseqN(init)
}

data class CtorCall (
    val type: Type,
    val args: List<Expr>,
    val body: List<Decl>?)
    : Node(), Expr
{
    override fun children() = type + args.nseq + body.nseq
}

data class ParenExpr (
    val expr: Expr)
    : Node(), Expr
{
    override fun children() = nseq(expr)
}

data class MethodCall (
    val op: Expr?,
    val name: String,
    val args: List<Expr>)
    : Node(), Expr
{
    override fun children() = nseqN(op) + args.nseq
}

// -------------------------------------------------------------------------------------------------
// -------------------------------------------------------------------------------------------------
// -------------------------------------------------------------------------------------------------

interface Stmt
interface Decl