package norswap.lang.java4
import norswap.autumn.Parser
import norswap.autumn.TokenGrammar
import norswap.autumn.parsers.*
import norswap.lang.java_base.*
import norswap.lang.java4.ast.*

class Java4Grammar: TokenGrammar()
{
    /// LEXICAL ====================================================================================

    // Whitespace

    fun lineComment()
        = seq { "//".str && until0 ({char_any()}, {"\n".str}) }

    fun multiComment()
        = seq { "/*".str && until0 ({char_any()}, {"*/".str}) }

    override fun whitespace()
        = repeat0 { choice { space_char() || lineComment() || multiComment() } }

    // Keywords and operators

    val `boolean` = "boolean".token
    val `byte` = "byte".token
    val `char` = "char".token
    val `double` = "double".token
    val `float` = "float".token
    val `int` = "int".token
    val `long` = "long".token
    val `short` = "short".token
    val `void` = "void".token

    val `abstract` = "abstract".token
    val `default` = "default".token
    val `final` = "final".token
    val `native` = "native".token
    val `private` = "private".token
    val `protected` = "protected".token
    val `public` = "public".token
    val `static` = "static".token
    val `strictfp` = "strictfp".token
    val `synchronized` = "synchronized".token
    val `transient` = "transient".token
    val `volatile` = "volatile".token

    val `false` = token ({ false }) { string("false") }
    val `true`  = token ({ true  }) { string("true")  }
    val `null`  = token ({ Null  }) { string("null")  }

    val `assert` = "assert".keyword
    val `break` = "break".keyword
    val `case` = "case".keyword
    val `catch` = "catch".keyword
    val `class` = "class".keyword
    val `const` = "const".keyword
    val `continue` = "continue".keyword
    val `do` = "do".keyword
    val `else` = "else".keyword
    val `extends` = "extends".keyword
    val `finally` = "finally".keyword
    val `for` = "for".keyword
    val `goto` = "goto".keyword
    val `if` = "if".keyword
    val `implements` = "implements".keyword
    val `import` = "import".keyword
    val `interface` = "interface".keyword
    val `instanceof` = "instanceof".keyword
    val `new` = "new".keyword
    val `package` = "package".keyword
    val `return` = "return".keyword
    val `super` = "super".keyword
    val `switch` = "switch".keyword
    val `this` = "this".keyword
    val `throws` = "throws".keyword
    val `throw` = "throw".keyword
    val `try` = "try".keyword
    val `while` = "while".keyword

    // Since Java 1.5
    // val `enum` = "enum".keyword

    val `!` = "!".keyword
    val `%` = "%".keyword
    val `%=` = "%=".keyword
    val `&` = "&".keyword
    val `&&` = "&&".keyword
    val `&=` = "&=".keyword
    val `(` = "(".keyword
    val `)` = ")".keyword
    val `*` = "*".keyword
    val `*=` = "*=".keyword
    val `+` = "+".keyword
    val `++` = "++".keyword
    val `+=` = "+=".keyword
    val `,` = ",".keyword
    val `-` = "-".keyword
    val `--` = "--".keyword
    val `-=` = "-=".keyword
    val `=` = "=".keyword
    val `==` = "==".keyword
    val `?` = "?".keyword
    val `^` = "^".keyword
    val `^=` = "^=".keyword
    val `{` = "{".keyword
    val `|` = "|".keyword
    val `|=` = "|=".keyword
    val `!=` = "!=".keyword
    val `||` = "||".keyword
    val `}` = "}".keyword
    val `~` = "~".keyword

    // Since Java 1.5
    // val `@` = "@".keyword

    val div = "/".keyword
    val dive = "/=".keyword
    val gt = ">".keyword
    val lt = "<".keyword
    val ge = ">=".keyword
    val le = "<=".keyword
    val sl = "<<".keyword
    val sle = "<<=".keyword
    val sr = +">>" // to avoid ambiguity with gt
    val sre = ">>=".keyword
    val bsr = +">>>" // to avoid ambiguity with gt
    val bsre = ">>>=".keyword
    val lsbra = "[".keyword
    val rsbra = "]".keyword
    val arrow = "->".keyword
    val colon = ":".keyword
    val semi = ";".keyword
    val dot = ".".keyword

    // Since Java 1.5
    // val ellipsis = "...".keyword

    // Since Java 1.8
    // val dcolon = "::".keyword


    // Identifiers (must come after keywords)

    val iden = token { java_iden() }

    // Numerals: bits and pieces

     fun `_`()
        = "_".str

     fun dlit()
        = ".".str

     fun hex_prefix()
        =  choice { "0x".str || "0x".str }

     fun digits1()
        = repeat1 { digit() }

     fun digits0()
        = repeat0 { digit() }

     fun hex_digits()
        = repeat1 { hex_digit() }

     fun hex_num()
        =  seq { hex_prefix() && hex_digits() }

    // Numerals: floating point

     fun expSignOpt()
        = opt { "+-".set }

     fun exponent()
        = seq { "eE".set && expSignOpt() && digits1() }

     fun exponent_opt()
        = opt { exponent() }

     fun float_suffix()
        = "fFdD".set

     fun float_suffix_opt()
        = opt { float_suffix() }

     fun dec_float_lit()
        = choice { seq { digits1() && dlit() && digits0() && exponent_opt() && float_suffix_opt() } ||
            seq { dlit() && digits1() && exponent_opt() && float_suffix_opt() } ||
            seq { digits1() && exponent() && float_suffix_opt() } ||
            seq { digits1() && exponent_opt() && float_suffix() }
    }

    val float_lit
        = token(this::parse_float) { dec_float_lit() }

    // Numerals: integral

     fun bit()
        = "01".set

     fun binary_prefix()
        = choice { "0b".str || "0B".str }

     fun octal_num()
        =  seq { "0".str && repeat1 { oct_digit() } }

     fun dec_num()
        = choice { "0".str || digits1() }

     fun integer_num()
        = choice { hex_num() || octal_num() || dec_num() }

    val integer_lit
        = token (this::parse_int) { seq { integer_num() && opt { char_set('l', 'L') } } }

    // Characters

     fun octal_escape()
        = choice {
        seq { char_range('0', '3') && oct_digit() && oct_digit() } ||
            seq { oct_digit() && opt { oct_digit() } }
    }

     fun unicode_escape()
        = seq { repeat1 { "u".str } && repeat(4) { hex_digit() } }

     fun escape()
        =  seq { "\\".str && choice { "btnfr\"'\\".set || octal_escape() || unicode_escape() } }

     fun naked_char()
        = choice { escape() || seq { not { "'\\\n\r".set } && char_any() } }

     fun char_syntax()
        = seq { "'".str && naked_char() && "'".str }

    val char_literal
        = token (::parse_char) { char_syntax() }

    // Strings

     fun naked_string_char()
        = choice { escape() || seq { not { "\"\\\n\r".set } && char_any() } }

     fun string_syntax()
        = ("\"".str && repeat0 { naked_string_char() } && "\"".str)

    val string_lit
        = token (::parse_string) { string_syntax() }

    // Literal

    val literal_syntax
        = token_choice(
            integer_lit,
            string_lit,
            `null`,
            float_lit,
            `true`,
            `false`,
            char_literal)

    // later versions add: underscore separators, hex floats, binary integers

    fun literal()
        = build ({literal_syntax()}, {Literal(it(0))})

    /// UTILS ======================================================================================

    inline fun angles  (crossinline p: Parser) = seq { lt()      && p() && gt() }
    inline fun squares (crossinline p: Parser) = seq { lsbra()   && p() && rsbra() }
    inline fun braces  (crossinline p: Parser) = seq { `{`()     && p() && `}`() }
    inline fun parens  (crossinline p: Parser) = seq { `(`()     && p() && `)`() }

    inline fun opt_term_list (crossinline item: Parser, crossinline sep: Parser)
        = seq { around0(item, sep) && opt { sep() } }

    /// TYPES ======================================================================================

    val basicType = token_choice(
        `byte`,
        `short`,
        `int`,
        `long`,
        `char`,
        `float`,
        `double`,
        `boolean`,
        `void`)

    fun primitive_type() = build(
        syntax = { basicType() },
        effect = { PrimitiveType(it(0)) })

    fun class_type() = build(
        syntax = { around1({ iden() }, { dot() }) },
        effect = { ClassType(it.list<String>()) })

    fun stem_type()
        = choice { primitive_type() || class_type() }

    fun dim() = build(
        syntax = { squares { true } },
        effect = { 1 })

    fun dims() = build(
        syntax = { repeat0 { dim() } },
        effect = { it.sumBy { it as Int } })

    fun dims1() = build(
        syntax = { repeat1 { dim() } },
        effect = { it.sumBy { it as Int } })

    fun type_dim_suffix() = build(1,
        syntax = { dims1() },
        effect = { ArrayType(it(0), it(1)) })

    fun type(): Boolean
        = seq { stem_type() && opt { type_dim_suffix() } }

    /// NON-TYPE DECLARATIONS ======================================================================

    fun var_init(): Boolean
        = choice { expr() || array_init() }

    fun array_init() = build(
        syntax = { braces { opt_term_list ({ var_init() } , { `,`() }) } },
        effect = { ArrayInit(it.list()) })

    /// PARAMETERS =================================================================================

    fun args() = build(
        syntax = { parens { around0 ({ expr() } , { `,`() }) } },
        effect = { it.list<Expr>() })

    /// EXPRESSIONS ================================================================================

    // Array Constructor ------------------------------------------------------

    fun dim_exprs() = build(
        syntax = { repeat1 { squares { expr() } } },
        effect = { it.list<Expr>() })

    fun dim_expr_array_creator() = build(
        syntax = { seq { stem_type() && dim_exprs() && dims() } },
        effect = { ArrayCtorCall(it(0), it(1), it(2), null) })

    fun init_array_creator() = build(
        syntax = { seq { stem_type() && dims1() && array_init() } },
        effect = { ArrayCtorCall(it(0), emptyList(), it(1), it(2)) })

    fun array_ctor_call()
        = seq { `new`() && choice { dim_expr_array_creator() || init_array_creator() } }

    // Expression: Primary ----------------------------------------------------

    fun par_expr() = build(
        syntax = { parens { expr() } },
        effect = { ParenExpr(it(0)) })

    fun ctor_call() = build(
        syntax = { seq { `new`() && stem_type() && args() && maybe { type_body() } } },
        effect = { CtorCall(it(0), it(1), it(2)) })

    fun class_expr() = build(
        syntax = { seq { type() && dot() && `class`() } },
        effect = { ClassExpr(it(0)) })

    fun iden_method_expr() = build(
        syntax = { seq { iden() && maybe { args() }}},
        effect = { it[1] ?. let { MethodCall(null, it(0), it(1)) } ?: Identifier(it(0)) })

    fun this_expr() = build(
        syntax = { seq { `this`() && maybe { args() } } },
        effect = { it[0] ?. let { ThisCall(it(0)) } ?: This })

    fun super_expr() = build(
        syntax = { seq { `super`() && maybe { args() } } },
        effect = { it[0] ?. let { SuperCall(it(0)) } ?: Super })

    fun primary_expr() = choice {
        par_expr() ||
        array_ctor_call() ||
        ctor_call() ||
        class_expr() ||
        iden_method_expr() ||
        this_expr() ||
        super_expr() ||
        literal()
    }

    // =============================================================================================

    fun identifier() = build(
        syntax = { iden() },
        effect = { Identifier(it(0)) })

    fun ternary() = choice { literal() || identifier() }

    fun expr(): Boolean = primary_expr()

    fun type_body() = false

    // =============================================================================================

    override fun root() = true
}
