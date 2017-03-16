package norswap.autumn.model
import norswap.lang.java_base.escape
import norswap.lang.java8.Java8Model
import norswap.utils.*

// -------------------------------------------------------------------------------------------------

fun main (args: Array<String>)
{
    val str = compile_model("Scratch", Java8Model())
    println(str)
}

// -------------------------------------------------------------------------------------------------

val overrides = listOf("whitespace", "root")

val kotlin_keywords = listOf(
    "package", "as", "typealias", "class", "this", "super", "val", "var", "fun", "for", "null",
    "true", "false", "is", "in", "throw", "return", "break", "continue", "object", "if", "try",
    "else", "while", "do", "when", "interface", "typeof", "_")

val equal_same_line = listOf<Class<*>>(
    BuildBuilder        ::class.java,
    BuildBuilderCode    ::class.java,
    AffectBuilder       ::class.java,
    AffectBuilderCode   ::class.java)

// -------------------------------------------------------------------------------------------------

fun String.kotlin_getter_to_val_name(): String
{
    var name = removePrefix("get").decapitalize()

    if (!name[0].isJavaIdentifierStart() || kotlin_keywords.contains(name))
        name = "`$name`"

    return name
}

// -------------------------------------------------------------------------------------------------

fun compile_model (klass_name: String, model: Any): String
{
    order_next = 0

    val b = StringBuilder()

    b += "package norswap.lang.java8\n"
    b += "import norswap.autumn.Parser\n"
    b += "import norswap.autumn.TokenGrammar\n"
    b += "import norswap.autumn.parsers.*\n"
    b += "import norswap.lang.java_base.*\n"
    b += "import norswap.lang.java8.ast.*\n"
    b += "import norswap.lang.java8.ast.TypeDeclKind.*\n\n"

    b += "class $klass_name: TokenGrammar()\n{"

    model::class.java.methods

        .filter { supers <Builder> (it.returnType) }

        .map {
            val builder = it.invoke(model) as Builder
            builder.name = it.name.kotlin_getter_to_val_name()
            builder
        }

        .sortedBy { it.order }

        .forEach {
            b += "\n\n"

            when (it) {
                is SectionBuilder -> {
                    b += "    /// "
                    b += it.name?.capitalize()
                    b += " "
                    b += "=".repeat(91 - it.name!!.length)
                }

                is CodeBuilder -> {
                    b += it.code.prependIndent("    ")
                }

                is ParserBuilder ->
                {
                    val (func, str) = str_compiler(it)
                    b += "    "
                    if (overrides.contains(it.name)) b += "override "
                    if (func) {
                        b += "fun "
                        b += it.name
                        b += "()"
                        if (it.attributes.contains(TypeHint))
                            b += " : Boolean"
                        if (equal_same_line.contains(it::class.java))
                            b += " = $str"
                        else
                            b += "\n        = $str"
                    }
                    else {
                        b += "val "
                        b += it.name
                        if (equal_same_line.contains(it::class.java))
                            b += " = $str"
                        else
                            b += "\n        = $str"
                    }
                }
            }
        }

    b += "\n}"
    return b.toString()
}

// -------------------------------------------------------------------------------------------------

fun Poly1<ParserBuilder, String>.digest(p: ParserBuilder): String
    = p.name ?. let { "$it()" } ?: invoke(p)

// -------------------------------------------------------------------------------------------------

inline fun <reified T: WrapperBuilder> Poly1<ParserBuilder, String>
    .on(prefix: String)
{
    on <T> {
        "$prefix { ${digest(it.child)} }"
    }
}

// -------------------------------------------------------------------------------------------------

val model_compiler = Poly1 <ParserBuilder, String>().apply {

    on <ReferenceBuilder> {
        it.str.camel_to_snake() + "()"
    }

    on <ParserCodeBuilder> {
        it.code
    }

    on <StrBuilder> {
        "\"${it.str.escape()}\".str"
    }

    on <WordBuilder> {
        "\"${it.str.escape()}\".word"
    }

    on <StrTokenBuilder> {
        "\"${it.str.escape()}\".token"
    }

    on <TokenBuilder> {
        "token ({ TODO() }) { ${digest(it.child)} }"
    }

    on <TokenBuilderCode> {
        "token ({ ${it.value} }) { ${digest(it.child)} }"
    }

    on <KeywordBuilder> {
        "\"${it.str.escape()}\".keyword"
    }

    on <CharRangeBuilder> {
        "char_range('${it.start}', '${it.end}')"
    }

    on <CharSetBuilder> {
        "\"${it.str.escape()}\".set"
    }

    on <SeqBuilder> {
        val children = it.list
            .map { digest(it) }
            .joinToString(separator = " && ")
        "seq { $children }"
    }

    on <ChoiceBuilder> {
        val children = it.list
            .map { digest(it) }
            .joinToString(separator = " || ")
        "choice { $children }"
    }

    on <LongestBuilder> {
        val children = it.list
            .map { it.name ?: "{ ${invoke(it)} }" }
            .joinToString(separator = " , ")
        "longest ( $children )"
    }

    on <TokenChoiceBuilder> {
        val children = it.list
            .map { it.name }
            .joinToString()
        "token_choice { $children }"
    }

    on <AheadBuilder>   ("ahead")
    on <NotBuilder>     ("not")
    on <OptBuilder>     ("opt")
    on <MaybeBuilder>   ("maybe")
    on <AsBoolBuilder>  ("as_bool")
    on <Repeat0Builder> ("repeat0")
    on <Repeat1Builder> ("repeat1")
    on <AnglesBuilder>  ("angles")
    on <CurliesBuilder> ("curlies")
    on <SquaresBuilder> ("squares")
    on <ParensBuilder>  ("squares")

    on <EmptyAnglesBuilder>  { "angles()"  }
    on <EmptyCurliesBuilder> { "curlies()" }
    on <EmptySquaresBuilder> { "squares()" }
    on <EmptyParensBuilder>  { "parens()"  }

    on <CommaList0Builder>      ("comma_list0")
    on <CommaList1Builder>      ("comma_list1")
    on <CommaListTerm0Builder>  ("comma_list_term0")
    on <CommaListTerm1Builder>  ("comma_list_term1")

    on <AsValBuilder> {
        "as_val (${it.value}) { ${digest(it.child)} }"
    }

    on <RepeatNBuilder> {
        "repeat(${it.n}) { ${digest(it.child)} }"
    }

    on <Around0Builder> {
        "around0 ( {${digest(it.around)}} , {${digest(it.inside)}} )"
    }

    on <Around1Builder> {
        "around1 ( {${digest(it.around)}} , {${digest(it.inside)}} )"
    }

    on <Until0Builder> {
        "until0 ( {${digest(it.repeat)}} , {${digest(it.terminator)}} )"
    }

    on <Until1Builder> {
        "until1 ( {${digest(it.repeat)}} , {${digest(it.terminator)}} )"
    }

    on <BuildBuilder> {
        val backlog = if (it.backlog == 0) "" else "${it.backlog},"
        "build($backlog\n" +
            "        syntax = { ${digest(it.child)} },\n" +
            "        effect = { TODO() })"
    }

    on <BuildBuilderCode> {
        val backlog = if (it.backlog == 0) "" else "${it.backlog},"
        "build($backlog\n" +
            "        syntax = { ${digest(it.child)} },\n" +
            "        effect = { ${it.effect} })"
    }

    on <AffectBuilder> {
        val backlog = if (it.backlog == 0) "" else "${it.backlog},"
        "affect($backlog\n" +
            "        syntax = { ${digest(it.child)} },\n" +
            "        effect = { TODO() })"
    }

    on <AffectBuilderCode> {
        val backlog = if (it.backlog == 0) "" else "${it.backlog},"
        "affect($backlog\n" +
            "        syntax = { ${digest(it.child)} },\n" +
            "        effect = { ${it.effect} })"
    }

    on <BuildStrBuilderCode> {
        "build_str(\n" +
            "        syntax = { ${digest(it.child)} },\n" +
            "        effect = { TODO() })"
    }

    on <BuildStrBuilderCode> {
        "build_str(\n" +
            "        syntax = { ${digest(it.child)} },\n" +
            "        effect = { ${it.effect} })"
    }
}

// -------------------------------------------------------------------------------------------------


val str_compiler = Poly1 <ParserBuilder, Pair<Boolean, String>> ().apply {

    on <TokenBuilder> {
        false to model_compiler(it)
    }

    on <TokenBuilderCode> {
        false to model_compiler(it)
    }

    on <StrTokenBuilder> {
        false to model_compiler(it)
    }

    on <KeywordBuilder> {
        false to model_compiler(it)
    }

    default = {
        true to model_compiler(it)
    }
}

// -------------------------------------------------------------------------------------------------

