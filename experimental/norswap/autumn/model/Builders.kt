package norswap.autumn.model
import norswap.autumn.Grammar
import norswap.utils.Visitable

// -------------------------------------------------------------------------------------------------

var order_next: Int = 0
    get() = field++
    set(x) { field = x }

// -------------------------------------------------------------------------------------------------

class section (val level: Int)
{
    val order = order_next
    lateinit var name: String
}

// -------------------------------------------------------------------------------------------------

abstract class ParserBuilder: Visitable<ParserBuilder>
{
    open var complete = true
    var name: String? = null
    val order = order_next
}

// -------------------------------------------------------------------------------------------------

abstract class LeafBuilder: ParserBuilder()
{
    override fun children() = emptySequence<ParserBuilder>()
}

// -------------------------------------------------------------------------------------------------

class ReferenceBuilder (val str: String): LeafBuilder()

// -------------------------------------------------------------------------------------------------

class CodeBuilder (val code: String): LeafBuilder()

// -------------------------------------------------------------------------------------------------

class StrBuilder (val str: String): LeafBuilder()

// -------------------------------------------------------------------------------------------------

class WordBuilder (val str: String): LeafBuilder()

// -------------------------------------------------------------------------------------------------

class StrTokenBuilder (val str: String): LeafBuilder()

// -------------------------------------------------------------------------------------------------

class TokenBuilder (child: ParserBuilder, val value: ((String) -> Any)?): WrapperBuilder(child)

// -------------------------------------------------------------------------------------------------

class TokenBuilderCode (child: ParserBuilder, val value: String): WrapperBuilder(child)

// -------------------------------------------------------------------------------------------------

class KeywordBuilder (val str: String): LeafBuilder()

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
    init { complete = false }

    val build: ParserBuilder
        get() = apply { complete = true }
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