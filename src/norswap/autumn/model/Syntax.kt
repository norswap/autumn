package norswap.autumn.model
import norswap.autumn.Grammar

// -------------------------------------------------------------------------------------------------

operator fun String.not()
    = ReferenceBuilder(this)

// -------------------------------------------------------------------------------------------------

val String.code
    get() = CodeBuilder(this)

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
    get() = TokenBuilder(this, null)

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