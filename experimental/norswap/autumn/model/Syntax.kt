package norswap.autumn.model
import norswap.autumn.Grammar

// -------------------------------------------------------------------------------------------------

fun section (level: Int)
    = SectionBuilder(level)

// -------------------------------------------------------------------------------------------------

fun separator (level: Int)
    = SeparatorBuilder(level)

// -------------------------------------------------------------------------------------------------

fun <T: Builder> T.with (attribute: Any): T
{
    attributes.add(attribute)
    return this
}

// -------------------------------------------------------------------------------------------------

val String.code
    get() = CodeBuilder(this)

// -------------------------------------------------------------------------------------------------

operator fun String.not()
    = ReferenceBuilder(this)

// -------------------------------------------------------------------------------------------------

val String.parser
    get() = ParserCodeBuilder(this)

// -------------------------------------------------------------------------------------------------

val String.str
    get() = StrBuilder(this)

// -------------------------------------------------------------------------------------------------

operator fun String.unaryPlus()
    = WordBuilder(this)

// -------------------------------------------------------------------------------------------------

val String.token
    get() = StrTokenBuilder(this)

// -------------------------------------------------------------------------------------------------

val ParserBuilder.token
    get() = PlainTokenBuilder(this)

// -------------------------------------------------------------------------------------------------

fun ParserBuilder.token (value: (String) -> Any)
    = TokenBuilder(this, value)

// -------------------------------------------------------------------------------------------------

fun ParserBuilder.token (value: String)
    = TokenBuilderCode(this, value)

// -------------------------------------------------------------------------------------------------

val String.keyword
    get() = KeywordBuilder(this)

// -------------------------------------------------------------------------------------------------

val String.set
    get() = CharSetBuilder(this)

// -------------------------------------------------------------------------------------------------

infix fun Char.upto (c: Char)
    = CharRangeBuilder(this, c)

// -------------------------------------------------------------------------------------------------

operator fun ParserBuilder.rangeTo (right: ParserBuilder)
    = SeqBuilder(listOf(this, right))

// -------------------------------------------------------------------------------------------------

operator fun ParserBuilder.div (right: ParserBuilder)
    = ChoiceBuilder(listOf(this, right))

// -------------------------------------------------------------------------------------------------

fun longest (vararg builders: ParserBuilder)
    = LongestBuilder(builders.asList())

// -------------------------------------------------------------------------------------------------

fun token_choice (vararg builders: ParserBuilder)
    = TokenChoiceBuilder(builders.asList())

// -------------------------------------------------------------------------------------------------

val ParserBuilder.opt
    get() = OptBuilder(this)

// -------------------------------------------------------------------------------------------------

val ParserBuilder.ahead
    get() = AheadBuilder(this)

// -------------------------------------------------------------------------------------------------

val ParserBuilder.not
    get() = NotBuilder(this)

// -------------------------------------------------------------------------------------------------

val ParserBuilder.maybe
    get() = MaybeBuilder(this)

// -------------------------------------------------------------------------------------------------

val ParserBuilder.repeat0
    get() = Repeat0Builder(this)

// -------------------------------------------------------------------------------------------------

val ParserBuilder.repeat1
    get() = Repeat1Builder(this)

// -------------------------------------------------------------------------------------------------

fun ParserBuilder.repeat (n: Int)
    = RepeatNBuilder(n, this)

// -------------------------------------------------------------------------------------------------

val ParserBuilder.as_bool
    get() = AsBoolBuilder(this)

// -------------------------------------------------------------------------------------------------

fun ParserBuilder.as_val (value: Any?)
    = AsValBuilder(value, this)

// -------------------------------------------------------------------------------------------------

infix fun ParserBuilder.around0 (inside: ParserBuilder)
    = Around0Builder(this, inside)

// -------------------------------------------------------------------------------------------------

infix fun ParserBuilder.around1 (inside: ParserBuilder)
    = Around1Builder(this, inside)

// -------------------------------------------------------------------------------------------------

infix fun ParserBuilder.until0 (terminator: ParserBuilder)
    = Until0Builder(this, terminator)

// -------------------------------------------------------------------------------------------------

infix fun ParserBuilder.until1 (terminator: ParserBuilder)
    = Until1Builder(this, terminator)

// -------------------------------------------------------------------------------------------------

fun ParserBuilder.build (backlog: Int = 0, effect: Grammar.(Array<Any?>) -> Any)
    = BuildBuilder(backlog, this, effect)

// -------------------------------------------------------------------------------------------------

fun ParserBuilder.build (backlog: Int, effect: String)
    = BuildBuilderCode(backlog, this, effect)

// -------------------------------------------------------------------------------------------------

fun ParserBuilder.build (effect: String)
    = BuildBuilderCode(0, this, effect)

// -------------------------------------------------------------------------------------------------

fun ParserBuilder.affect (backlog: Int = 0, effect: Grammar.(Array<Any?>) -> Unit)
    = AffectBuilder(backlog, this, effect)

// -------------------------------------------------------------------------------------------------

fun ParserBuilder.affect (backlog: Int, effect: String)
    = AffectBuilderCode(backlog, this, effect)

// -------------------------------------------------------------------------------------------------

fun ParserBuilder.affect (effect: String)
    = AffectBuilderCode(0, this, effect)

// -------------------------------------------------------------------------------------------------

fun ParserBuilder.build_str (effect: Grammar.(String) -> Any)
    = BuildStrBuilder(this, effect)

// -------------------------------------------------------------------------------------------------

fun ParserBuilder.build_str (effect: String)
    = BuildStrBuilderCode(this, effect)

// -------------------------------------------------------------------------------------------------

val ParserBuilder.angles
    get() = AnglesBuilder(this)

// -------------------------------------------------------------------------------------------------

val ParserBuilder.squares
    get() = SquaresBuilder(this)

// -------------------------------------------------------------------------------------------------

val ParserBuilder.curlies
    get() = CurliesBuilder(this)

// -------------------------------------------------------------------------------------------------

val ParserBuilder.parens
    get() = ParensBuilder(this)

// -------------------------------------------------------------------------------------------------

val angles = EmptyAnglesBuilder

// -------------------------------------------------------------------------------------------------

val curlies = EmptyCurliesBuilder

// -------------------------------------------------------------------------------------------------

val squares = EmptySquaresBuilder

// -------------------------------------------------------------------------------------------------

val parens = EmptyParensBuilder

// -------------------------------------------------------------------------------------------------

val ParserBuilder.comma_list0
    get() = CommaList0Builder(this)

// -------------------------------------------------------------------------------------------------

val ParserBuilder.comma_list1
    get() = CommaList1Builder(this)

// -------------------------------------------------------------------------------------------------

val ParserBuilder.comma_list_term0
    get() = CommaListTerm0Builder(this)

// -------------------------------------------------------------------------------------------------

val ParserBuilder.comma_list_term1
    get() = CommaListTerm1Builder(this)

// -------------------------------------------------------------------------------------------------

fun assoc_left (init: AssocLeftBuilder.() -> Unit): AssocLeftBuilder
{
    val builder = AssocLeftBuilder()
    builder.init()
    return builder
}

// -------------------------------------------------------------------------------------------------