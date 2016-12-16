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
    var name = removePrefix("get")

    if (!name[0].isJavaIdentifierStart() || kotlin_keywords.contains(name))
        name = "`$name`"

    return name
}

// -------------------------------------------------------------------------------------------------

fun compile_model (klass_name: String, model: Any): String
{
    order_next = 0

    val b = StringBuilder()

    b += "package norswap.lang.java8"
    b += "import norswap.autumn.Parser"
    b += "import norswap.autumn.TokenGrammar"
    b += "import norswap.autumn.parsers.*"
    b += "import norswap.lang.java_base.*"
    b += "import norswap.lang.java8.ast.*"
    b += "import norswap.lang.java8.ast.TypeDeclKind.*"
    b += "class $klass_name: Grammar\n{"

    model.javaClass.methods

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
                    b += it.name
                    b += " "
                    b += "=".repeat(91 - it.name!!.length)
                }

                is CodeBuilder -> {
                    b += it.code.prependIndent("    ")
                }

                is ParserBuilder ->
                {
                    val str = model_compiler(it)
                    b += "    "
                    if (overrides.contains(it.name)) b += "override "
                    b += "fun "
                    b += it.name
                    if (equal_same_line.contains(it.javaClass))
                        b += "() = $str"
                    else
                        b += "()\n        = $str"
                }
            }
        }

    b += "\n}"
    return b.toString()
}

// -------------------------------------------------------------------------------------------------

fun Polymorphic<ParserBuilder, String>.digest(p: ParserBuilder): String
    = p.name ?. let { "$it()" } ?: invoke(p)

// -------------------------------------------------------------------------------------------------

inline fun <reified T: WrapperBuilder> Polymorphic<ParserBuilder, String>
    .on(prefix: String)
{
    on <T> {
        "$prefix { ${digest(it.child)} }"
    }
}

// -------------------------------------------------------------------------------------------------

val model_compiler = Polymorphic <ParserBuilder, String>
{
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

    on <AheadBuilder>   ("ahead")
    on <NotBuilder>     ("not")
    on <OptBuilder>     ("opt")
    on <MaybeBuilder>   ("maybe")
    on <AsBoolBuilder>  ("as_bool")
    on <Repeat0Builder> ("repeat0")
    on <Repeat1Builder> ("repeat1")

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