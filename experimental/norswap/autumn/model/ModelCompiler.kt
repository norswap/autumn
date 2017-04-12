package norswap.autumn.model
import norswap.lang.java_base.escape
import norswap.lang.java8.Java8Model
import norswap.utils.*
import norswap.utils.poly.Poly1
import java.io.PrintWriter

// -------------------------------------------------------------------------------------------------

fun main (args: Array<String>)
{
    val str = compile_model("Grammar2", Java8Model())
    val writer = PrintWriter("src/norswap/lang/java8/Grammar2.kt")
    writer.println(str)
    writer.flush()
}

// -------------------------------------------------------------------------------------------------
// Global Context (global variables for now)

/**
 * Whether the parser being compiled is a top-level parser.
 */
var top_level = true

// -------------------------------------------------------------------------------------------------

/**
 * Parsers with these names are overrides from `Grammarè.
 */
val overrides = listOf("whitespace", "root")

// -------------------------------------------------------------------------------------------------

/**
 * List of Kotlin keywords, that have to be escaped with backquotes if used as parser names.
 */
val kotlin_keywords = listOf(
    "package", "as", "typealias", "class", "this", "super", "val", "var", "fun", "for", "null",
    "true", "false", "is", "in", "throw", "return", "break", "continue", "object", "if", "try",
    "else", "while", "do", "when", "interface", "typeof", "_")

// -------------------------------------------------------------------------------------------------

/**
 * List of parser builders that result in a value definition instead of a function definition
 * (said otherwise, their expressions return `Parser` rather than `Boolean`).
 */
val val_parsers = listOf<Class<out ParserBuilder>>(
    TokenBuilder::class.java,
    PlainTokenBuilder   ::class.java,
    StrTokenBuilder     ::class.java,
    KeywordBuilder      ::class.java,
    AssocLeftBuilder::class.java,
    TokenChoiceBuilder  ::class.java)

// -------------------------------------------------------------------------------------------------

/**
 * List of parser builders where the `=` sign has to be on the same line as the parser name,
 * when used as top-level parser declaration.
 */
val equal_same_line = listOf<Class<out ParserBuilder>>(
    BuildBuilder::class.java,
    AffectBuilder::class.java,
    AssocLeftBuilder    ::class.java)

// -------------------------------------------------------------------------------------------------

/**
 * Converts a Kotlin getter name to the corresponding field name.
 * Assumes that all fields start with a lowercase letter.
 * Also escapes (using backquotes) identifiers from [kotlin_keywords] and field names starting
 * with illegal start of identifier characters.
 */
fun String.kotlin_getter_to_val_name(): String
{
    var name = removePrefix("get").decapitalize()

    if (!name[0].isJavaIdentifierStart() || kotlin_keywords.contains(name))
        name = "`$name`"

    return name
}

// -------------------------------------------------------------------------------------------------

/**
 * Returns a list of builders defined in [model],
 * setting their names, and sorted by [Builder.order].
 */
fun builders (model: Any): List<Builder>
{
    return model::class.java.methods
        .filter { supers <Builder> (it.returnType) }
        .map {
            val builder = it.invoke(model) as Builder
            builder.name = it.name.kotlin_getter_to_val_name()
            builder
        }
        .sortedBy { it.order }
}

// -------------------------------------------------------------------------------------------------

/**
 * Compile a grammar model to an Autumn grammar source string.
 */
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

    builders(model).forEach {
        b += "\n\n"
        b += compile_top_level(it)
    }

    b += "\n}"
    return b.toString()
}

// -------------------------------------------------------------------------------------------------

/**
 * Compile a top-level model builder to its string representation.
 */
val compile_top_level = Poly1<Builder, String>().apply {

    default { "" }

    on <SectionBuilder> {
        when (it.level) {
            1 ->  "    /// ${it.name!!.capitalize()} " + "=".repeat(91 - it.name!!.length)
            2 -> "    //// ${it.name!!.capitalize()} " + "-".repeat(71 - it.name!!.length)
            else -> ""
    }   }

    on <SeparatorBuilder> {
        when (it.level) {
            1 ->  "    /// " + "=".repeat(92)
            2 -> "    //// " + "-".repeat(72)
            else -> ""
    }   }

    on <CodeBuilder> {
        it.code.prependIndent("    ")
    }

    on <ParserBuilder> {
        top_level = true
        val func = !val_parsers.contains(it::class.java)
        val str = model_compiler(it)
        val b = StringBuilder()
        b += "    "

        if (overrides.contains(it.name))
            b += "override "

        if (func)   b += "fun ${it.name}()"
        else        b += "val ${it.name}"

        if (it.attributes.contains(TypeHint))
            if (func)   b += " : Boolean"
            else        b += " : Parser"

        if (equal_same_line.contains(it::class.java))
            b += " = $str"
        else
            b += "\n        = $str"

        b.toString()
    }
}

// -------------------------------------------------------------------------------------------------

fun Poly1<ParserBuilder, String>.digest(p: ParserBuilder): String
{
    top_level = false
    return p.name ?. let { "$it()" } ?: invoke(p)
}

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
            .replace("\n", "\n             ")
        "choice { $children }"
    }

    on <LongestBuilder> {
        val children = it.list
            // use short form when possible (`name`, not `{ name() }`)
            .map { it.name ?: "{ ${digest(it)} }" }
            .joinToString(separator = " , ")
        "longest ( $children )"
    }

    on <TokenChoiceBuilder> {
        val children = it.list
            .map { it.name }
            .joinToString()
        "token_choice($children)"
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
    on <ParensBuilder>  ("parens")
    on <PlainTokenBuilder> ("token")

    on <EmptyAnglesBuilder>  { "angles()"  }
    on <EmptyCurliesBuilder> { "curlies()" }
    on <EmptySquaresBuilder> { "squares()" }
    on <EmptyParensBuilder>  { "parens()"  }

    on <CommaList0Builder>      ("comma_list0")
    on <CommaList1Builder>      ("comma_list1")
    on <CommaListTerm0Builder>  ("comma_list_term0")
    on <CommaListTerm1Builder>  ("comma_list_term1")

    // TODO only works for value that print to their code representation
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
        val backlog = if (it.backlog == 0) "" else "${it.backlog}, "
        if (top_level)
            "build($backlog\n" +
            "        syntax = { ${digest(it.child)} },\n" +
            "        effect = { ${it.effect.replace("\n", "\n" + " ".repeat(19))} })"
        else
            "\nbuild($backlog{ ${digest(it.child)} }, { ${it.effect} })"
    }

    on <AffectBuilder> {
        val backlog = if (it.backlog == 0) "" else "${it.backlog},"
        "affect($backlog\n" +
            "        syntax = { ${digest(it.child)} },\n" +
            "        effect = { ${it.effect} })"
    }

    on <BuildStrBuilder> {
        "build_str(\n" +
            "        syntax = { ${digest(it.child)} },\n" +
            "        effect = { TODO() })"
    }

    on <BuildStrBuilder> {
        "build_str(\n" +
            "        syntax = { ${digest(it.child)} },\n" +
            "        effect = { ${it.effect} })"
    }

    on <AssocLeftBuilder> {
        val b = StringBuilder()

        if (it is AssocLeftBuilder)
            b += "assoc_left {\n"

        if (it.operands != null && (it.left != null || it.right != null))
            throw Error("Cannot set both operands and left/right.")

        if (it.strict != null)
            b += "        strict = ${it.strict!!}\n"
        if (it.operands != null)
            b += "        operands = { ${digest(it.operands!!)} }\n"
        if (it.left != null)
            b += "        left = { ${digest(it.left!!)} }\n"
        if (it.right != null)
            b += "        right = { ${digest(it.right!!)} }\n"

        it.operators.forEach {
            b += "        ${it.kind}({ ${digest(it.parser)} }, { ${it.effect} })\n"
        }

        b += "    }"
        b.toString()
    }
}

// -------------------------------------------------------------------------------------------------

