package norswap.lang.java;

import norswap.autumn.DSL;
import norswap.lang.java.ast.*;

import static norswap.lang.java.LexUtils.*;

public final class Grammar extends DSL
{
    /// LEXICAL ====================================================================================

    // Whitespace ----------------------------------------------------------------------------------

    Wrapper space_char          = cpred(Character::isWhitespace);
    Wrapper not_line            = seq(str("\n").not(), any);
    Wrapper line_comment        = seq("//", not_line.at_least(0), str("\n").opt());

    Wrapper not_comment_term    = seq(str("*/").not(), any);
    Wrapper multi_comment       = seq("/*", not_comment_term.at_least(0), "*/");

    {
        ws = choice(space_char, line_comment, multi_comment).at_least(0).get();
    }

    // Keywords and Operators ----------------------------------------------------------------------

    Wrapper _boolean         = word("boolean")      .token();
    Wrapper _byte            = word("byte")         .token();
    Wrapper _char            = word("char")         .token();
    Wrapper _double          = word("double")       .token();
    Wrapper _float           = word("float")        .token();
    Wrapper _int             = word("int")          .token();
    Wrapper _long            = word("long")         .token();
    Wrapper _short           = word("short")        .token();
    Wrapper _void            = word("void")         .token();
    Wrapper _abstract        = word("abstract")     .token();
    Wrapper _default         = word("default")      .token();
    Wrapper _final           = word("final")        .token();
    Wrapper _native          = word("native")       .token();
    Wrapper _private         = word("private")      .token();
    Wrapper _protected       = word("protected")    .token();
    Wrapper _public          = word("public")       .token();
    Wrapper _static          = word("static")       .token();
    Wrapper _strictfp        = word("strictfp")     .token();
    Wrapper _synchronized    = word("synchronized") .token();
    Wrapper _transient       = word("transient")    .token();
    Wrapper _volatile        = word("volatile")     .token();
    Wrapper _assert          = word("assert")       .token();
    Wrapper _break           = word("break")        .token();
    Wrapper _case            = word("case")         .token();
    Wrapper _catch           = word("catch")        .token();
    Wrapper _class           = word("class")        .token();
    Wrapper _const           = word("const")        .token();
    Wrapper _continue        = word("continue")     .token();
    Wrapper _do              = word("do")           .token();
    Wrapper _else            = word("else")         .token();
    Wrapper _enum            = word("enum")         .token();
    Wrapper _extends         = word("extends")      .token();
    Wrapper _finally         = word("finally")      .token();
    Wrapper _for             = word("for")          .token();
    Wrapper _goto            = word("goto")         .token();
    Wrapper _if              = word("if")           .token();
    Wrapper _implements      = word("implements")   .token();
    Wrapper _import          = word("import")       .token();
    Wrapper _interface       = word("interface")    .token();
    Wrapper _instanceof      = word("instanceof")   .token();
    Wrapper _new             = word("new")          .token();
    Wrapper _package         = word("package")      .token();
    Wrapper _return          = word("return")       .token();
    Wrapper _super           = word("super")        .token();
    Wrapper _switch          = word("switch")       .token();
    Wrapper _this            = word("this")         .token();
    Wrapper _throws          = word("throws")       .token();
    Wrapper _throw           = word("throw")        .token();
    Wrapper _try             = word("try")          .token();
    Wrapper _while           = word("while")        .token();

    Wrapper _BANG            = word("!")            .token();
    Wrapper _BANGEQ          = word("!=")           .token();
    Wrapper _PERCENT         = word("%")            .token();
    Wrapper _PERCENTEQ       = word("%=")           .token();
    Wrapper _AMP             = word("&")            .token();
    Wrapper _AMPAMP          = word("&&")           .token();
    Wrapper _AMPEQ           = word("&=")           .token();
    Wrapper _LPAREN          = word("(")            .token();
    Wrapper _RPAREN          = word(")")            .token();
    Wrapper _STAR            = word("*")            .token();
    Wrapper _STAREQ          = word("*=")           .token();
    Wrapper _PLUS            = word("+")            .token();
    Wrapper _PLUSPLUS        = word("++")           .token();
    Wrapper _PLUSEQ          = word("+=")           .token();
    Wrapper _COMMA           = word(",")            .token();
    Wrapper _SUB             = word("-")            .token();
    Wrapper _SUBSUB          = word("--")           .token();
    Wrapper _SUBEQ           = word("-=")           .token();
    Wrapper _EQ              = word("=")            .token();
    Wrapper _EQEQ            = word("==")           .token();
    Wrapper _QUES            = word("?")            .token();
    Wrapper _CARET           = word("^")            .token();
    Wrapper _CARETEQ         = word("^=")           .token();
    Wrapper _LBRACE          = word("{")            .token();
    Wrapper _RBRACE          = word("}")            .token();
    Wrapper _BAR             = word("|")            .token();
    Wrapper _BARBAR          = word("||")           .token();
    Wrapper _BAREQ           = word("|=")           .token();
    Wrapper _TILDE           = word("~")            .token();
    Wrapper _MONKEYS_AT      = word("@")            .token();
    Wrapper _DIV             = word("/")            .token();
    Wrapper _DIVEQ           = word("/=")           .token();
    Wrapper _GT              = word(">")            .token();
    Wrapper _LT              = word("<")            .token();
    Wrapper _GTEQ            = word(">=")           .token();
    Wrapper _LTEQ            = word("<=")           .token();
    Wrapper _LTLT            = word("<<")           .token();
    Wrapper _LTLTEQ          = word("<<=")          .token();
    Wrapper _GTGT            = word(">>")           .token();
    Wrapper _GTGTEQ          = word(">>=")          .token();
    Wrapper _GTGTGT          = word(">>>")          .token();
    Wrapper _GTGTGTEQ        = word(">>>=")         .token();
    Wrapper _LBRACKET        = word("[")            .token();
    Wrapper _RBRACKET        = word("]")            .token();
    Wrapper _ARROW           = word("->")           .token();
    Wrapper _COL             = word(":")            .token();
    Wrapper _COLCOL          = word("::")           .token();
    Wrapper _SEMI            = word(";")            .token();
    Wrapper _DOT             = word(".")            .token();
    Wrapper _ELLIPSIS        = word("...")          .token();

    Wrapper _false           = word("false")          .as_val(false).token();
    Wrapper _true            = word("true")           .as_val(true).token();
    // TODO Null node
    Wrapper _null            = word("null")           .as_val(null).token();

    // Identifiers ---------------------------------------------------------------------------------

    Wrapper id_start    = cpred(Character::isJavaIdentifierStart);
    Wrapper id_part     = cpred(Character::isJavaIdentifierPart);
    Wrapper iden        = seq(id_start, id_part.at_least(0)).token();

    // Numerals - Common Parts ---------------------------------------------------------------------

    Wrapper underscore  = str("_");
    Wrapper dlit        = str(".");
    Wrapper hex_prefix  = choice("0x", "0x");
    Wrapper underscores = underscore.at_least(0);
    Wrapper digits1     = digit.sep(1, underscores);
    Wrapper digits0     = digit.sep(0, underscores);
    Wrapper hex_digits  = hex_digit.sep(1, underscores);
    Wrapper hex_num     = seq(hex_prefix, hex_digits);

    // Numerals - Floating Point -------------------------------------------------------------------

    Wrapper hex_significand = choice(
        seq(hex_prefix, hex_digits.opt(), dlit, hex_digits),
        seq(hex_num, dlit.opt()));

    Wrapper exp_sign_opt        = set("+-").opt();
    Wrapper exponent            = seq(set("eE"), exp_sign_opt, digits1);
    Wrapper binary_exponent     = seq(set("pP"), exp_sign_opt, digits1);
    Wrapper float_suffix        = set("fFdD");
    Wrapper float_suffix_opt    = float_suffix.opt();
    Wrapper hex_float_lit       = seq(hex_significand, binary_exponent, float_suffix_opt);

    Wrapper decimal_float_lit = choice(
        seq(digits1, dlit, digits0, exponent.opt(), float_suffix_opt),
        seq(dlit, digits1, exponent.opt(), float_suffix_opt),
        seq(digits1, exponent, float_suffix_opt),
        seq(digits1, exponent.opt(), float_suffix));

    Wrapper float_literal = choice(hex_float_lit, decimal_float_lit)
        .reduce_str((p,str,xs) -> p.push(parse_floating(str)))
        .token();

    // Numerals - Integral -------------------------------------------------------------------------

    Wrapper bit             = set("01");
    Wrapper binary_prefix   = choice("0b", "0B");
    Wrapper binary_num      = seq(binary_prefix, bit.at_least(1).sep(1, underscores));
    Wrapper octal_num       = seq("0", seq(underscores, octal_digit).at_least(1));
    Wrapper decimal_num     = choice("0", digits1);
    Wrapper integer_num     = choice(hex_num, binary_num, octal_num, decimal_num);

    Wrapper integer_literal = seq(integer_num, set("lL").opt())
        .reduce_str((p,str,xs) -> p.push(parse_integer(str)))
        .token();

    // Characters and Strings ----------------------------------------------------------------------

    Wrapper octal_code_3    = seq(range('0', '3'), octal_digit, octal_digit);
    Wrapper octal_code_2    = seq(octal_digit, octal_digit.opt());
    Wrapper octal_code      = choice(octal_code_3, octal_code_2);
    Wrapper unicode_code    = seq(str("u").at_least(1), hex_digit.repeat(4));
    Wrapper escape_suffix   = choice(set("btnfr\"'\\"), octal_code, unicode_code);
    Wrapper escape          = seq("\\", escape_suffix);
    Wrapper naked_char      = choice(escape, seq(set("'\\\n\r").not(), any));
    Wrapper nake_str_char   = choice(escape, seq(set("\"\\\n\r").not(), any));

    Wrapper char_literal = seq("'", naked_char, "'")
        .reduce_str((p,str,xs) -> p.push(parse_char(str)))
        .token();

    Wrapper string_literal = seq("\"", nake_str_char.at_least(0), "\"")
        .reduce_str((p,str,xs) -> p.push(parse_string(str)))
        .token();

    // Literal ----------------------------------------------------------------

    Wrapper literal = token_choice(
            integer_literal, string_literal, _null, float_literal, _true, _false, char_literal)
        .push((p,xs) -> new Literal(xs[0]));

    // ---------------------------------------------------------------------------------------------
    {
        build_tokenizer();
    }
    // ---------------------------------------------------------------------------------------------

//    /// ANNOTATIONS ================================================================================
//
//    Parser annotation_element = choice(lazy("ternary", () -> this.ternary), lazy("annotation_element_list", () -> this.annotation_element_list), lazy("annotation", () -> this.annotation)).get();
//
//    Parser annotation_inner_list = lazy("annotation_element", () -> this.annotation_element).sep_trailing(0, COMMA).get();
//
//    Parser annotation_element_list = annotation_inner_list.bracketed("{}").reduce((p,xs) -> {AnnotationElementList(it.list())}.get();
//
//    Parser annotation_element_pair = seq(iden, EQ, annotation_element).reduce((p,xs) -> {Pair<String, AnnotationElement>(it(0), it(1))}.get();
//
//    Parser normal_annotation_suffix = annotation_element_pair.sep(1, COMMA).bracketed("()").reduce((p,xs) -> {val elements = it.list<Pair<String, AnnotationElement>>(1).unzip()
//        NormalAnnotation(it(0), elements.first, elements.second)}.get();
//
//    Parser single_element_annotation_suffix = annotation_element.bracketed("()").reduce((p,xs) -> {SingleElementAnnotation(it(0), it(1))}.get();
//
//    Parser marker_annotation_suffix = seq(LPAREN, RPAREN).opt().reduce((p,xs) -> {MarkerAnnotation(it(0))}.get();
//
//    Parser annotation_suffix = choice(normal_annotation_suffix, single_element_annotation_suffix, marker_annotation_suffix).get();
//
//    Parser qualified_iden = iden.sep(1, DOT).reduce((p, xs) -> {it.list<String>()}.get();
//
//    Parser annotation = seq(MONKEYS_AT, qualified_iden, annotation_suffix).get();
//
//    Parser annotations = annotation.at_least(0).reduce((p,xs) -> {it.list<Annotation>()}.get();
//
//    /// TYPES ======================================================================================
//
//    Parser basic_type = tokens.token_choice(byte, short, int, long, char, float, double, boolean, void).get();
//
//    Parser primitive_type = seq(annotations, basic_type).reduce((p,xs) -> {PrimitiveType(it(0), it(1))}.get();
//
//    Parser extends_bound = seq(extends, lazy("type", () -> this.type)).reduce((p,xs) -> {ExtendsBound(it(0))}.get();
//
//    Parser super_bound = seq(`super`, lazy("type", () -> this.type)).reduce((p,xs) -> {SuperBound(it(0))}.get();
//
//    Parser type_bound = choice(extends_bound, super_bound).maybe().get();
//
//    Parser wildcard = seq(annotations, QUES, type_bound).reduce((p,xs) -> {Wildcard(it(0), it(1))}.get();
//
//    Parser type_args = choice(lazy("type", () -> this.type), wildcard).sep(0, COMMA).bracketed("<>").opt().reduce((p,xs) -> {it.list<Type>()}.get();
//
//    Parser class_type_part = seq(annotations, iden, type_args).reduce((p,xs) -> {ClassTypePart(it(0), it(1), it(2))}.get();
//
//    Parser class_type = class_type_part.sep(1, DOT).reduce((p, xs) -> {ClassType(it.list<ClassTypePart>())}.get();
//
//    Parser stem_type = choice(primitive_type, class_type).get();
//
//    Parser dim = seq(annotations, seq(LBRACKET, RBRACKET)).reduce((p,xs) -> {Dimension(it(0))}.get();
//
//    Parser dims = dim.at_least(0).reduce((p,xs) -> {it.list<Dimension>()}.get();
//
//    Parser dims1 = dim.at_least(1).reduce((p,xs) -> {it.list<Dimension>()}.get();
//
//    Parser type_dim_suffix = dims1.reduce((p,xs) -> {ArrayType(it(0), it(1))}.get();
//
//    Parser type = seq(stem_type, type_dim_suffix.opt()).get();
//
//    Parser type_union_syntax = lazy("type", () -> this.type).sep(1, AMP).get();
//
//    Parser type_union = type_union_syntax.reduce((p,xs) -> {it.list<Type>()}.get();
//
//    Parser type_bounds = seq(extends, type_union_syntax).opt().reduce((p,xs) -> {it.list<Type>()}.get();
//
//    Parser type_param = seq(annotations, iden, type_bounds).reduce((p,xs) -> {TypeParam(it(0), it(1), it(2))}.get();
//
//    Parser type_params = type_param.sep(0, COMMA).bracketed("<>").opt().reduce((p,xs) -> {it.list<TypeParam>()}.get();
//
//    /// MODIFIERS ==================================================================================
//
//    Parser keyword_modifier = choice(public, protected, private, abstract, static, final, synchronized, native, strictfp, default, transient, volatile).reduce((p,xs) -> {Keyword.valueOf(it(0))}.get();
//
//    Parser modifier = choice(annotation, keyword_modifier).get();
//
//    Parser modifiers = modifier.at_least(0).reduce((p,xs) -> {it.list<Modifier>()}.get();
//
//    /// PARAMETERS =================================================================================
//
//    Parser args = lazy("expr", () -> this.expr).sep(0, COMMA).bracketed("()").reduce((p,xs) -> {it.list<Expr>()}.get();
//
//    Parser this_parameter_qualifier = seq(iden, DOT).at_least(0).reduce((p, xs) -> {it.list<String>()}.get();
//
//    Parser this_param_suffix = seq(this_parameter_qualifier, `this`).reduce((p,xs) -> {ThisParameter(it(0), it(1), it(2))}.get();
//
//    Parser iden_param_suffix = seq(iden, dims).reduce((p,xs) -> {IdenParameter(it(0), it(1), it(2), it(3))}.get();
//
//    Parser variadic_param_suffix = seq(annotations, ELLIPSIS, iden).reduce((p, xs) -> {VariadicParameter(it(0), it(1), it(2), it(3))}.get();
//
//    Parser formal_param_suffix = choice(iden_param_suffix, this_param_suffix, variadic_param_suffix).get();
//
//    Parser formal_param = seq(modifiers, type, formal_param_suffix).get();
//
//    Parser formal_params = formal_param.sep(0, COMMA).bracketed("()").reduce((p,xs) -> {FormalParameters(it.list())}.get();
//
//    Parser untyped_params = iden.sep(1, COMMA).bracketed("()").reduce((p,xs) -> {UntypedParameters(it.list())}.get();
//
//    Parser single_param = iden.reduce((p,xs) -> {UntypedParameters(it.list<String>())}.get();
//
//    Parser lambda_params = choice(formal_params, untyped_params, single_param).get();
//
//    /// NON-TYPE DECLARATIONS ======================================================================
//
//    Parser var_init = choice(lazy("expr", () -> this.expr), lazy("array_init", () -> this.array_init)).get();
//
//    Parser array_init = var_init.sep_trailing(0, COMMA).bracketed("{}").reduce((p,xs) -> {ArrayInit(it.list())}.get();
//
//    Parser var_declarator_id = seq(iden, dims).reduce((p,xs) -> {VarDeclaratorID(it(0), it(1))}.get();
//
//    Parser var_declarator = seq(var_declarator_id, seq(EQ, var_init).maybe()).reduce((p,xs) -> {VarDeclarator(it(0), it(1))}.get();
//
//    Parser var_decl_no_semi = seq(type, var_declarator.sep(1, COMMA)).reduce((p,xs) -> {VarDecl(it(0), it(1), it.list(2))}.get();
//
//    Parser var_decl_suffix = seq(var_decl_no_semi, SEMI).get();
//
//    Parser var_decl = seq(modifiers, var_decl_suffix).get();
//
//    Parser throws_clause = seq(throws, type.sep(1, COMMA)).opt().reduce((p,xs) -> {it.list<Type>()}.get();
//
//    Parser block_or_semi = choice(lazy("block", () -> this.block), SEMI.as_val(null)).get();
//
//    Parser method_decl_suffix = seq(type_params, type, iden, formal_params, dims, throws_clause, block_or_semi).reduce((p,xs) -> {MethodDecl(it(0), it(1), it(2), it(3), it(4), it(5), it(6), it(7))}.get();
//
//    Parser constructor_decl_suffix = seq(type_params, iden, formal_params, throws_clause, lazy("block", () -> this.block)).reduce((p,xs) -> {ConstructorDecl(it(0), it(1), it(2), it(3), it(4), it(5))}.get();
//
//    Parser init_block = seq(static.as_bool(), lazy("block", () -> this.block)).reduce((p,xs) -> {InitBlock(it(0), it(1))}.get();
//
//    /// TYPE DECLARATIONS ==========================================================================
//
//    // Common -----------------------------------------------------------------
//
//    Parser extends_clause = seq(extends, type.sep(0, COMMA)).opt().reduce((p,xs) -> {it.list<Type>()}.get();
//
//    Parser implements_clause = seq(implements, type.sep(0, COMMA)).opt().reduce((p,xs) -> {it.list<Type>()}.get();
//
//    Parser type_sig = seq(iden, type_params, extends_clause, implements_clause).get();
//
//    Parser class_modified_decl = seq(modifiers, choice(var_decl_suffix, method_decl_suffix, constructor_decl_suffix, lazy("type_decl_suffix", () -> this.type_decl_suffix))).get();
//
//    Parser class_body_decl = choice(class_modified_decl, init_block, SEMI).get();
//
//    Parser class_body_decls = class_body_decl.at_least(0).reduce((p,xs) -> {it.list<Decl>()}.get();
//
//    Parser type_body = class_body_decls.bracketed("{}").get();
//
//    // Enum -------------------------------------------------------------------
//
//    Parser enum_constant = seq(annotations, iden, args.maybe(), type_body.maybe()).reduce((p,xs) -> {EnumConstant(it(0), it(1), it(2), it(3))}.get();
//
//    Parser enum_class_decls = seq(SEMI, class_body_decl.at_least(0)).opt().reduce((p, xs) -> {it.list<Decl>()}.get();
//
//    Parser enum_constants = enum_constant.sep(1, COMMA).opt().reduce((p,xs) -> {it.list<EnumConstant>()}.get();
//
//    Parser enum_body = seq(enum_constants, enum_class_decls).bracketed("{}").collect((p,xs) -> stack.push(it(1)) ; stack.push(it(0)) /* swap */).get();
//
//    Parser enum_decl = seq(enum, type_sig, enum_body).reduce((p,xs) -> {val td = TypeDecl(input, ENUM, it(0), it(1), it(2), it(3), it(4), it(5))
//    EnumDecl(td, it(6))}.get();
//
//    // Annotations ------------------------------------------------------------
//
//    Parser annot_default_clause = seq(default, annotation_element).reduce((p,xs) -> {it(1)}.get();
//
//    Parser annot_elem_decl = seq(modifiers, type, iden, seq(LPAREN, RPAREN), dims, annot_default_clause.maybe(), SEMI).reduce((p, xs) -> {AnnotationElemDecl(it(0), it(1), it(2), it(3), it(4))}.get();
//
//    Parser annot_body_decls = choice(annot_elem_decl, class_body_decl).at_least(0).reduce((p,xs) -> {it.list<Decl>()}.get();
//
//    Parser annotation_decl = seq(MONKEYS_AT, `interface`, type_sig, annot_body_decls.bracketed("{}")).reduce((p,xs) -> {TypeDecl(input, ANNOTATION, it(0), it(1), it(2), it(3), it(4), it(5))}.get();
//
//    //// ------------------------------------------------------------------------
//
//    Parser class_decl = seq(`class`, type_sig, type_body).reduce((p,xs) -> {TypeDecl(input, CLASS, it(0), it(1), it(2), it(3), it(4), it(5))}.get();
//
//    Parser interface_declaration = seq(`interface`, type_sig, type_body).reduce((p,xs) -> {TypeDecl(input, INTERFACE, it(0), it(1), it(2), it(3), it(4), it(5))}.get();
//
//    Parser type_decl_suffix = choice(class_decl, interface_declaration, enum_decl, annotation_decl).get();
//
//    Parser type_decl = seq(modifiers, type_decl_suffix).get();
//
//    Parser type_decls = choice(type_decl, SEMI).at_least(0).reduce((p, xs) -> {it.list<Decl>()}.get();
//
//    /// EXPRESSIONS ================================================================================
//
//    // Array Constructor ------------------------------------------------------
//
//    Parser dim_expr = seq(annotations, lazy("expr", () -> this.expr).bracketed("[]")).reduce((p,xs) -> {DimExpr(it(0), it(1))}.get();
//
//    Parser dim_exprs = dim_expr.at_least(1).reduce((p,xs) -> {it.list<DimExpr>()}.get();
//
//    Parser dim_expr_array_creator = seq(stem_type, dim_exprs, dims).reduce((p,xs) -> {ArrayCtorCall(it(0), it(1), it(2), null)}.get();
//
//    Parser init_array_creator = seq(stem_type, dims1, array_init).reduce((p,xs) -> {ArrayCtorCall(it(0), emptyList(), it(1), it(2))}.get();
//
//    Parser array_ctor_call = seq(new, choice(dim_expr_array_creator, init_array_creator)).get();
//
//    // Lambda Expression ------------------------------------------------------
//
//    Parser lambda = seq(lambda_params, ARROW, choice(lazy("block", () -> this.block), lazy("expr", () -> this.expr))).reduce((p, xs) -> {Lambda(it(0), it(1))}.get();
//
//    // Expression - Primary ---------------------------------------------------
//
//    Parser par_expr = lazy("expr", () -> this.expr).bracketed("()").reduce((p,xs) -> {ParenExpr(it(0))}.get();
//
//    Parser ctor_call = seq(new, type_args, stem_type, args, type_body.maybe()).reduce((p,xs) -> {CtorCall(it(0), it(1), it(2), it(3))}.get();
//
//    Parser new_ref_suffix = new.reduce((p,xs) -> {NewReference(it(0), it(1))}.get();
//
//    Parser method_ref_suffix = iden.reduce((p,xs) -> {MaybeBoundMethodReference(it(0), it(1), it(2))}.get();
//
//    Parser ref_suffix = seq(COLCOL, type_args, choice(new_ref_suffix, method_ref_suffix)).get();
//
//    Parser class_expr_suffix = seq(DOT, `class`).reduce((p, xs) -> {ClassExpr(it(0))}.get();
//
//    Parser type_suffix_expr = seq(type, choice(ref_suffix, class_expr_suffix)).get();
//
//    Parser iden_or_method_expr = seq(iden, args.maybe()).reduce((p,xs) -> {it[1] ?. let { MethodCall(null, listOf(), it(0), it(1)) } ?: Identifier(it(0))}.get();
//
//    Parser this_expr = seq(`this`, args.maybe()).reduce((p,xs) -> {it[0] ?. let { ThisCall(it(0)) } ?: This}.get();
//
//    Parser super_expr = seq(`super`, args.maybe()).reduce((p,xs) -> {it[0] ?. let { SuperCall(it(0)) } ?: Super}.get();
//
//    Parser class_expr = seq(type, DOT, `class`).reduce((p, xs) -> {ClassExpr(it(0))}.get();
//
//    Parser primary_expr = choice(par_expr, array_ctor_call, ctor_call, type_suffix_expr, iden_or_method_expr, this_expr, super_expr, literal).get();
//
//    // Expression - Postfix ---------------------------------------------------
//
//    Parser dot_this = `this`.reduce((p,xs) -> {DotThis(it(0))}.get();
//
//    Parser dot_super = `super`.reduce((p,xs) -> {DotSuper(it(0))}.get();
//
//    Parser dot_iden = iden.reduce((p,xs) -> {DotIden(it(0), it(1))}.get();
//
//    Parser dot_new = ctor_call.reduce((p,xs) -> {DotNew(it(0), it(1))}.get();
//
//    Parser dot_method = seq(type_args, iden, args).reduce((p,xs) -> {MethodCall(it(0), it(1), it(2), it(3))}.get();
//
//    Parser dot_postfix = choice(dot_method, dot_iden, dot_this, dot_super, dot_new).get();
//
//    Parser ref_postfix = seq(COLCOL, type_args, iden).reduce((p, xs) -> {BoundMethodReference(it(0), it(1), it(2))}.get();
//
//    Parser array_postfix = lazy("expr", () -> this.expr).bracketed("[]").reduce((p,xs) -> {ArrayAccess(it(0), it(1))}.get();
//
//    Parser inc_suffix = PLUSPLUS.reduce((p,xs) -> {PostIncrement(it(0))}.get();
//
//    Parser dec_suffix = SUBSUB.reduce((p,xs) -> {PostDecrement(it(0))}.get();
//
//    Parser postfix = choice(seq(DOT, dot_postfix), array_postfix, inc_suffix, dec_suffix, ref_postfix).get();
//
//    Parser postfix_expr = seq(primary_expr, postfix.at_least(0)).get();
//
//    Parser inc_prefix = seq(PLUSPLUS, lazy("prefix_expr", () -> this.prefix_expr)).reduce((p,xs) -> {PreIncrement(it(0))}.get();
//
//    Parser dec_prefix = seq(SUBSUB, lazy("prefix_expr", () -> this.prefix_expr)).reduce((p,xs) -> {PreDecrement(it(0))}.get();
//
//    Parser unary_plus = seq(PLUS, lazy("prefix_expr", () -> this.prefix_expr)).reduce((p,xs) -> {UnaryPlus(it(0))}.get();
//
//    Parser unary_minus = seq(SUB, lazy("prefix_expr", () -> this.prefix_expr)).reduce((p,xs) -> {UnaryMinus(it(0))}.get();
//
//    Parser complement = seq(TILDE, lazy("prefix_expr", () -> this.prefix_expr)).reduce((p,xs) -> {Complement(it(0))}.get();
//
//    Parser not = seq(BANG, lazy("prefix_expr", () -> this.prefix_expr)).reduce((p,xs) -> {Negate(it(0))}.get();
//
//    Parser cast = seq(type_union.bracketed("()"), choice(lambda, lazy("prefix_expr", () -> this.prefix_expr))).reduce((p,xs) -> {Cast(it(0), it(1))}.get();
//
//    Parser prefix_expr = choice(inc_prefix, dec_prefix, unary_plus, unary_minus, complement, not, cast, postfix_expr).get();
//
//    // Expression - Binary ----------------------------------------------------
//
//    Parser mult_expr = AssocLeft(this) {
//    operands = prefix_expr
//    op(STAR, { Product(it(0), it(1)) })
//    op(DIV, { Division(it(0), it(1)) })
//    op(PERCENT, { Remainder(it(0), it(1)) })
//}.get();
//
//    Parser add_expr = AssocLeft(this) {
//    operands = mult_expr
//    op(PLUS, { Sum(it(0), it(1)) })
//    op(SUB, { Diff(it(0), it(1)) })
//}.get();
//
//    Parser shift_expr = AssocLeft(this) {
//    operands = add_expr
//    op(LTLT, { ShiftLeft(it(0), it(1)) })
//    op(GTGT, { ShiftRight(it(0), it(1)) })
//    op(GTGTGT, { BinaryShiftRight(it(0), it(1)) })
//}.get();
//
//    Parser order_expr = AssocLeft(this) {
//    operands = shift_expr
//    op(LT, { Lower(it(0), it(1)) })
//    op(LTEQ, { LowerEqual(it(0), it(1)) })
//    op(GT, { Greater(it(0), it(1)) })
//    op(GTEQ, { GreaterEqual(it(0), it(1)) })
//    postfix(seq(instanceof, type), { Instanceof(it(0), it(1)) })
//}.get();
//
//    Parser eq_expr = AssocLeft(this) {
//    operands = order_expr
//    op(EQEQ, { Equal(it(0), it(1)) })
//    op(BANGEQ, { NotEqual(it(0), it(1)) })
//}.get();
//
//    Parser binary_and_expr = AssocLeft(this) {
//    operands = eq_expr
//    op(AMP, { BinaryAnd(it(0), it(1)) })
//}.get();
//
//    Parser xor_expr = AssocLeft(this) {
//    operands = binary_and_expr
//    op(CARET, { Xor(it(0), it(1)) })
//}.get();
//
//    Parser binary_or_expr = AssocLeft(this) {
//    operands = xor_expr
//    op(BAR, { BinaryOr(it(0), it(1)) })
//}.get();
//
//    Parser and_expr = AssocLeft(this) {
//    operands = binary_or_expr
//    op(AMPAMP, { And(it(0), it(1)) })
//}.get();
//
//    Parser or_expr = AssocLeft(this) {
//    operands = and_expr
//    op(BARBAR, { Or(it(0), it(1)) })
//}.get();
//
//    Parser ternary_suffix = seq(QUES, lazy("expr", () -> this.expr), COL, lazy("expr", () -> this.expr)).reduce((p, xs) -> {Ternary(it(0), it(1), it(2))}.get();
//
//    Parser ternary = seq(or_expr, ternary_suffix.opt()).get();
//
//    Parser assignment_suffix = choice(seq(EQ, lazy("expr", () -> this.expr)).reduce((p,xs) -> {Assign(it(0), it(1), "=")}, seq(PLUSEQ, lazy("expr", () -> this.expr)).reduce((p,xs) -> {Assign(it(0), it(1), "+=")}, seq(SUBEQ, lazy("expr", () -> this.expr)).reduce((p,xs) -> {Assign(it(0), it(1), "-=")}, seq(STAREQ, lazy("expr", () -> this.expr)).reduce((p,xs) -> {Assign(it(0), it(1), "*=")}, seq(DIVEQ, lazy("expr", () -> this.expr)).reduce((p, xs) -> {Assign(it(0), it(1), "/=")}, seq(PERCENTEQ, lazy("expr", () -> this.expr)).reduce((p, xs) -> {Assign(it(0), it(1), "%=")}, seq(LTLTEQ, lazy("expr", () -> this.expr)).reduce((p, xs) -> {Assign(it(0), it(1), "<<=")}, seq(GTGTEQ, lazy("expr", () -> this.expr)).reduce((p, xs) -> {Assign(it(0), it(1), ">>=")}, seq(GTGTGTEQ, lazy("expr", () -> this.expr)).reduce((p, xs) -> {Assign(it(0), it(1), ">>>=")}, seq(AMPEQ, lazy("expr", () -> this.expr)).reduce((p, xs) -> {Assign(it(0), it(1), "&=")}, seq(CARETEQ, lazy("expr", () -> this.expr)).reduce((p, xs) -> {Assign(it(0), it(1), "^=")}, seq(BAREQ, lazy("expr", () -> this.expr)).reduce((p, xs) -> {Assign(it(0), it(1), "|=")}).get();
//
//    Parser assignment = seq(ternary, assignment_suffix.opt()).get();
//
//    Parser expr = choice(lambda, assignment).get();
//
//    /// STATEMENTS =================================================================================
//
//    Parser if_stmt = seq(`if`, par_expr, lazy("stmt", () -> this.stmt), seq(`else`, lazy("stmt", () -> this.stmt)).maybe()).reduce((p,xs) -> {If(it(0), it(1), it(2))}.get();
//
//    Parser expr_stmt_list = expr.sep(0, COMMA).reduce((p,xs) -> {it.list<Stmt>()}.get();
//
//    Parser for_init_decl = seq(modifiers, var_decl_no_semi).reduce((p,xs) -> {it.list<Stmt>()}.get();
//
//    Parser for_init = choice(for_init_decl, expr_stmt_list).get();
//
//    Parser basic_for_paren_part = seq(for_init, SEMI, expr.maybe(), SEMI, expr_stmt_list.opt()).get();
//
//    Parser basic_for_stmt = seq(`for`, basic_for_paren_part.bracketed("()"), lazy("stmt", () -> this.stmt)).reduce((p,xs) -> {BasicFor(it(0), it(1), it(2), it(3))}.get();
//
//    Parser for_val_decl = seq(modifiers, type, var_declarator_id, COL, expr).get();
//
//    Parser enhanced_for_stmt = seq(`for`, for_val_decl.bracketed("()"), lazy("stmt", () -> this.stmt)).reduce((p,xs) -> {EnhancedFor(it(0), it(1), it(2), it(3), it(4))}.get();
//
//    Parser while_stmt = seq(`while`, par_expr, lazy("stmt", () -> this.stmt)).reduce((p,xs) -> {WhileStmt(it(0), it(1))}.get();
//
//    Parser do_while_stmt = seq(`do`, lazy("stmt", () -> this.stmt), `while`, par_expr, SEMI).reduce((p, xs) -> {DoWhileStmt(it(0), it(1))}.get();
//
//    Parser catch_parameter_types = type.sep(0, BAR).reduce((p,xs) -> {it.list<Type>()}.get();
//
//    Parser catch_parameter = seq(modifiers, catch_parameter_types, var_declarator_id).get();
//
//    Parser catch_clause = seq(catch, catch_parameter.bracketed("()"), lazy("block", () -> this.block)).reduce((p,xs) -> {CatchClause(it(0), it(1), it(2), it(3))}.get();
//
//    Parser catch_clauses = catch_clause.at_least(0).reduce((p,xs) -> {it.list<CatchClause>()}.get();
//
//    Parser finally_clause = seq(finally, lazy("block", () -> this.block)).get();
//
//    Parser resource = seq(modifiers, type, var_declarator_id, EQ, expr).reduce((p,xs) -> {TryResource(it(0), it(1), it(2), it(3))}.get();
//
//    Parser resources = resource.sep(1, SEMI).bracketed("()").opt().reduce((p, xs) -> {it.list<TryResource>()}.get();
//
//    Parser try_stmt = seq(`try`, resources, lazy("block", () -> this.block), catch_clauses, finally_clause.maybe()).reduce((p,xs) -> {TryStmt(it(0), it(1), it(2), it(3))}.get();
//
//    Parser default_label = seq(default, COL).reduce((p, xs) -> {DefaultLabel}.get();
//
//    Parser case_label = seq(case, expr, COL).reduce((p, xs) -> {CaseLabel(it(0))}.get();
//
//    Parser switch_label = choice(case_label, default_label).get();
//
//    Parser switch_clause = seq(switch_label, lazy("stmts", () -> this.stmts)).reduce((p,xs) -> {SwitchClause(it(0), it(1))}.get();
//
//    Parser switch_stmt = seq(switch, par_expr, switch_clause.at_least(0).bracketed("{}")).reduce((p,xs) -> {SwitchStmt(it(0), it.list(1))}.get();
//
//    Parser synchronized_stmt = seq(synchronized, par_expr, lazy("block", () -> this.block)).reduce((p,xs) -> {SynchronizedStmt(it(1), it(2))}.get();
//
//    Parser return_stmt = seq(`return`, expr.maybe(), SEMI).reduce((p, xs) -> {ReturnStmt(it(0))}.get();
//
//    Parser throw_stmt = seq(`throw`, expr, SEMI).reduce((p, xs) -> {ThrowStmt(it(0))}.get();
//
//    Parser break_stmt = seq(`break`, iden.maybe(), SEMI).reduce((p, xs) -> {BreakStmt(it(0))}.get();
//
//    Parser continue_stmt = seq(`continue`, iden.maybe(), SEMI).reduce((p, xs) -> {ContinueStmt(it(0))}.get();
//
//    Parser assert_stmt = seq(assert, expr, seq(COL, expr).maybe(), semi).reduce((p, xs) -> {AssertStmt(it(0), it(1))}.get();
//
//    Parser semi_stmt = SEMI.reduce((p, xs) -> {SemiStmt}.get();
//
//    Parser expr_stmt = seq(expr, SEMI).get();
//
//    Parser labelled_stmt = seq(iden, COL, lazy("stmt", () -> this.stmt)).reduce((p, xs) -> {LabelledStmt(it(0), it(1))}.get();
//
//    Parser stmt = choice(lazy("block", () -> this.block), if_stmt, basic_for_stmt, enhanced_for_stmt, while_stmt, do_while_stmt, try_stmt, switch_stmt, synchronized_stmt, return_stmt, throw_stmt, break_stmt, continue_stmt, assert_stmt, semi_stmt, expr_stmt, labelled_stmt, var_decl, type_decl).get();
//
//    Parser block = stmt.at_least(0).bracketed("{}").reduce((p,xs) -> {Block(it.list())}.get();
//
//    Parser stmts = stmt.at_least(0).reduce((p,xs) -> {it.list<Stmt>()}.get();
//
//    /// TOP-LEVEL ==================================================================================
//
//    Parser package_decl = seq(annotations, `package`, qualified_iden, SEMI).reduce((p, xs) -> {Package(it(0), it(1))}.get();
//
//    Parser import_decl = seq(import, static.as_bool(), qualified_iden, seq(DOT, STAR).as_bool(), semi).reduce((p, xs) -> {Import(it(0), it(1), it(2))}.get();
//
//    Parser import_decls = import_decl.at_least(0).reduce((p,xs) -> {it.list<Import>()}.get();
//
//    Parser root = seq(lazy("whitespace", () -> this.whitespace), package_decl.maybe(), import_decls, type_decls).reduce((p,xs) -> {File(input, it(0), it(1), it(2))}.get();
}

