package norswap.lang.java;

import norswap.autumn.DSL;
import norswap.lang.java.ast.*;
import norswap.utils.Pair;

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

    Wrapper BANG            = word("!")            .token();
    Wrapper BANGEQ          = word("!=")           .token();
    Wrapper PERCENT         = word("%")            .token();
    Wrapper PERCENTEQ       = word("%=")           .token();
    Wrapper AMP             = word("&")            .token();
    Wrapper AMPAMP          = word("&&")           .token();
    Wrapper AMPEQ           = word("&=")           .token();
    Wrapper LPAREN          = word("(")            .token();
    Wrapper RPAREN          = word(")")            .token();
    Wrapper STAR            = word("*")            .token();
    Wrapper STAREQ          = word("*=")           .token();
    Wrapper PLUS            = word("+")            .token();
    Wrapper PLUSPLUS        = word("++")           .token();
    Wrapper PLUSEQ          = word("+=")           .token();
    Wrapper COMMA           = word(",")            .token();
    Wrapper SUB             = word("-")            .token();
    Wrapper SUBSUB          = word("--")           .token();
    Wrapper SUBEQ           = word("-=")           .token();
    Wrapper EQ              = word("=")            .token();
    Wrapper EQEQ            = word("==")           .token();
    Wrapper QUES            = word("?")            .token();
    Wrapper CARET           = word("^")            .token();
    Wrapper CARETEQ         = word("^=")           .token();
    Wrapper LBRACE          = word("{")            .token();
    Wrapper RBRACE          = word("}")            .token();
    Wrapper BAR             = word("|")            .token();
    Wrapper BARBAR          = word("||")           .token();
    Wrapper BAREQ           = word("|=")           .token();
    Wrapper TILDE           = word("~")            .token();
    Wrapper MONKEYS_AT      = word("@")            .token();
    Wrapper DIV             = word("/")            .token();
    Wrapper DIVEQ           = word("/=")           .token();
    Wrapper GT              = word(">")            .token();
    Wrapper LT              = word("<")            .token();
    Wrapper GTEQ            = word(">=")           .token();
    Wrapper LTEQ            = word("<=")           .token();
    Wrapper LTLT            = word("<<")           .token();
    Wrapper LTLTEQ          = word("<<=")          .token();
    Wrapper GTGTEQ          = word(">>=")          .token();
    Wrapper GTGTGTEQ        = word(">>>=")         .token();
    Wrapper LBRACKET        = word("[")            .token();
    Wrapper RBRACKET        = word("]")            .token();
    Wrapper ARROW           = word("->")           .token();
    Wrapper COL             = word(":")            .token();
    Wrapper COLCOL          = word("::")           .token();
    Wrapper SEMI            = word(";")            .token();
    Wrapper DOT             = word(".")            .token();
    Wrapper ELLIPSIS        = word("...")          .token();

    // These two are not tokens, because they would cause issue with nested generic types.
    // e.g. in List<List<String>>, you want ">>" to lex as [_GT, _GT]

    Wrapper GTGT            = word(">>");
    Wrapper GTGTGT          = word(">>>");

    Wrapper _false           = word("false")          .as_val(false).token();
    Wrapper _true            = word("true")           .as_val(true).token();
    Wrapper _null            = word("null")           .as_val(null).token();

    // Identifiers ---------------------------------------------------------------------------------

    Wrapper id_start    = cpred(Character::isJavaIdentifierStart);
    Wrapper id_part     = cpred(Character::isJavaIdentifierPart);
    Wrapper iden        = seq(id_start, id_part.at_least(0)).token();

    // Numerals - Common Parts ---------------------------------------------------------------------

    Wrapper underscore  = str("_");
    Wrapper dlit        = str(".");
    Wrapper hex_prefix  = choice("0x", "0X");
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

    /// ANNOTATIONS ================================================================================

    Wrapper annotation_element = choice(
        lazy(() -> this.ternary),
        lazy(() -> this.annotation_element_list),
        lazy(() -> this.annotation));

    Wrapper annotation_inner_list
        = lazy(() -> this.annotation_element).sep_trailing(0, COMMA);

    Wrapper annotation_element_list
        = seq(LBRACE, annotation_inner_list, RBRACE)
        .push((p,xs) -> new AnnotationElementList(list(xs)));

    Wrapper annotation_element_pair
        = seq(iden, EQ, annotation_element)
        .push((p,xs) -> new Pair<String, AnnotationElement>($(xs,0), $(xs,1)));

    Wrapper normal_annotation_suffix
        = seq(LPAREN, annotation_element_pair.sep(1, COMMA), RPAREN)
            .push((p,xs) -> new NormalAnnotation($(p.pop()), list(xs)));

    Wrapper single_element_annotation_suffix
        = seq(LPAREN, annotation_element, RPAREN)
        .push((p,xs) -> new SingleElementAnnotation($(xs,0), $(xs,1)));

    Wrapper marker_annotation_suffix
        = seq(LPAREN, RPAREN).opt()
        .push((p,xs) -> new MarkerAnnotation($(xs,0)));

    Wrapper annotation_suffix = choice(
        normal_annotation_suffix,
        single_element_annotation_suffix,
        marker_annotation_suffix);

    Wrapper qualified_iden
        = iden.sep(1, DOT)
        .push((p, xs) -> this.<String>list(xs));

    Wrapper annotation
        = seq(MONKEYS_AT, qualified_iden, annotation_suffix);

    Wrapper annotations
        = annotation.at_least(0)
        .push((p,xs) -> this.<TAnnotation>list(xs));

    // TODO placeholder
    Wrapper ternary = null;

    /// TYPES ======================================================================================

    Wrapper basic_type
        = token_choice(_byte, _short, _int, _long, _char, _float, _double, _boolean, _void);

    Wrapper primitive_type
        = seq(annotations, basic_type)
        .push((p,xs) -> new PrimitiveType($(xs,0), $(xs,1)));

    Wrapper extends_bound
        = seq(_extends, lazy(() -> this.type))
        .push((p,xs) -> new ExtendsBound($(xs,0)));

    Wrapper super_bound
        = seq(_super, lazy(() -> this.type))
        .push((p,xs) -> new SuperBound($(xs,0)));

    Wrapper type_bound
        = choice(extends_bound, super_bound).maybe();

    Wrapper wildcard
        = seq(annotations, QUES, type_bound)
        .push((p,xs) -> new Wildcard($(xs,0), $(xs,1)));

    Wrapper type_args
        = seq(LT, choice(lazy(() -> this.type), wildcard).sep(0, COMMA), GT).opt()
        .push((p,xs) -> this.<TType>list(xs));

    Wrapper class_type_part
        = seq(annotations, iden, type_args)
        .push((p,xs) -> new ClassTypePart($(xs,0), $(xs,1), $(xs,2)));

    Wrapper class_type
        = class_type_part.sep(1, DOT)
        .push((p, xs) -> new ClassType(list(xs)));

    Wrapper stem_type
        = choice(primitive_type, class_type);

    Wrapper dim
        = seq(annotations, seq(LBRACKET, RBRACKET))
        .push((p,xs) -> new Dimension($(xs,0)));

    Wrapper dims
        = dim.at_least(0)
        .push((p,xs) -> this.<Dimension>list(xs));

    Wrapper dims1
        = dim.at_least(1)
        .push((p,xs) -> this.<Dimension>list(xs));

    Wrapper type_dim_suffix
        = dims1
        .push((p,xs) -> new ArrayType($(xs,0), $(xs,1)));

    Wrapper type
        = seq(stem_type, type_dim_suffix.opt());

    Wrapper type_union_syntax
        = lazy(() -> this.type).sep(1, AMP);

    Wrapper type_union
        = type_union_syntax
        .push((p,xs) -> this.<TType>list(xs));

    Wrapper type_bounds
        = seq(_extends, type_union_syntax).opt()
        .push((p,xs) -> this.<TType>list(xs));

    Wrapper type_param
        = seq(annotations, iden, type_bounds)
        .push((p,xs) -> new TypeParam($(xs,0), $(xs,1), $(xs,2)));

    Wrapper type_params
        = seq(LT, type_param.sep(0, COMMA), GT).opt()
        .push((p,xs) -> this.<TypeParam>list(xs));
 
    /// MODIFIERS ==================================================================================

    Wrapper keyword_modifier =
        token_choice(
            _public, _protected, _private, _abstract, _static, _final, _synchronized,
            _native, _strictfp, _default, _transient, _volatile)
        .reduce_str((p,str,xs) -> Keyword.valueOf("_" + trim_trailing_whitespace(str)));

    Wrapper modifier =
        choice(annotation, keyword_modifier);

    Wrapper modifiers =
        modifier.at_least(0)
        .push((p,xs) -> this.<Modifier>list(xs));

//    /// PARAMETERS =================================================================================
//
//    Wrapper args =
//        lazy("expr", () -> this.expr).sep(0, COMMA).bracketed("()")
//        .push((p,xs) -> this.<Expr>list(xs));
//
//    Wrapper this_parameter_qualifier =
//        seq(iden, DOT).at_least(0)
//        .push((p, xs) -> this.<String>list(xs));
//
//    Wrapper this_param_suffix =
//        seq(this_parameter_qualifier, `this`)
//        .push((p,xs) -> thisParameter($(xs,0), $(xs,1), $(xs,2)));
//
//    Wrapper iden_param_suffix =
//        seq(iden, dims)
//        .push((p,xs) -> new IdenParameter($(xs,0), $(xs,1), $(xs,2), $(xs,3)));
//
//    Wrapper variadic_param_suffix =
//        seq(annotations, ELLIPSIS, iden)
//        .push((p, xs) -> new VariadicParameter($(xs,0), $(xs,1), $(xs,2), $(xs,3)));
//
//    Wrapper formal_param_suffix =
//        choice(iden_param_suffix, this_param_suffix, variadic_param_suffix);
//
//    Wrapper formal_param =
//        seq(modifiers, type, formal_param_suffix);
//
//    Wrapper formal_params =
//        formal_param.sep(0, COMMA).bracketed("()")
//        .push((p,xs) -> new FormalParameters(it.list()));
//
//    Wrapper untyped_params =
//        iden.sep(1, COMMA).bracketed("()")
//        .push((p,xs) -> new UntypedParameters(it.list()));
//
//    Wrapper single_param =
//        iden
//        .push((p,xs) -> new UntypedParameters(this.<String>list(xs)));
//
//    Wrapper lambda_params =
//        choice(formal_params, untyped_params, single_param);
//
//    /// NON-TYPE DECLARATIONS ======================================================================
//
//    Wrapper var_init =
//        choice(lazy("expr", () -> this.expr), lazy("array_init", () -> this.array_init));
//
//    Wrapper array_init =
//        var_init.sep_trailing(0, COMMA).bracketed("{}")
//        .push((p,xs) -> new ArrayInit(it.list()));
//
//    Wrapper var_declarator_id =
//        seq(iden, dims)
//        .push((p,xs) -> new VarDeclaratorID($(xs,0), $(xs,1)));
//
//    Wrapper var_declarator =
//        seq(var_declarator_id, seq(EQ, var_init).maybe())
//        .push((p,xs) -> new VarDeclarator($(xs,0), $(xs,1)));
//
//    Wrapper var_decl_no_semi =
//        seq(type, var_declarator.sep(1, COMMA))
//        .push((p,xs) -> new VarDecl($(xs,0), $(xs,1), it.list(2)));
//
//    Wrapper var_decl_suffix =
//        seq(var_decl_no_semi, SEMI);
//
//    Wrapper var_decl =
//        seq(modifiers, var_decl_suffix);
//
//    Wrapper throws_clause =
//        seq(throws, type.sep(1, COMMA)).opt()
//        .push((p,xs) -> this.<Type>list(xs));
//
//    Wrapper block_or_semi =
//        choice(lazy("block", () -> this.block), SEMI.as_val(null));
//
//    Wrapper method_decl_suffix =
//        seq(type_params, type, iden, formal_params, dims, throws_clause, block_or_semi)
//        .push((p,xs) -> new MethodDecl($(xs,0), $(xs,1), $(xs,2), $(xs,3), $(xs,4), $(xs,5), $(xs,6), $(xs,7)));
//
//    Wrapper constructor_decl_suffix =
//        seq(type_params, iden, formal_params, throws_clause, lazy("block", () -> this.block))
//        .push((p,xs) -> new ConstructorDecl($(xs,0), $(xs,1), $(xs,2), $(xs,3), $(xs,4), $(xs,5)));
//
//    Wrapper init_block =
//        seq(static.as_bool(), lazy("block", () -> this.block))
//        .push((p,xs) -> new InitBlock($(xs,0), $(xs,1)));
//
//    /// TYPE DECLARATIONS ==========================================================================
//
//    // Common -----------------------------------------------------------------
//
//    Wrapper extends_clause =
//        seq(extends, type.sep(0, COMMA)).opt()
//        .push((p,xs) -> this.<Type>list(xs));
//
//    Wrapper implements_clause =
//        seq(implements, type.sep(0, COMMA)).opt()
//        .push((p,xs) -> this.<Type>list(xs));
//
//    Wrapper type_sig =
//        seq(iden, type_params, extends_clause, implements_clause);
//
//    Wrapper class_modified_decl =
//        seq(modifiers, choice(var_decl_suffix, method_decl_suffix, constructor_decl_suffix, lazy("type_decl_suffix", () -> this.type_decl_suffix)));
//
//    Wrapper class_body_decl =
//        choice(class_modified_decl, init_block, SEMI);
//
//    Wrapper class_body_decls =
//        class_body_decl.at_least(0)
//        .push((p,xs) -> this.<Decl>list(xs));
//
//    Wrapper type_body =
//        class_body_decls.bracketed("{}");
//
//    // Enum -------------------------------------------------------------------
//
//    Wrapper enum_constant =
//        seq(annotations, iden, args.maybe(), type_body.maybe())
//        .push((p,xs) -> new EnumConstant($(xs,0), $(xs,1), $(xs,2), $(xs,3)));
//
//    Wrapper enum_class_decls =
//        seq(SEMI, class_body_decl.at_least(0)).opt()
//        .push((p, xs) -> this.<Decl>list(xs));
//
//    Wrapper enum_constants =
//        enum_constant.sep(1, COMMA).opt()
//        .push((p,xs) -> this.<EnumConstant>list(xs));
//
//    Wrapper enum_body =
//        seq(enum_constants, enum_class_decls).bracketed("{}").collect((p,xs) -> stack
//        .push($(xs,1)) ; stack
//        .push($(xs,0)) /* swap */);
//
//    Wrapper enum_decl =
//        seq(enum, type_sig, enum_body)
//        .push((p,xs) -> new val td = TypeDecl(input, ENUM, $(xs,0), $(xs,1), $(xs,2), $(xs,3), $(xs,4), $(xs,5))
//    EnumDecl(td, $(xs,6)));
//
//    // Annotations ------------------------------------------------------------
//
//    Wrapper annot_default_clause =
//        seq(default, annotation_element)
//        .push((p,xs) -> {$(xs,1));
//
//    Wrapper annot_elem_decl =
//        seq(modifiers, type, iden, seq(LPAREN, RPAREN), dims, annot_default_clause.maybe(), SEMI)
//        .push((p, xs) -> new AnnotationElemDecl($(xs,0), $(xs,1), $(xs,2), $(xs,3), $(xs,4)));
//
//    Wrapper annot_body_decls =
//        choice(annot_elem_decl, class_body_decl).at_least(0)
//        .push((p,xs) -> this.<Decl>list(xs));
//
//    Wrapper annotation_decl =
//        seq(MONKEYS_AT, `interface`, type_sig, annot_body_decls.bracketed("{}"))
//        .push((p,xs) -> new TypeDecl(input, ANNOTATION, $(xs,0), $(xs,1), $(xs,2), $(xs,3), $(xs,4), $(xs,5)));
//
//    //// ------------------------------------------------------------------------
//
//    Wrapper class_decl =
//        seq(`class`, type_sig, type_body)
//        .push((p,xs) -> new TypeDecl(input, CLASS, $(xs,0), $(xs,1), $(xs,2), $(xs,3), $(xs,4), $(xs,5)));
//
//    Wrapper interface_declaration =
//        seq(`interface`, type_sig, type_body)
//        .push((p,xs) -> new TypeDecl(input, INTERFACE, $(xs,0), $(xs,1), $(xs,2), $(xs,3), $(xs,4), $(xs,5)));
//
//    Wrapper type_decl_suffix =
//        choice(class_decl, interface_declaration, enum_decl, annotation_decl);
//
//    Wrapper type_decl =
//        seq(modifiers, type_decl_suffix);
//
//    Wrapper type_decls =
//        choice(type_decl, SEMI).at_least(0)
//        .push((p, xs) -> this.<Decl>list(xs));
//
//    /// EXPRESSIONS ================================================================================
//
//    // Array Constructor ------------------------------------------------------
//
//    Wrapper dim_expr =
//        seq(annotations, lazy("expr", () -> this.expr).bracketed("[]"))
//        .push((p,xs) -> new DimExpr($(xs,0), $(xs,1)));
//
//    Wrapper dim_exprs =
//        dim_expr.at_least(1)
//        .push((p,xs) -> this.<DimExpr>list(xs));
//
//    Wrapper dim_expr_array_creator =
//        seq(stem_type, dim_exprs, dims)
//        .push((p,xs) -> new ArrayCtorCall($(xs,0), $(xs,1), $(xs,2), null));
//
//    Wrapper init_array_creator =
//        seq(stem_type, dims1, array_init)
//        .push((p,xs) -> new ArrayCtorCall($(xs,0), emptyList(), $(xs,1), $(xs,2)));
//
//    Wrapper array_ctor_call =
//        seq(new, choice(dim_expr_array_creator, init_array_creator));
//
//    // Lambda Expression ------------------------------------------------------
//
//    Wrapper lambda =
//        seq(lambda_params, ARROW, choice(lazy("block", () -> this.block), lazy("expr", () -> this.expr)))
//        .push((p, xs) -> new Lambda($(xs,0), $(xs,1)));
//
//    // Expression - Primary ---------------------------------------------------
//
//    Wrapper par_expr =
//        lazy("expr", () -> this.expr).bracketed("()")
//        .push((p,xs) -> new ParenExpr($(xs,0)));
//
//    Wrapper ctor_call =
//        seq(new, type_args, stem_type, args, type_body.maybe())
//        .push((p,xs) -> new CtorCall($(xs,0), $(xs,1), $(xs,2), $(xs,3)));
//
//    Wrapper new_ref_suffix =
//        new
//        .push((p,xs) -> new NewReference($(xs,0), $(xs,1)));
//
//    Wrapper method_ref_suffix =
//        iden
//        .push((p,xs) -> new MaybeBoundMethodReference($(xs,0), $(xs,1), $(xs,2)));
//
//    Wrapper ref_suffix =
//        seq(COLCOL, type_args, choice(new_ref_suffix, method_ref_suffix));
//
//    Wrapper class_expr_suffix =
//        seq(DOT, `class`)
//        .push((p, xs) -> new ClassExpr($(xs,0)));
//
//    Wrapper type_suffix_expr =
//        seq(type, choice(ref_suffix, class_expr_suffix));
//
//    Wrapper iden_or_method_expr =
//        seq(iden, args.maybe())
//        .push((p,xs) -> new it[1] ?. let { MethodCall(null, listOf(), $(xs,0), $(xs,1)) } ?: Identifier($(xs,0)));
//
//    Wrapper this_expr =
//        seq(`this`, args.maybe())
//        .push((p,xs) -> new it[0] ?. let { ThisCall($(xs,0)) } ?: This);
//
//    Wrapper super_expr =
//        seq(`super`, args.maybe())
//        .push((p,xs) -> new it[0] ?. let { SuperCall($(xs,0)) } ?: Super);
//
//    Wrapper class_expr =
//        seq(type, DOT, `class`)
//        .push((p, xs) -> new ClassExpr($(xs,0)));
//
//    Wrapper primary_expr =
//        choice(par_expr, array_ctor_call, ctor_call, type_suffix_expr, iden_or_method_expr, this_expr, super_expr, literal);
//
//    // Expression - Postfix ---------------------------------------------------
//
//    Wrapper dot_this =
//        `this`
//        .push((p,xs) -> new DotThis($(xs,0)));
//
//    Wrapper dot_super =
//        `super`
//        .push((p,xs) -> new DotSuper($(xs,0)));
//
//    Wrapper dot_iden =
//        iden
//        .push((p,xs) -> new DotIden($(xs,0), $(xs,1)));
//
//    Wrapper dot_new =
//        ctor_call
//        .push((p,xs) -> new DotNew($(xs,0), $(xs,1)));
//
//    Wrapper dot_method =
//        seq(type_args, iden, args)
//        .push((p,xs) -> new MethodCall($(xs,0), $(xs,1), $(xs,2), $(xs,3)));
//
//    Wrapper dot_postfix =
//        choice(dot_method, dot_iden, dot_this, dot_super, dot_new);
//
//    Wrapper ref_postfix =
//        seq(COLCOL, type_args, iden)
//        .push((p, xs) -> new BoundMethodReference($(xs,0), $(xs,1), $(xs,2)));
//
//    Wrapper array_postfix =
//        lazy("expr", () -> this.expr).bracketed("[]")
//        .push((p,xs) -> new ArrayAccess($(xs,0), $(xs,1)));
//
//    Wrapper inc_suffix =
//        PLUSPLUS
//        .push((p,xs) -> new PostIncrement($(xs,0)));
//
//    Wrapper dec_suffix =
//        SUBSUB
//        .push((p,xs) -> new PostDecrement($(xs,0)));
//
//    Wrapper postfix =
//        choice(seq(DOT, dot_postfix), array_postfix, inc_suffix, dec_suffix, ref_postfix);
//
//    Wrapper postfix_expr =
//        seq(primary_expr, postfix.at_least(0));
//
//    Wrapper inc_prefix =
//        seq(PLUSPLUS, lazy("prefix_expr", () -> this.prefix_expr))
//        .push((p,xs) -> new PreIncrement($(xs,0)));
//
//    Wrapper dec_prefix =
//        seq(SUBSUB, lazy("prefix_expr", () -> this.prefix_expr))
//        .push((p,xs) -> new PreDecrement($(xs,0)));
//
//    Wrapper unary_plus =
//        seq(PLUS, lazy("prefix_expr", () -> this.prefix_expr))
//        .push((p,xs) -> new UnaryPlus($(xs,0)));
//
//    Wrapper unary_minus =
//        seq(SUB, lazy("prefix_expr", () -> this.prefix_expr))
//        .push((p,xs) -> new UnaryMinus($(xs,0)));
//
//    Wrapper complement =
//        seq(TILDE, lazy("prefix_expr", () -> this.prefix_expr))
//        .push((p,xs) -> new Complement($(xs,0)));
//
//    Wrapper not =
//        seq(BANG, lazy("prefix_expr", () -> this.prefix_expr))
//        .push((p,xs) -> new Negate($(xs,0)));
//
//    Wrapper cast =
//        seq(type_union.bracketed("()"), choice(lambda, lazy("prefix_expr", () -> this.prefix_expr)))
//        .push((p,xs) -> new Cast($(xs,0), $(xs,1)));
//
//    Wrapper prefix_expr =
//        choice(inc_prefix, dec_prefix, unary_plus, unary_minus, complement, not, cast, postfix_expr);
//
//    // Expression - Binary ----------------------------------------------------
//
//    Wrapper mult_expr =
//        AssocLeft(this) {
//    operands = prefix_expr
//    op(STAR, { Product($(xs,0), $(xs,1)) })
//    op(DIV, { Division($(xs,0), $(xs,1)) })
//    op(PERCENT, { Remainder($(xs,0), $(xs,1)) })
//);
//
//    Wrapper add_expr =
//        AssocLeft(this) {
//    operands = mult_expr
//    op(PLUS, { Sum($(xs,0), $(xs,1)) })
//    op(SUB, { Diff($(xs,0), $(xs,1)) })
//);
//
//    Wrapper shift_expr =
//        AssocLeft(this) {
//    operands = add_expr
//    op(LTLT, { ShiftLeft($(xs,0), $(xs,1)) })
//    op(GTGT, { ShiftRight($(xs,0), $(xs,1)) })
//    op(GTGTGT, { BinaryShiftRight($(xs,0), $(xs,1)) })
//);
//
//    Wrapper order_expr =
//        AssocLeft(this) {
//    operands = shift_expr
//    op(LT, { Lower($(xs,0), $(xs,1)) })
//    op(LTEQ, { LowerEqual($(xs,0), $(xs,1)) })
//    op(GT, { Greater($(xs,0), $(xs,1)) })
//    op(GTEQ, { GreaterEqual($(xs,0), $(xs,1)) })
//    postfix(seq(instanceof, type), { Instanceof($(xs,0), $(xs,1)) })
//);
//
//    Wrapper eq_expr =
//        AssocLeft(this) {
//    operands = order_expr
//    op(EQEQ, { Equal($(xs,0), $(xs,1)) })
//    op(BANGEQ, { NotEqual($(xs,0), $(xs,1)) })
//);
//
//    Wrapper binary_and_expr =
//        AssocLeft(this) {
//    operands = eq_expr
//    op(AMP, { BinaryAnd($(xs,0), $(xs,1)) })
//);
//
//    Wrapper xor_expr =
//        AssocLeft(this) {
//    operands = binary_and_expr
//    op(CARET, { Xor($(xs,0), $(xs,1)) })
//);
//
//    Wrapper binary_or_expr =
//        AssocLeft(this) {
//    operands = xor_expr
//    op(BAR, { BinaryOr($(xs,0), $(xs,1)) })
//);
//
//    Wrapper and_expr =
//        AssocLeft(this) {
//    operands = binary_or_expr
//    op(AMPAMP, { And($(xs,0), $(xs,1)) })
//);
//
//    Wrapper or_expr =
//        AssocLeft(this) {
//    operands = and_expr
//    op(BARBAR, { Or($(xs,0), $(xs,1)) })
//);
//
//    Wrapper ternary_suffix =
//        seq(QUES, lazy("expr", () -> this.expr), COL, lazy("expr", () -> this.expr))
//        .push((p, xs) -> new Ternary($(xs,0), $(xs,1), $(xs,2)));
//
//    Wrapper ternary =
//        seq(or_expr, ternary_suffix.opt());
//
//    Wrapper assignment_suffix =
//        choice(seq(EQ, lazy("expr", () -> this.expr))
//        .push((p,xs) -> new Assign($(xs,0), $(xs,1), "=")}, seq(PLUSEQ, lazy("expr", () -> this.expr))
//        .push((p,xs) -> new Assign($(xs,0), $(xs,1), "+=")}, seq(SUBEQ, lazy("expr", () -> this.expr))
//        .push((p,xs) -> new Assign($(xs,0), $(xs,1), "-=")}, seq(STAREQ, lazy("expr", () -> this.expr))
//        .push((p,xs) -> new Assign($(xs,0), $(xs,1), "*=")}, seq(DIVEQ, lazy("expr", () -> this.expr))
//        .push((p, xs) -> new Assign($(xs,0), $(xs,1), "/=")}, seq(PERCENTEQ, lazy("expr", () -> this.expr))
//        .push((p, xs) -> new Assign($(xs,0), $(xs,1), "%=")}, seq(LTLTEQ, lazy("expr", () -> this.expr))
//        .push((p, xs) -> new Assign($(xs,0), $(xs,1), "<<=")}, seq(GTGTEQ, lazy("expr", () -> this.expr))
//        .push((p, xs) -> new Assign($(xs,0), $(xs,1), ">>=")}, seq(GTGTGTEQ, lazy("expr", () -> this.expr))
//        .push((p, xs) -> new Assign($(xs,0), $(xs,1), ">>>=")}, seq(AMPEQ, lazy("expr", () -> this.expr))
//        .push((p, xs) -> new Assign($(xs,0), $(xs,1), "&=")}, seq(CARETEQ, lazy("expr", () -> this.expr))
//        .push((p, xs) -> new Assign($(xs,0), $(xs,1), "^=")}, seq(BAREQ, lazy("expr", () -> this.expr))
//        .push((p, xs) -> new Assign($(xs,0), $(xs,1), "|=")});
//
//    Wrapper assignment =
//        seq(ternary, assignment_suffix.opt());
//
//    Wrapper expr =
//        choice(lambda, assignment);
//
//    /// STATEMENTS =================================================================================
//
//    Wrapper if_stmt =
//        seq(`if`, par_expr, lazy("stmt", () -> this.stmt), seq(`else`, lazy("stmt", () -> this.stmt)).maybe())
//        .push((p,xs) -> new If($(xs,0), $(xs,1), $(xs,2)));
//
//    Wrapper expr_stmt_list =
//        expr.sep(0, COMMA)
//        .push((p,xs) -> this.<Stmt>list(xs));
//
//    Wrapper for_init_decl =
//        seq(modifiers, var_decl_no_semi)
//        .push((p,xs) -> this.<Stmt>list(xs));
//
//    Wrapper for_init =
//        choice(for_init_decl, expr_stmt_list);
//
//    Wrapper basic_for_paren_part =
//        seq(for_init, SEMI, expr.maybe(), SEMI, expr_stmt_list.opt());
//
//    Wrapper basic_for_stmt =
//        seq(`for`, basic_for_paren_part.bracketed("()"), lazy("stmt", () -> this.stmt))
//        .push((p,xs) -> new BasicFor($(xs,0), $(xs,1), $(xs,2), $(xs,3)));
//
//    Wrapper for_val_decl =
//        seq(modifiers, type, var_declarator_id, COL, expr);
//
//    Wrapper enhanced_for_stmt =
//        seq(`for`, for_val_decl.bracketed("()"), lazy("stmt", () -> this.stmt))
//        .push((p,xs) -> new EnhancedFor($(xs,0), $(xs,1), $(xs,2), $(xs,3), $(xs,4)));
//
//    Wrapper while_stmt =
//        seq(`while`, par_expr, lazy("stmt", () -> this.stmt))
//        .push((p,xs) -> new WhileStmt($(xs,0), $(xs,1)));
//
//    Wrapper do_while_stmt =
//        seq(`do`, lazy("stmt", () -> this.stmt), `while`, par_expr, SEMI)
//        .push((p, xs) -> new DoWhileStmt($(xs,0), $(xs,1)));
//
//    Wrapper catch_parameter_types =
//        type.sep(0, BAR)
//        .push((p,xs) -> this.<Type>list(xs));
//
//    Wrapper catch_parameter =
//        seq(modifiers, catch_parameter_types, var_declarator_id);
//
//    Wrapper catch_clause =
//        seq(catch, catch_parameter.bracketed("()"), lazy("block", () -> this.block))
//        .push((p,xs) -> new CatchClause($(xs,0), $(xs,1), $(xs,2), $(xs,3)));
//
//    Wrapper catch_clauses =
//        catch_clause.at_least(0)
//        .push((p,xs) -> this.<CatchClause>list(xs));
//
//    Wrapper finally_clause =
//        seq(finally, lazy("block", () -> this.block));
//
//    Wrapper resource =
//        seq(modifiers, type, var_declarator_id, EQ, expr)
//        .push((p,xs) -> new TryResource($(xs,0), $(xs,1), $(xs,2), $(xs,3)));
//
//    Wrapper resources =
//        resource.sep(1, SEMI).bracketed("()").opt()
//        .push((p, xs) -> this.<TryResource>list(xs));
//
//    Wrapper try_stmt =
//        seq(`try`, resources, lazy("block", () -> this.block), catch_clauses, finally_clause.maybe())
//        .push((p,xs) -> new TryStmt($(xs,0), $(xs,1), $(xs,2), $(xs,3)));
//
//    Wrapper default_label =
//        seq(default, COL)
//        .push((p, xs) -> new DefaultLabel);
//
//    Wrapper case_label =
//        seq(case, expr, COL)
//        .push((p, xs) -> new CaseLabel($(xs,0)));
//
//    Wrapper switch_label =
//        choice(case_label, default_label);
//
//    Wrapper switch_clause =
//        seq(switch_label, lazy("stmts", () -> this.stmts))
//        .push((p,xs) -> new SwitchClause($(xs,0), $(xs,1)));
//
//    Wrapper switch_stmt =
//        seq(switch, par_expr, switch_clause.at_least(0).bracketed("{}"))
//        .push((p,xs) -> new SwitchStmt($(xs,0), it.list(1)));
//
//    Wrapper synchronized_stmt =
//        seq(synchronized, par_expr, lazy("block", () -> this.block))
//        .push((p,xs) -> new SynchronizedStmt($(xs,1), $(xs,2)));
//
//    Wrapper return_stmt =
//        seq(`return`, expr.maybe(), SEMI)
//        .push((p, xs) -> new ReturnStmt($(xs,0)));
//
//    Wrapper throw_stmt =
//        seq(`throw`, expr, SEMI)
//        .push((p, xs) -> new ThrowStmt($(xs,0)));
//
//    Wrapper break_stmt =
//        seq(`break`, iden.maybe(), SEMI)
//        .push((p, xs) -> new BreakStmt($(xs,0)));
//
//    Wrapper continue_stmt =
//        seq(`continue`, iden.maybe(), SEMI)
//        .push((p, xs) -> new ContinueStmt($(xs,0)));
//
//    Wrapper assert_stmt =
//        seq(assert, expr, seq(COL, expr).maybe(), semi)
//        .push((p, xs) -> new AssertStmt($(xs,0), $(xs,1)));
//
//    Wrapper semi_stmt =
//        SEMI
//        .push((p, xs) -> new SemiStmt);
//
//    Wrapper expr_stmt =
//        seq(expr, SEMI);
//
//    Wrapper labelled_stmt =
//        seq(iden, COL, lazy("stmt", () -> this.stmt))
//        .push((p, xs) -> new LabelledStmt($(xs,0), $(xs,1)));
//
//    Wrapper stmt =
//        choice(lazy("block", () -> this.block), if_stmt, basic_for_stmt, enhanced_for_stmt, while_stmt, do_while_stmt, try_stmt, switch_stmt, synchronized_stmt, return_stmt, throw_stmt, break_stmt, continue_stmt, assert_stmt, semi_stmt, expr_stmt, labelled_stmt, var_decl, type_decl);
//
//    Wrapper block =
//        stmt.at_least(0).bracketed("{}")
//        .push((p,xs) -> new Block(it.list()));
//
//    Wrapper stmts =
//        stmt.at_least(0)
//        .push((p,xs) -> this.<Stmt>list(xs));
//
//    /// TOP-LEVEL ==================================================================================
//
//    Wrapper package_decl =
//        seq(annotations, `package`, qualified_iden, SEMI)
//        .push((p, xs) -> new Package($(xs,0), $(xs,1)));
//
//    Wrapper import_decl =
//        seq(import, static.as_bool(), qualified_iden, seq(DOT, STAR).as_bool(), semi)
//        .push((p, xs) -> new Import($(xs,0), $(xs,1), $(xs,2)));
//
//    Wrapper import_decls =
//        import_decl.at_least(0)
//        .push((p,xs) -> this.<Import>list(xs));
//
//    Wrapper root =
//        seq(lazy("whitespace", () -> this.whitespace), package_decl.maybe(), import_decls, type_decls)
//        .push((p,xs) -> new File(input, $(xs,0), $(xs,1), $(xs,2)));
}

