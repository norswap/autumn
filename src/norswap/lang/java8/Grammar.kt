package norswap.lang.java8
import norswap.autumn.TokenGrammar
import norswap.autumn.parsers.*
import norswap.lang.java_base.*
import norswap.lang.java8.ast.*
import norswap.lang.java8.ast.TypeDeclKind.*

class Java8Grammar : TokenGrammar()
{
    /// LEXICAL ====================================================================================

    // Whitespace

    fun line_comment()
        = seq { "//".str && until0 ({char_any()}, {"\n".str}) }

    fun multi_comment()
        = seq { "/*".str && until0 ({char_any()}, {"*/".str}) }

    override fun whitespace()
        = repeat0 { choice { space_char() || line_comment() || multi_comment() } }

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

    val `false` = token ({ false }) { "false".str }
    val `true`  = token ({ true  }) { "true".str  }
    val `null`  = token ({ Null  }) { "null".str  }

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
    val `enum` = "enum".keyword

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
    val `@` = "@".keyword

    val div = "/".keyword
    val dive = "/=".keyword
    val gt = ">".keyword
    val lt = "<".keyword
    val ge = ">=".keyword
    val le = "<=".keyword
    val sl = "<<".keyword
    val sle = "<<=".keyword
    // todo this really a thing?
    fun sr() = +">>" // to avoid ambiguity with gt
    val sre = ">>=".keyword
    fun bsr() = +">>>" // to avoid ambiguity with gt
    val bsre = ">>>=".keyword
    val lsbra = "[".keyword
    val rsbra = "]".keyword
    val arrow = "->".keyword
    val colon = ":".keyword
    val semi = ";".keyword
    val dot = ".".keyword
    val ellipsis = "...".keyword
    val dcolon = "::".keyword

    // Identifiers (must come after keywords)

    val iden = token { java_iden() }

    // Numerals: bits and pieces

    fun `_`()
        = "_".str

    fun dlit()
        = ".".str

    fun hex_prefix()
        =  choice { "0x".str || "0x".str }

    fun underscores()
        = repeat0 { `_`() }

    fun digits1()
        = around1 ({digit()} , {underscores()})

    fun digits0()
        = around0 ({digit()} , {underscores()})

    fun hex_digits()
        = around1 ({hex_digit()} , {underscores()})

    fun hex_num()
        =  seq { hex_prefix() && hex_digits() }

    // Numerals: floating point

    fun hex_significand() = choice {
        seq { hex_prefix() && opt { hex_digits() } && dlit() && hex_digits() } ||
        seq { hex_num() && opt { dlit() } }
    }

    fun exp_sign_opt()
        = opt { "+-".set }

    fun exponent()
        = seq { "eE".set && exp_sign_opt() && digits1() }

    fun binary_exponent()
        =  seq { "pP".set && exp_sign_opt() && digits1() }

    fun exponent_opt()
        = opt { exponent() }

    fun float_suffix()
        = "fFdD".set

    fun float_suffix_opt()
        = opt { float_suffix() }

    fun hex_float_lit()
        =  seq { hex_significand() && binary_exponent() && float_suffix_opt() }

    fun decimal_float_lit()
        = choice {
            seq { digits1() && dlit() && digits0() && exponent_opt() && float_suffix_opt() } ||
            seq { dlit() && digits1() && exponent_opt() && float_suffix_opt() } ||
            seq { digits1() && exponent() && float_suffix_opt() } ||
            seq { digits1() && exponent_opt() && float_suffix() }
        }

    val float_literal
        = token(this::parse_float) { choice { hex_float_lit() || decimal_float_lit() } }

    // Numerals: integral

    fun bit()
        = "01".set

    fun binary_prefix()
        = choice { "0b".str || "0B".str }

    fun binary_num()
        =  seq { binary_prefix() && around1 ({bit()} , {underscores()}) }

    fun octal_num()
        =  seq { "0".str && repeat1 { underscores() && octal_digit() } }

    fun dec_num()
        = choice { "0".str || digits1() }

    fun integer_num()
        = choice { hex_num() || binary_num() || octal_num() || dec_num() }

    val integer_literal
        = token (this::parse_int) { seq { integer_num() && opt { "lL".set } }}

    // Characters

    fun octal_escape()
        = choice {
            seq { char_range('0', '3') && octal_digit() && octal_digit() } ||
            seq { octal_digit() && opt { octal_digit() } }
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

    val string_literal
        = token (::parse_string) { string_syntax() }

    // Literal

    val literal_syntax = token_choice(
        integer_literal,
        string_literal,
        `null`,
        float_literal,
        `true`,
        `false`,
        char_literal)

    fun literal()
        = build ({literal_syntax()}, { Literal(it(0)) })

    /// ANNOTATIONS ================================================================================

    fun annotation_element(): Boolean
        = choice { ternary() || annotation_element_list() || annotation() }

    fun annotation_inner_list()
        = comma_list_term0 { annotation_element() }

    fun annotation_element_list() = build (
        syntax = { curlies { annotation_inner_list() } },
        effect = { AnnotationElementList(it.list()) })

    fun annotation_element_pair() = build(
        syntax = { seq { iden() && `=`() && annotation_element() } },
        effect = { Pair<String, AnnotationElement>(it(0), it(1)) })

    fun normal_annotation_suffix() = build(1,
        syntax = { parens { comma_list1 { annotation_element_pair() } } },
        effect = { NormalAnnotation(it(0), it.list<Pair<String, AnnotationElement>>(1)) })

    fun single_element_annotation_suffix() = build(1,
        syntax = { parens { annotation_element() } },
        effect = { SingleElementAnnotation(it(0), it(1)) })

    fun marker_annotation_suffix() = build(1,
        syntax = { opt { parens() } },
        effect = { MarkerAnnotation(it(0)) })

    fun annotation_suffix() = choice {
        normal_annotation_suffix() ||
        single_element_annotation_suffix() ||
        marker_annotation_suffix()
    }

    fun qualified_iden() = build(
        syntax = { around1 ({iden()} , {dot()}) },
        effect = { it.list<String>() })

    fun annotation()
        = seq { `@`() && qualified_iden() && annotation_suffix() }

    fun annotations() = build(
        syntax = { repeat0 { annotation() } },
        effect = { it.list<Annotation>() })

    /// TYPES ======================================================================================

    val basic_type = token_choice(
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
        syntax = { seq { annotations() && basic_type() } },
        effect = { PrimitiveType(it(0), it(1)) })

    fun extends_bound() = build(
        syntax = { seq { `extends`() && type() } },
        effect = { ExtendsBound(it(0)) })

    fun super_bound() = build(
        syntax = { seq { `super`() && type() } },
        effect = { SuperBound(it(0)) })

    fun type_bound() =
        maybe { choice { extends_bound() || super_bound() } }

    fun wildcard() = build(
        syntax = { seq { annotations() && `?`() && type_bound() } },
        effect = { Wildcard(it(0), it(1)) })

    fun type_args() = build(
        syntax = { opt { angles { comma_list0 {choice { type() || wildcard() } } } } },
        effect = { it.list<Type>() })

    fun class_type_part() = build(
        syntax = { seq { annotations() && iden() && type_args() } },
        effect = { ClassTypePart(it(0), it(1), it(2)) })

    fun class_type() = build(
        syntax = { around1({ class_type_part() }, { dot() }) },
        effect = { ClassType(it.list<ClassTypePart>()) })

    fun stem_type()
        = choice { primitive_type() || class_type() }

    fun dim() = build(
        syntax = { seq { annotations() && squares() } },
        effect = { Dimension(it(0)) })

    fun dims() = build(
        syntax = { repeat0 { dim() } },
        effect = { it.list<Dimension>() })

    fun dims1() = build(
        syntax = { repeat1 { dim() } },
        effect = { it.list<Dimension>() })

    fun type_dim_suffix() = build(1,
        syntax = { dims1() },
        effect = { ArrayType(it(0), it(1)) })

    fun type(): Boolean
        = seq { stem_type() && opt { type_dim_suffix() } }

    fun type_union_syntax()
        = around1 ({type()} ,  {`&`()})

    fun type_union() = build(
        syntax = { type_union_syntax() },
        effect = { it.list<Type>() })

    fun type_bounds() = build(
        syntax = { opt { seq { `extends`() && type_union_syntax() } } },
        effect = { it.list<Type>() })

    fun type_param() = build(
        syntax = { seq { annotations() && iden() && type_bounds() } },
        effect = { TypeParam(it(0), it(1), it(2)) })

    fun type_params() = build(
        syntax = { opt { angles { comma_list0 { type_param() } } } },
        effect = { it.list<TypeParam>() })

    /// MODIFIERS ==================================================================================

    fun keyword_modifier() = build(
        syntax = { choice {
               public()     || protected()      || private()    || abstract()   || static()
            || final()      || synchronized()   || native()     || strictfp()   || default()
            || transient()  || volatile() } },
        effect = { Keyword.valueOf(it(0)) })

    fun modifier()
        = choice { annotation() || keyword_modifier() }

    fun modifiers() = build(
        syntax = { repeat0 { modifier() } },
        effect = { it.list<Modifier>() })

    /// PARAMETERS =================================================================================

    fun args() = build(
        syntax = { parens { comma_list0 { expr() } } },
        effect = { it.list<Expr>() })

    fun this_parameter_qualifier() = build(
        syntax = { repeat0 { seq { iden() && dot() } } },
        effect = { it.list<String>() })

    fun this_param_suffix() = build(2,
        syntax = { seq { this_parameter_qualifier() && `this`() } },
        effect = { ThisParameter(it(0), it(1), it(2)) })

    fun iden_param_suffix() = build(2,
        syntax = { seq { iden() && dims() } },
        effect = { IdenParameter(it(0), it(1), it(2), it(3)) })

    fun variadic_param_suffix() = build(2,
        syntax = { seq { annotations() && ellipsis() && iden() } },
        effect = { VariadicParameter(it(0), it(1), it(2), it(3)) })

    fun formal_param_suffix()
        = choice { iden_param_suffix() || this_param_suffix() || variadic_param_suffix() }

    fun formal_param()
        = seq { modifiers() && type() && formal_param_suffix() }

    fun formal_params() = build(
        syntax = { parens { comma_list0 { formal_param() } } },
        effect = { FormalParameters(it.list()) })

    fun untyped_params() = build(
        syntax = { parens { comma_list1 { iden() } } },
        effect = { UntypedParameters(it.list()) })

    fun single_param() = build(
        syntax = { iden() },
        effect = { UntypedParameters(it.list<String>()) })

    fun lambda_params()
        = choice { formal_params() || untyped_params() || single_param() }

    /// NON-TYPE DECLARATIONS ======================================================================

    fun var_init(): Boolean
        = choice { expr() || array_init() }

    fun array_init() = build(
        syntax = { curlies { comma_list_term0 { var_init() } } },
        effect = { ArrayInit(it.list()) })

    fun var_declarator_id() = build(
        syntax = { seq { iden() && dims() } },
        effect = { VarDeclaratorID(it(0), it(1)) })

    fun var_declarator() = build(
        syntax = { seq { var_declarator_id() && maybe { seq { `=`() && var_init() } } } },
        effect = { VarDeclarator(it(0), it(1)) })

    fun var_decl_no_semi() = build(1,
        syntax = { seq { type() && comma_list1 { var_declarator() } } },
        effect = { VarDecl(it(0), it(1), it.list(2)) })

    fun var_decl_suffix()
        = seq { var_decl_no_semi() && semi() }

    fun var_decl()
        = seq { modifiers() && var_decl_suffix() }

    fun throws_clause() = build(
        syntax = { opt { seq { throws() && comma_list1 { type() } } } },
        effect = { it.list<Type>() })

    fun block_or_semi()
        = choice { block() || as_val(null) { semi() } }

    fun method_decl_suffix() = build(1,
        syntax = { seq {
            type_params() && type() && iden() && formal_params() && dims()
            && throws_clause() && block_or_semi() } },
        effect = { MethodDecl(it(0), it(1), it(2), it(3), it(4), it(5), it(6), it(7)) })

    fun constructor_decl_suffix() = build(1,
        syntax = { seq { type_params() && iden() && formal_params() && throws_clause() && block() } },
        effect = { ConstructorDecl(it(0), it(1), it(2), it(3), it(4), it(5)) })

    fun init_block() = build(
        syntax = { seq { as_bool { static() } && block() } },
        effect = { InitBlock(it(0), it(1)) })

    /// TYPE DECLARATIONS ==========================================================================

    // Common -----------------------------------------------------------------

    fun extends_clause() = build(
        syntax = { opt { seq { extends() && comma_list0 { type() } } } },
        effect = { it.list<Type>() })

    fun implements_clause() = build(
        syntax = { opt { seq { implements() && comma_list0 { type() } } } },
        effect = { it.list<Type>() })

    fun type_sig()
        = seq { iden() && type_params() && extends_clause() && implements_clause() }

    fun class_modified_decl()
        = seq { modifiers() && choice { var_decl_suffix() || method_decl_suffix() || constructor_decl_suffix() || type_decl_suffix() } }

    fun class_body_decl():  Boolean
        = choice { class_modified_decl() || init_block() || semi() }

    fun class_body_decls() = build(
        syntax = { repeat0 { class_body_decl() } },
        effect = { it.list<Decl>() })

    fun type_body()
        = curlies { class_body_decls() }

    // Enum -------------------------------------------------------------------

    fun enum_constant() = build(
        syntax = { seq { annotations() && iden() && maybe { args() } && maybe { type_body() } } },
        effect = { EnumConstant(it(0), it(1), it(2), it(3)) })

    fun enum_class_decls() = build(
        syntax = { opt { seq { semi() && repeat0 { class_body_decl() } } } },
        effect = { it.list<Decl>() })

    fun enum_constants() = build(
        syntax = { opt { comma_list_term1 { enum_constant() } } },
        effect = { it.list<EnumConstant>() })

    fun enum_body() = affect(
        syntax = { curlies { seq { enum_constants() && enum_class_decls() } } },
        effect = { stack.push(it(1)) ; stack.push(it(0)) /* swap */ })

    fun enum_decl() = build(1,
        syntax = { seq { enum() && type_sig() && enum_body() } },
        effect = {
            val td = TypeDecl(ENUM, it(0), it(1), it(2), it(3), it(4), it(5))
            EnumDecl(td, it(6))
        })

    // Annotations ------------------------------------------------------------

    fun annot_default_clause() = build(
        syntax = { seq { default() && annotation_element() } },
        effect = { it(1) })

    fun annot_elem_decl() = build(
        syntax = { seq {
            modifiers() && type() && iden() && parens() && dims()
            && maybe { annot_default_clause() } && semi() } },
        effect = { AnnotationElemDecl(it(0), it(1), it(2), it(3), it(4)) })

    fun annot_body_decls() = build(
        syntax = { repeat0 { choice { annot_elem_decl() || class_body_decl() } } },
        effect = { it.list<Decl>() })

    fun annotation_decl() = build(1,
        syntax = { seq { `@`() && `interface`() && type_sig() && curlies { annot_body_decls() } } },
        effect = { TypeDecl(ANNOTATION, it(0), it(1), it(2), it(3), it(4), it(5)) })

    // ------------------------------------------------------------------------

    fun class_decl() = build(1,
        syntax = { seq { `class`() && type_sig() && type_body() } },
        effect = { TypeDecl(CLASS, it(0), it(1), it(2), it(3), it(4), it(5)) })

    fun interface_decl() = build(1,
        syntax = { seq { `interface`() && type_sig() && type_body() } },
        effect = { TypeDecl(INTERFACE, it(0), it(1), it(2), it(3), it(4), it(5)) })

    fun type_decl_suffix()
        = choice { class_decl() || interface_decl() || enum_decl() || annotation_decl() }

    fun type_decl()
        = seq { modifiers() && type_decl_suffix() }

    fun type_decls() = build(
        syntax = { repeat0 { choice { type_decl() || semi() } } },
        effect = { it.list<Decl>() })

    /// EXPRESSIONS ================================================================================

    // Expression: Array Constructor ------------------------------------------

    fun dim_expr() = build(
        syntax = { seq { annotations() && squares { expr() } } },
        effect = { DimExpr(it(0), it(1)) })

    fun dim_exprs() = build(
        syntax = { repeat1 { dim_expr() } },
        effect = { it.list<DimExpr>() })

    fun dim_expr_array_creator() = build(
        syntax = { seq { stem_type() && dim_exprs() && dims() } },
        effect = { ArrayCtorCall(it(0), it(1), it(2), null) })

    fun init_array_creator() = build(
        syntax = { seq { stem_type() && dims1() && array_init() } },
        effect = { ArrayCtorCall(it(0), emptyList(), it(1), it(2)) })

    fun array_ctor_call()
        = seq { `new`() && choice { dim_expr_array_creator() || init_array_creator() } }

    // Expression: Lambda -----------------------------------------------------

    fun lambda() = build(
        syntax = { seq { lambda_params() && arrow() && choice { block() || expr() } } },
        effect = { Lambda(it(0), it(1)) })

    // Expression: Primary ----------------------------------------------------

    fun par_expr() = build(
        syntax = { parens { expr() } },
        effect = { ParenExpr(it(0)) })

    fun ctor_call() = build(
        syntax = { seq { `new`() && type_args() && stem_type()
                        && args() && maybe { type_body() } } },
        effect = { CtorCall(it(0), it(1), it(2), it(3)) })

    fun new_ref_suffix() = build(2,
        syntax = { `new`() },
        effect = { NewReference(it(0), it(1)) })

    fun method_ref_suffix() = build(2,
        syntax = { iden() },
        effect = { MaybeBoundMethodReference(it(0), it(1), it(2)) })

    fun ref_suffix()
        = seq { dcolon() && type_args() && choice { new_ref_suffix() || method_ref_suffix() } }

    fun class_expr_suffix() = build(1,
        syntax = { seq { dot() && `class`() } },
        effect = { ClassExpr(it(0)) })

    fun type_suffix_expr()
        = seq { type() && choice { ref_suffix() || class_expr_suffix() } }

    fun iden_method_expr() = build(
        syntax = { seq { iden() && maybe { args() }}},
        effect = { it[1] ?. let { MethodCall(null, listOf(), it(0), it(1)) } ?: Identifier(it(0)) })

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
        type_suffix_expr() ||
        iden_method_expr() ||
        this_expr() ||
        super_expr() ||
        literal()
    }

    // Expression: Postfix ----------------------------------------------------

    fun dot_this() = build(1,
        syntax = { `this`() },
        effect = { DotThis(it(0)) })

    fun dot_super() = build(1,
        syntax = { `super`() },
        effect = { DotSuper(it(0)) })

    fun dot_iden() = build(1,
        syntax = { iden() },
        effect = { DotIden(it(0), it(1)) })

    fun dot_new() = build(1,
        syntax = { ctor_call() },
        effect = { DotNew(it(0), it(1)) })

    fun dot_method() = build(1,
        syntax = { seq { type_args() && iden() && args() } },
        effect = { MethodCall(it(0), it(1), it(2), it(3)) })

    fun dot_postfix()
        = choice { dot_method() || dot_iden() || dot_this() || dot_super() || dot_new() }

    fun ref_postfix() = build(1,
        syntax = { seq { dcolon() && type_args() && iden() } },
        effect = { BoundMethodReference(it(0), it(1), it(2)) })

    fun array_postfix() = build(1,
        syntax = { squares { expr() } },
        effect = { ArrayAccess(it(0), it(1)) })

    fun inc_suffix() = build(1,
        syntax = { `++`() },
        effect = { PostIncrement(it(0)) })

    fun dec_suffix() = build(1,
        syntax = { `--`() },
        effect = { PostDecrement(it(0)) })

    fun postfix() = choice {
            seq { dot() && dot_postfix() } ||
            array_postfix() ||
            inc_suffix() ||
            dec_suffix() ||
            ref_postfix()
    }

    fun postfix_expr()
        = seq { primary_expr() && repeat0 { postfix() } }

    // Expression: Prefix -----------------------------------------------------

    fun inc_prefix() = build(
        syntax = { seq { `++`() && prefix_expr() } },
        effect = { PreIncrement(it(0)) })

    fun dec_prefix() = build(
        syntax = { seq { `--`() && prefix_expr() } },
        effect = { PostIncrement(it(0)) })

    fun unary_plus() = build(
        syntax = { seq { `+`() && prefix_expr() } },
        effect = { UnaryPlus(it(0)) })

    fun unary_minus() = build(
        syntax = { seq { `-`() && prefix_expr() } },
        effect = { UnaryMinus(it(0)) })

    fun complement() = build(
        syntax = { seq { `~`() && prefix_expr() } },
        effect = { Complement(it(0)) })

    fun not() = build(
        syntax = { seq { `!`() && prefix_expr() } },
        effect = { Not(it(0)) })

    fun cast() = build(
        syntax = { seq { parens { type_union() } && choice { lambda() || prefix_expr() } } },
        effect = { Cast(it(0), it(1)) })

    fun prefix_expr(): Boolean = choice {
        inc_prefix() ||
        dec_prefix() ||
        unary_plus() ||
        unary_minus() ||
        complement() ||
        not() ||
        cast() ||
        postfix_expr()
    }

    // Expression: Binary -----------------------------------------------------

    val mult_expr = assoc_left {
        operands = { prefix_expr() }
        op(2, { `*`() },  { Product(it(0), it(1)) })
        op(2, { div() },  { Division(it(0), it(1)) })
        op(2, { `%`() },  { Remainder(it(0), it(1)) })
    }

    val add_expr = assoc_left {
        operands = mult_expr
        op(2, { `+`() },  { Sum(it(0), it(1)) })
        op(2, { `-`() },  { Diff(it(0), it(1)) })
    }

    val shift_expr = assoc_left {
        operands = add_expr
        op(2, { sl() },  { ShiftLeft(it(0), it(1)) })
        op(2, { sr() },  { ShiftRight(it(0), it(1)) })
        op(2, { bsr() }, { BinaryShiftRight(it(0), it(1)) })
    }

    val order_expr = assoc_left {
        operands = shift_expr
        op(2, { lt() }, { Lower(it(0), it(1)) })
        op(2, { le() }, { LowerEqual(it(0), it(1)) })
        op(2, { gt() }, { Greater(it(0), it(1)) })
        op(2, { ge() }, { GreaterEqual(it(0), it(1)) })
        postfix(2, { seq { instanceof() && type() } }, { Instanceof(it(0), it(1)) })
    }

    val eq_expr = assoc_left {
        operands = order_expr
        op(2, { `==`() }, { Equal(it(0), it(1)) })
        op(2, { `!=`() }, { NotEqual(it(0), it(1)) })
    }

    val binary_and_expr = assoc_left {
        operands = eq_expr
        op(2, { `&`() }, { BinaryAnd(it(0), it(1)) }) }

    val xor_expr = assoc_left {
        operands = binary_and_expr
        op(2, { `^`() }, { Xor(it(0), it(1)) }) }

    val binary_or_expr = assoc_left {
        operands = xor_expr
        op(2, { `|`() }, { BinaryOr(it(0), it(1)) }) }

    val and_expr = assoc_left {
        operands = binary_or_expr
        op(2, { `&&`() }, { And(it(0), it(1)) }) }

    val or_expr = assoc_left {
        operands = and_expr
        op(2, { `||`() }, { Or(it(0), it(1)) }) }

    val ternary = assoc_right {
        left  = or_expr
        // todo this alright?
        right = { choice { or_expr() || lambda() } }
        op(3,
            syntax = { `?`() && expr() && colon() },
            effect = { Ternary(it(0), it(1), it(2)) })
    }

    val assignment = assoc_right {
        left = ternary
        right = { choice { lambda() || ternary()  } }

        op (2, { `=`() },   { Assign(it(0), it(1), "=") })
        op (2, { `+=`() },  { Assign(it(0), it(1), "+=") })
        op (2, { `-=`() },  { Assign(it(0), it(1), "-=") })
        op (2, { `*=`() },  { Assign(it(0), it(1), "*=") })
        op (2, { dive() },  { Assign(it(0), it(1), "/=") })
        op (2, { `%=`() },  { Assign(it(0), it(1), "%=") })
        op (2, { sle() },   { Assign(it(0), it(1), "<<=") })
        op (2, { sre() },   { Assign(it(0), it(1), ">>=") })
        op (2, { bsre() },  { Assign(it(0), it(1), ">>>=") })
        op (2, { `&=`() },  { Assign(it(0), it(1), "&=") })
        op (2, { `^=`() },  { Assign(it(0), it(1), "^=") })
        op (2, { `|=`() },  { Assign(it(0), it(1), "|=") })
    }

    fun expr(): Boolean
        = choice { lambda() || assignment() }

    /// STATEMENTS =================================================================================

    fun if_stmt() = build(
        syntax = { seq { `if`() && par_expr() && stmt() && maybe { seq { `else`() && stmt() } } } },
        effect = { If(it(0), it(1), it(2)) })

    fun expr_stmt_list() = build(
        syntax = { comma_list0 { expr() } },
        effect = { it.list<Stmt>() })

    fun for_init_decl() = build(
        syntax = { seq { modifiers() && var_decl_no_semi() } },
        effect = { it.list<Stmt>() })

    fun for_init()
        = choice { for_init_decl() || expr_stmt_list() }

    fun basic_for_paren_part()
        = seq { for_init() && semi() && maybe { expr() } && semi() && opt { expr_stmt_list() } }

    fun basic_for_stmt() = build(
        syntax = { seq { `for`() && parens { basic_for_paren_part() } && stmt() } },
        effect = { BasicFor(it(0), it(1), it(2), it(3)) })

    fun for_var_decl()
        = seq { modifiers() && type() && var_declarator_id() && colon() && expr() }

    fun enhanced_for_stmt() = build(
        syntax = { seq { `for`() && parens { for_var_decl() } && stmt() } },
        effect = { EnhancedFor(it(0), it(1), it(2), it(3), it(4)) })

    fun while_stmt() = build(
        syntax = { seq { `while`() && par_expr() && stmt() } },
        effect = { WhileStmt(it(0), it(1)) })

    fun do_while_stmt() = build(
        syntax = { seq { `do`() && stmt() && `while`() && par_expr() && semi() } },
        effect = { DoWhileStmt(it(0), it(1)) })

    fun catch_parameter_types() = build(
        syntax = { around0 ( {type()} , {`|`()} ) },
        effect = { it.list<Type>() })

    fun catch_parameter()
        = seq { modifiers() && catch_parameter_types() && var_declarator_id() }

    fun catch_clause() = build(
        syntax = { seq { catch() && parens { catch_parameter() } && block() } },
        effect = { CatchClause(it(0), it(1), it(2), it(3)) })

    fun catch_clauses() = build(
        syntax = { repeat0 { catch_clause() } },
        effect = { it.list<TryResource>() })

    fun finally_clause()
        = seq { finally() && block() }

    fun resource() = build(
        syntax = { seq { modifiers() && type() && var_declarator_id() && `=`() && expr() } },
        effect = { TryResource(it(0), it(1), it(2), it(3)) })

    fun resources() = build(
        syntax = { opt { seq { parens { around1 ( {resource()} , {semi()} ) } } } },
        effect = { it.list<TryResource>() })

    fun try_stmt() = build(
        syntax = { seq {
            `try`() && resources() && block() && catch_clauses() && maybe { finally_clause() } } },
        effect = { TryStmt(it(0), it(1), it(2), it(3)) })

    fun default_label() = build(
        syntax = { seq { default() && colon() } },
        effect = { DefaultLabel })

    fun case_label() = build(
        syntax = { seq { case() && expr() && colon() } },
        effect = { CaseLabel(it(0)) })

    fun switch_label()
        = choice { case_label() || default_label() }

    fun switch_clause() = build(
        syntax = { seq { switch_label() && stmts() } },
        effect = { SwitchClause(it(0), it(1)) })

    fun switch_stmt() = build(
        syntax = { seq { switch() && par_expr() && curlies {repeat0 { switch_clause() } } } },
        effect = { SwitchStmt(it(0), it.list(1)) })

    fun synchronized_stmt() = build(
        syntax = { seq { synchronized() && par_expr() && block() } },
        effect = { SynchronizedStmt(it(1), it(2)) })

    fun return_stmt() = build(
        syntax = { seq { `return`() && maybe { expr() } && semi() } },
        effect = { ReturnStmt(it(0)) })

    fun throw_stmt() = build(
        syntax = { seq { `throw`() && expr() && semi() } },
        effect = { ThrowStmt(it(0)) })

    fun break_stmt() = build(
        syntax = { seq { `break`() && maybe { iden() } && semi() } },
        effect = { BreakStmt(it(0)) })

    fun continue_stmt() = build(
        syntax = { seq { `continue`() && maybe { iden() } && semi() } },
        effect = { ContinueStmt(it(0)) })

    fun assert_stmt() = build(
        syntax = { seq { assert() && expr() && maybe { seq { colon() && expr() } } && semi() } },
        effect = { AssertStmt(it(0), it(1)) })

    fun semi_stmt() = build(
        syntax = { semi() },
        effect = { SemiStmt })

    fun expr_stmt()
        = seq { expr() && semi() }

    fun labelled_stmt() = build(
        syntax = { seq { iden() && colon() && stmt() } },
        effect = { LabelledStmt(it(0), it(1)) })

    fun stmt(): Boolean
        = choice {
            block() || if_stmt() || basic_for_stmt() || enhanced_for_stmt() || while_stmt()
            || do_while_stmt() || try_stmt() || switch_stmt() || synchronized_stmt()
            || return_stmt() || throw_stmt() || break_stmt() || continue_stmt()
            || assert_stmt() || semi_stmt() || expr_stmt() || labelled_stmt()
            || var_decl() || type_decl() }

    fun block() = build(
        syntax = { curlies { repeat0 { stmt() } } },
        effect = { Block(it.list()) })

    fun stmts() = build(
        syntax = { repeat0 { stmt() } },
        effect = { it.list<Stmt>() })

    /// TOP-LEVEL ==================================================================================

    fun package_decl() = build(
        syntax = { seq { annotations() && `package`() && qualified_iden() && semi() } },
        effect = { Package(it(0), it(1)) })

    fun import_decl() = build(
        syntax = {
            seq { import() && as_bool { static() } && qualified_iden()
            && as_bool { seq { dot() && `*`() } } && semi() } },
        effect = { Import(it(0), it(1), it(2)) })

    fun import_decls() = build(
        syntax = { repeat0 { import_decl() } },
        effect = { it.list<Import>() })

    override fun root() = build(
        syntax = { seq { whitespace() && maybe { package_decl() } && import_decls() && type_decls() } },
        effect = { File(it(0), it(1), it(2)) })

    /// ============================================================================================
}
