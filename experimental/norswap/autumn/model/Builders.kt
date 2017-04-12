package norswap.autumn.model
import norswap.autumn.Grammar
import norswap.utils.Visitable

// -------------------------------------------------------------------------------------------------

/**
 * Since builders are  gathered through reflection, their order is not guaranteed.
 * However, initialization order is guaranteed for fields.
 * By assigning each builder an order number at construction time, we can recover the file ordering.
  */
var order_next: Int = 0
    get() = field++
    set(x) { field = x }

// -------------------------------------------------------------------------------------------------

/**
 * An attribute that indicates that the generated code might need to provide a type hint
 * because of recursion.
 */
object TypeHint

// -------------------------------------------------------------------------------------------------

/**
 * A builder can be used to generate a file item.
 */
abstract class Builder : Visitable<Builder>
{
    val order = order_next
    val attributes: ArrayList<Any> = ArrayList()
    var name: String? = null
    override fun children() = emptySequence<Builder>()
}

// -------------------------------------------------------------------------------------------------

/**
 * Builder to generate section-type comments.
 */
class SectionBuilder (val level: Int): Builder()

// -------------------------------------------------------------------------------------------------

/**
 * Builder to generate separator comments.
 */
class SeparatorBuilder (val level: Int): Builder()

// -------------------------------------------------------------------------------------------------

/**
 * Builder for custom code.
 */
class CodeBuilder (val code: String): Builder()

// -------------------------------------------------------------------------------------------------

/**
 * Builder for parsers.
 */
abstract class ParserBuilder: Builder()

// -------------------------------------------------------------------------------------------------

/**
 * Builder for parsers that do not have sub-parsers.
 */
abstract class LeafBuilder: ParserBuilder()

// -------------------------------------------------------------------------------------------------

/**
 * Builder for recursive reference to other parsers.
 * (Non-recursive references can be put in as identifiers.)
 */
class ReferenceBuilder (val str: String): LeafBuilder()

// -------------------------------------------------------------------------------------------------

/**
 * Builder for custom parsing code. The code does not need to include any wrapping: it should
 * be the body part of a `() -> Boolean` lambda.
 */
class ParserCodeBuilder (val code: String): LeafBuilder()

// -------------------------------------------------------------------------------------------------

/**
 * Builder for a string-recognition parser.
 */
class StrBuilder (val str: String): LeafBuilder()

// -------------------------------------------------------------------------------------------------

/**
 * Builder for a word-recognition parser (string + trailing whitespace).
 */
class WordBuilder (val str: String): LeafBuilder()

// -------------------------------------------------------------------------------------------------

/**
 * Builder for string-recognition parsers that register as tokens.
 */
class StrTokenBuilder (val str: String): LeafBuilder()

// -------------------------------------------------------------------------------------------------

/**
 * Builder for a token-building parser.
 * The code that builds the token is given as a function.
 * This is suitable for code generation, but not for live construction.
 */
class TokenBuilder (child: ParserBuilder, val value: ((String) -> Any)?): WrapperBuilder(child)

// -------------------------------------------------------------------------------------------------

/**
 * Builder for a token-building parser that takes no code block.
 */
class PlainTokenBuilder (child: ParserBuilder): WrapperBuilder(child)

// -------------------------------------------------------------------------------------------------

/**
 * Builder for a token-building parser.
 * The code that builds the token is represented by a string.
 * This is suitable for code generation, but not for live construction.
 */
class TokenBuilderCode (child: ParserBuilder, val value: String): WrapperBuilder(child)

// -------------------------------------------------------------------------------------------------

/**
 * Builder for keywords: words recognized as tokens but generating no token data.
 */
class KeywordBuilder (val str: String): LeafBuilder()

// -------------------------------------------------------------------------------------------------

/**
 * A parser that makes a choice amongst multiple tokens.
 */
class TokenChoiceBuilder(val list: List<ParserBuilder>): ParserBuilder()

// -------------------------------------------------------------------------------------------------

class CharRangeBuilder (val start: Char, val end: Char): LeafBuilder()

// -------------------------------------------------------------------------------------------------

class CharSetBuilder (val str: String): LeafBuilder()
{
    constructor (vararg c: Char): this(String(c))
}

// -------------------------------------------------------------------------------------------------

abstract class ContainerBuilder (val list: List<ParserBuilder>): ParserBuilder()
{
    override fun children() = list.asSequence()
}

// -------------------------------------------------------------------------------------------------

abstract class IncompleteContainerBuilder (list: List<ParserBuilder>): ContainerBuilder(list)
{
    val end: ParserBuilder = this
}

// -------------------------------------------------------------------------------------------------

class SeqBuilder (list: List<ParserBuilder>): IncompleteContainerBuilder(list)
{
    operator fun rangeTo (right: ParserBuilder)
        = SeqBuilder(list + right)
}

// -------------------------------------------------------------------------------------------------

class ChoiceBuilder (list: List<ParserBuilder>): IncompleteContainerBuilder(list)
{
    operator fun div (right: ParserBuilder)
        = ChoiceBuilder(list + right)
}

// -------------------------------------------------------------------------------------------------

class LongestBuilder (list: List<ParserBuilder>): ContainerBuilder(list)
{
    fun add (right: ParserBuilder) = LongestBuilder(list + right)
}

// -------------------------------------------------------------------------------------------------

/**
 * Builder for parsers that match a child parser bracketed by some strings.
 */
abstract class BracketsBuilder (val left: String, val right: String, child: ParserBuilder)
    : WrapperBuilder(child)

// -------------------------------------------------------------------------------------------------

class AnglesBuilder     (child: ParserBuilder): BracketsBuilder("<", ">", child)
class SquaresBuilder    (child: ParserBuilder): BracketsBuilder("[", "]", child)
class CurliesBuilder    (child: ParserBuilder): BracketsBuilder("{", "}", child)
class ParensBuilder     (child: ParserBuilder): BracketsBuilder("(", ")", child)

// -------------------------------------------------------------------------------------------------

/**
 * Builder for parsers that match an empty pair of brackets.
 */
abstract class EmptyBracketsBuilder (val left: String, val right: String): LeafBuilder()

// -------------------------------------------------------------------------------------------------

object EmptyAnglesBuilder   : EmptyBracketsBuilder("<", ">")
object EmptySquaresBuilder  : EmptyBracketsBuilder("[", "]")
object EmptyCurliesBuilder  : EmptyBracketsBuilder("{", "}")
object EmptyParensBuilder   : EmptyBracketsBuilder("(", ")")

// -------------------------------------------------------------------------------------------------

abstract class WrapperBuilder(val child: ParserBuilder): ParserBuilder()
{
    override fun children() = sequenceOf(child)
}

// -------------------------------------------------------------------------------------------------

class AheadBuilder      (child: ParserBuilder): WrapperBuilder(child)
class NotBuilder        (child: ParserBuilder): WrapperBuilder(child)
class OptBuilder        (child: ParserBuilder): WrapperBuilder(child)
class MaybeBuilder      (child: ParserBuilder): WrapperBuilder(child)
class AsBoolBuilder     (child: ParserBuilder): WrapperBuilder(child)
class Repeat0Builder    (child: ParserBuilder): WrapperBuilder(child)
class Repeat1Builder    (child: ParserBuilder): WrapperBuilder(child)

// -------------------------------------------------------------------------------------------------

class AsValBuilder (val value: Any?, child: ParserBuilder): WrapperBuilder(child)

// -------------------------------------------------------------------------------------------------

class RepeatNBuilder (val n: Int, child: ParserBuilder): WrapperBuilder(child)

// -------------------------------------------------------------------------------------------------

class Around0Builder (val around: ParserBuilder, val inside: ParserBuilder): ParserBuilder()
{
    override fun children() = sequenceOf(around, inside)
}

// -------------------------------------------------------------------------------------------------

class Around1Builder (val around: ParserBuilder, val inside: ParserBuilder): ParserBuilder()
{
    override fun children() = sequenceOf(around, inside)
}

// -------------------------------------------------------------------------------------------------

class Until0Builder (val repeat: ParserBuilder, val terminator: ParserBuilder): ParserBuilder()
{
    override fun children() = sequenceOf(repeat, terminator)
}

// -------------------------------------------------------------------------------------------------

class Until1Builder (val repeat: ParserBuilder, val terminator: ParserBuilder): ParserBuilder()
{
    override fun children() = sequenceOf(repeat, terminator)
}

// -------------------------------------------------------------------------------------------------

class BuildBuilder (val backlog: Int, syntax: ParserBuilder, val effect: Grammar.(Array<Any?>) -> Any?)
    : WrapperBuilder(syntax)

// -------------------------------------------------------------------------------------------------

class BuildBuilderCode (val backlog: Int, syntax: ParserBuilder, val effect: String)
    : WrapperBuilder(syntax)

// -------------------------------------------------------------------------------------------------

class AffectBuilder (val backlog: Int, syntax: ParserBuilder, val effect: Grammar.(Array<Any?>) -> Unit)
    : WrapperBuilder(syntax)

// -------------------------------------------------------------------------------------------------

class AffectBuilderCode (val backlog: Int, syntax: ParserBuilder, val effect: String)
    : WrapperBuilder(syntax)

// -------------------------------------------------------------------------------------------------

class BuildStrBuilder (syntax: ParserBuilder, val effect: Grammar.(String) -> Any)
    : WrapperBuilder(syntax)

// -------------------------------------------------------------------------------------------------

class BuildStrBuilderCode (syntax: ParserBuilder, val effect: String)
    : WrapperBuilder(syntax)

// -------------------------------------------------------------------------------------------------

/**
 * Builder for a parser of comma-separated lists of items (0+ items).
 */
class CommaList0Builder (child: ParserBuilder): WrapperBuilder(child)

// -------------------------------------------------------------------------------------------------

/**
 * Builder for a parser of comma-separated lists of items (1+ items).
 */
class CommaList1Builder (child: ParserBuilder): WrapperBuilder(child)

// -------------------------------------------------------------------------------------------------

/**
 * Builder for a parser of comma-separated lists of items, with optional terminating comma (0+ items).
 */
class CommaListTerm0Builder (child: ParserBuilder): WrapperBuilder(child)

// -------------------------------------------------------------------------------------------------

/**
 * Builder for a parser of comma-separated lists of items, with optional terminating comma (1+ items).
 */
class CommaListTerm1Builder (child: ParserBuilder): WrapperBuilder(child)

// -------------------------------------------------------------------------------------------------

class AssocLeftBuilder: ParserBuilder()
{
    val operators = ArrayList<OperatorBuilder>()

    var left: ParserBuilder? = null
    var right: ParserBuilder? = null
    var operands: ParserBuilder? = null
    var strict: Boolean? = null

    fun op (kind: String, parser: ParserBuilder): OperatorBuilder {
        val operator = OperatorBuilder(kind, parser)
        operators += operator
        return operator
    }

    fun op_stackless (parser: ParserBuilder) = op("op_stackless", parser)
    fun op_affect(parser: ParserBuilder) = op("op_affect", parser)
    fun op (parser: ParserBuilder) = op("op", parser)

    fun postfix_stackless (parser: ParserBuilder) = op("postfix_stackless", parser)
    fun postfix_affect(parser: ParserBuilder) = op("postfix_affect", parser)
    fun postfix (parser: ParserBuilder) = op("postfix", parser)
}

// -------------------------------------------------------------------------------------------------

class OperatorBuilder (val kind: String, val parser: ParserBuilder): WrapperBuilder(parser)
{
    var effect: String = ""

    fun effect (effect: String): OperatorBuilder {
        this.effect = effect
        return this
    }
}

// -------------------------------------------------------------------------------------------------