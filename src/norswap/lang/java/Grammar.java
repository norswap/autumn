package norswap.lang.java;

import norswap.autumn.DSL;
import norswap.autumn.Parse;
import norswap.lang.java.ast.*;
import norswap.utils.Pair;

import java.util.function.BiConsumer;

import static java.util.Collections.emptyList;
import static norswap.lang.java.LexUtils.*;
import static norswap.lang.java.ast.BinaryOperator.*;
import static norswap.lang.java.ast.UnaryOperator.*;

public final class Grammar extends DSL
{
    /// LEXICAL ====================================================================================

    // Whitespace ----------------------------------------------------------------------------------

    public rule space_char          = cpred(Character::isWhitespace);
    public rule not_line            = seq(str("\n").not(), any);
    public rule line_comment        = seq("//", not_line.at_least(0), str("\n").opt());

    public rule not_comment_term    = seq(str("*/").not(), any);
    public rule multi_comment       = seq("/*", not_comment_term.at_least(0), "*/");

    {
        ws = choice(space_char, line_comment, multi_comment).at_least(0).get();
        ws.exclude_error = true;
    }

    // Keywords and Operators ----------------------------------------------------------------------

    public rule _boolean        = word("boolean")      .token();
    public rule _byte           = word("byte")         .token();
    public rule _char           = word("char")         .token();
    public rule _double         = word("double")       .token();
    public rule _float          = word("float")        .token();
    public rule _int            = word("int")          .token();
    public rule _long           = word("long")         .token();
    public rule _short          = word("short")        .token();
    public rule _void           = word("void")         .token();
    public rule _abstract       = word("abstract")     .token();
    public rule _default        = word("default")      .token();
    public rule _final          = word("final")        .token();
    public rule _native         = word("native")       .token();
    public rule _private        = word("private")      .token();
    public rule _protected      = word("protected")    .token();
    public rule _public         = word("public")       .token();
    public rule _static         = word("static")       .token();
    public rule _strictfp       = word("strictfp")     .token();
    public rule _synchronized   = word("synchronized") .token();
    public rule _transient      = word("transient")    .token();
    public rule _volatile       = word("volatile")     .token();
    public rule _assert         = word("assert")       .token();
    public rule _break          = word("break")        .token();
    public rule _case           = word("case")         .token();
    public rule _catch          = word("catch")        .token();
    public rule _class          = word("class")        .token();
    public rule _const          = word("const")        .token();
    public rule _continue       = word("continue")     .token();
    public rule _do             = word("do")           .token();
    public rule _else           = word("else")         .token();
    public rule _enum           = word("enum")         .token();
    public rule _extends        = word("extends")      .token();
    public rule _finally        = word("finally")      .token();
    public rule _for            = word("for")          .token();
    public rule _goto           = word("goto")         .token();
    public rule _if             = word("if")           .token();
    public rule _implements     = word("implements")   .token();
    public rule _import         = word("import")       .token();
    public rule _interface      = word("interface")    .token();
    public rule _instanceof     = word("instanceof")   .token();
    public rule _new            = word("new")          .token();
    public rule _package        = word("package")      .token();
    public rule _return         = word("return")       .token();
    public rule _super          = word("super")        .token();
    public rule _switch         = word("switch")       .token();
    public rule _this           = word("this")         .token();
    public rule _throws         = word("throws")       .token();
    public rule _throw          = word("throw")        .token();
    public rule _try            = word("try")          .token();
    public rule _while          = word("while")        .token();

    // ordering matters when there are shared prefixes!

    public rule BANG            = word("!")            .token();
    public rule BANGEQ          = word("!=")           .token();
    public rule PERCENT         = word("%")            .token();
    public rule PERCENTEQ       = word("%=")           .token();
    public rule AMP             = word("&")            .token();
    public rule AMPAMP          = word("&&")           .token();
    public rule AMPEQ           = word("&=")           .token();
    public rule LPAREN          = word("(")            .token();
    public rule RPAREN          = word(")")            .token();
    public rule STAR            = word("*")            .token();
    public rule STAREQ          = word("*=")           .token();
    public rule PLUS            = word("+")            .token();
    public rule PLUSPLUS        = word("++")           .token();
    public rule PLUSEQ          = word("+=")           .token();
    public rule COMMA           = word(",")            .token();
    public rule MINUS           = word("-")            .token();
    public rule MINUSMINUS      = word("--")           .token();
    public rule SUBEQ           = word("-=")           .token();
    public rule EQ              = word("=")            .token();
    public rule EQEQ            = word("==")           .token();
    public rule QUES            = word("?")            .token();
    public rule CARET           = word("^")            .token();
    public rule CARETEQ         = word("^=")           .token();
    public rule LBRACE          = word("{")            .token();
    public rule RBRACE          = word("}")            .token();
    public rule BAR             = word("|")            .token();
    public rule BARBAR          = word("||")           .token();
    public rule BAREQ           = word("|=")           .token();
    public rule TILDE           = word("~")            .token();
    public rule MONKEYS_AT      = word("@")            .token();
    public rule DIV             = word("/")            .token();
    public rule DIVEQ           = word("/=")           .token();
    public rule GTEQ            = word(">=")           .token();
    public rule LTEQ            = word("<=")           .token();
    public rule LTLTEQ          = word("<<=")          .token();
    public rule LTLT            = word("<<")           .token();
    public rule GTGTEQ          = word(">>=")          .token();
    public rule GTGTGTEQ        = word(">>>=")         .token();
    public rule GT              = word(">")            .token();
    public rule LT              = word("<")            .token();
    public rule LBRACKET        = word("[")            .token();
    public rule RBRACKET        = word("]")            .token();
    public rule ARROW           = word("->")           .token();
    public rule COL             = word(":")            .token();
    public rule COLCOL          = word("::")           .token();
    public rule SEMI            = word(";")            .token();
    public rule DOT             = word(".")            .token();
    public rule ELLIPSIS        = word("...")          .token();

    // These two are not tokens, because they would cause issue with nested generic types.
    // e.g. in List<List<String>>, you want ">>" to lex as [_GT, _GT]

    public rule GTGT            = word(">>");
    public rule GTGTGT          = word(">>>");

    public rule _false          = word("false")          .as_val(false).token();
    public rule _true           = word("true")           .as_val(true).token();
    public rule _null           = word("null")           .as_val(Null.NULL).token();

    // Identifiers ---------------------------------------------------------------------------------

    public rule id_start    = cpred(Character::isJavaIdentifierStart);
    public rule id_part     = cpred(c -> c != 0 && Character.isJavaIdentifierPart(c));
    public rule iden = seq(id_start, id_part.at_least(0))
        .collect_str((p,str,xs) -> p.push(Identifier.mk(str)))
        .word()
        .token();

    // Numerals - Common Parts ---------------------------------------------------------------------

    public rule underscore  = str("_");
    public rule dlit        = str(".");
    public rule hex_prefix  = choice("0x", "0X");
    public rule underscores = underscore.at_least(0);
    public rule digits1     = digit.sep(1, underscores);
    public rule digits0     = digit.sep(0, underscores);
    public rule hex_digits  = hex_digit.sep(1, underscores);
    public rule hex_num     = seq(hex_prefix, hex_digits);

    // Numerals - Floating Point -------------------------------------------------------------------

    public rule hex_significand = choice(
        seq(hex_prefix, hex_digits.opt(), dlit, hex_digits),
        seq(hex_num, dlit.opt()));

    public rule exp_sign_opt        = set("+-").opt();
    public rule exponent            = seq(set("eE"), exp_sign_opt, digits1);
    public rule binary_exponent     = seq(set("pP"), exp_sign_opt, digits1);
    public rule float_suffix        = set("fFdD");
    public rule float_suffix_opt    = float_suffix.opt();
    public rule hex_float_lit       = seq(hex_significand, binary_exponent, float_suffix_opt);

    public rule decimal_float_lit = choice(
        seq(digits1, dlit, digits0, exponent.opt(), float_suffix_opt),
        seq(dlit, digits1, exponent.opt(), float_suffix_opt),
        seq(digits1, exponent, float_suffix_opt),
        seq(digits1, exponent.opt(), float_suffix));

    public rule float_literal = choice(hex_float_lit, decimal_float_lit)
        .collect_str((p,str,xs) -> p.push(parse_floating(str).unwrap()))
        .token();

    // Numerals - Integral -------------------------------------------------------------------------

    public rule bit             = set("01");
    public rule binary_prefix   = choice("0b", "0B");
    public rule binary_num      = seq(binary_prefix, bit.at_least(1).sep(1, underscores));
    public rule octal_num       = seq("0", seq(underscores, octal_digit).at_least(1));
    public rule decimal_num     = choice("0", digits1);
    public rule integer_num     = choice(hex_num, binary_num, octal_num, decimal_num);

    public rule integer_literal = seq(integer_num, set("lL").opt())
        .collect_str((p,str,xs) -> p.push(parse_integer(str).unwrap()))
        .token();

    // Characters and Strings ----------------------------------------------------------------------

    public rule octal_code_3    = seq(range('0', '3'), octal_digit, octal_digit);
    public rule octal_code_2    = seq(octal_digit, octal_digit.opt());
    public rule octal_code      = choice(octal_code_3, octal_code_2);
    public rule unicode_code    = seq(str("u").at_least(1), hex_digit.repeat(4));
    public rule escape_suffix   = choice(set("btnfr\"'\\"), octal_code, unicode_code);
    public rule escape          = seq("\\", escape_suffix);
    public rule naked_char      = choice(escape, seq(set("'\\\n\r").not(), any));
    public rule nake_str_char   = choice(escape, seq(set("\"\\\n\r").not(), any));

    public rule char_literal = seq("'", naked_char, "'")
        .collect_str((p,str,xs) -> p.push(parse_char(str).unwrap()))
        .token();

    public rule string_literal = seq("\"", nake_str_char.at_least(0), "\"")
        .collect_str((p,str,xs) -> p.push(parse_string(str).unwrap()))
        .token();

    // Literal ----------------------------------------------------------------

    public rule literal = seq(token_choice(
            integer_literal, string_literal, _null, float_literal, _true, _false, char_literal), ws)
        .push((p,xs) -> Literal.mk(xs[0]));

    // ---------------------------------------------------------------------------------------------
    {
        build_tokenizer();
    }
    // ---------------------------------------------------------------------------------------------

    /// ANNOTATIONS ================================================================================

    public rule annotation_element = choice(
        lazy(() -> this.ternary),
        lazy(() -> this.annotation_element_list),
        lazy(() -> this.annotation));

    public rule annotation_inner_list
        = lazy(() -> this.annotation_element).sep_trailing(0, COMMA);

    public rule annotation_element_list
        = seq(LBRACE, annotation_inner_list, RBRACE)
        .push((p,xs) -> AnnotationElementList.mk(list(xs)));

    public rule annotation_element_pair
        = seq(iden, EQ, annotation_element)
        .push((p,xs) -> new Pair<String, AnnotationElement>($(xs,0), $(xs,1)));

    public rule normal_annotation_suffix
        = seq(LPAREN, annotation_element_pair.sep(1, COMMA), RPAREN)
        .push((p,xs) -> NormalAnnotation.mk($(p.pop()), list(xs)));

    public rule single_element_annotation_suffix
        = seq(LPAREN, annotation_element, RPAREN)
        .lookback(1).push((p,xs) -> SingleElementAnnotation.mk($(xs,0), $(xs,1)));

    public rule marker_annotation_suffix
        = seq(LPAREN, RPAREN).opt()
         .lookback(1).push((p,xs) -> MarkerAnnotation.mk($(xs,0)));

    public rule annotation_suffix = choice(
        normal_annotation_suffix,
        single_element_annotation_suffix,
        marker_annotation_suffix);

    public rule qualified_iden
        = iden.sep(1, DOT)
        .push((p, xs) -> this.<Identifier>list(xs));

    public rule annotation
        = seq(MONKEYS_AT, qualified_iden, annotation_suffix);

    public rule annotations
        = annotation.at_least(0)
        .push((p,xs) -> this.<TAnnotation>list(xs));

    // TODO temp
    public rule dot_iden_temp
        = seq(DOT, iden)
        .lookback(1).push((p,xs) -> DotIden.mk($(xs,0), $(xs,1)));

    // TODO temp
    public rule expr_qualified_iden
        = seq(iden, dot_iden_temp.repeat(0));

    // TODO placeholder
    public rule ternary
        = choice(literal, expr_qualified_iden);

    /// TYPES ======================================================================================

    public rule basic_type
        = token_choice(_byte, _short, _int, _long, _char, _float, _double, _boolean, _void)
        .collect_str((p,str,xs) -> p.push(BasicType.valueOf("_" + trim_trailing_whitespace(str))));

    public rule primitive_type
        = seq(annotations, basic_type)
        .push((p,xs) -> PrimitiveType.mk($(xs,0), $(xs,1)));

    public rule extends_bound
        = seq(_extends, lazy(() -> this.type))
        .push((p,xs) -> ExtendsBound.mk($(xs,0)));

    public rule super_bound
        = seq(_super, lazy(() -> this.type))
        .push((p,xs) -> SuperBound.mk($(xs,0)));

    public rule type_bound
        = choice(extends_bound, super_bound).maybe();

    public rule wildcard
        = seq(annotations, QUES, type_bound)
        .push((p,xs) -> Wildcard.mk($(xs,0), $(xs,1)));

    public rule type_args
        = seq(LT, choice(lazy(() -> this.type), wildcard).sep(0, COMMA), GT).opt()
        .push((p,xs) -> this.<TType>list(xs));

    public rule class_type_part
        = seq(annotations, iden, type_args)
        .push((p,xs) -> ClassTypePart.mk($(xs,0), $(xs,1), $(xs,2)));

    public rule class_type
        = class_type_part.sep(1, DOT)
        .push((p, xs) -> ClassType.mk(list(xs)));

    public rule stem_type
        = choice(primitive_type, class_type);

    public rule dim
        = seq(annotations, seq(LBRACKET, RBRACKET))
        .push((p,xs) -> Dimension.mk($(xs,0)));

    public rule dims
        = dim.at_least(0)
        .push((p,xs) -> this.<Dimension>list(xs));

    public rule dims1
        = dim.at_least(1)
        .push((p,xs) -> this.<Dimension>list(xs));

    public rule type_dim_suffix
        = dims1
        .lookback(1).push((p,xs) -> ArrayType.mk($(xs,0), $(xs,1)));

    public rule type
        = seq(stem_type, type_dim_suffix.opt());

    public rule type_union_syntax
        = lazy(() -> this.type).sep(1, AMP);

    public rule type_union
        = type_union_syntax
        .push((p,xs) -> this.<TType>list(xs));

    public rule type_bounds
        = seq(_extends, type_union_syntax).opt()
        .push((p,xs) -> this.<TType>list(xs));

    public rule type_param
        = seq(annotations, iden, type_bounds)
        .push((p,xs) -> TypeParam.mk($(xs,0), $(xs,1), $(xs,2)));

    public rule type_params
        = seq(LT, type_param.sep(0, COMMA), GT).opt()
        .push((p,xs) -> this.<TypeParam>list(xs));
 
    /// MODIFIERS ==================================================================================

    public rule keyword_modifier =
        token_choice(
            _public, _protected, _private, _abstract, _static, _final, _synchronized,
            _native, _strictfp, _default, _transient, _volatile)
        .collect_str((p,str,xs) -> p.push(Keyword.valueOf("_" + trim_trailing_whitespace(str))));

    public rule modifier =
        choice(annotation, keyword_modifier);

    public rule modifiers =
        modifier.at_least(0)
        .push((p,xs) -> this.<Modifier>list(xs));

    /// PARAMETERS =================================================================================

    public rule args =
        seq(LPAREN, lazy(() -> this.expr).sep(0, COMMA), RPAREN)
        .push((p,xs) -> this.<Expression>list(xs));

    public rule this_parameter_qualifier =
        seq(iden, DOT).at_least(0)
        .push((p, xs) -> this.<String>list(xs));

    public rule this_param_suffix =
        seq(this_parameter_qualifier, _this)
        .push((p,xs) -> ThisParameter.mk($(xs,0), $(xs,1), $(xs,2)));

    public rule iden_param_suffix =
        seq(iden, dims)
        .push((p,xs) -> IdenParameter.mk($(xs,0), $(xs,1), $(xs,2), $(xs,3)));

//    rule variadic_param_suffix =
//        seq(annotations, ELLIPSIS, iden)
//        .push((p, xs) -> new VariadicParameter($(xs,0), $(xs,1), $(xs,2), $(xs,3)));
//
//    rule formal_param_suffix =
//        choice(iden_param_suffix, this_param_suffix, variadic_param_suffix);
//
//    rule formal_param =
//        seq(modifiers, type, formal_param_suffix);
//
//    rule formal_params =
//        formal_param.sep(0, COMMA).bracketed("()")
//        .push((p,xs) -> new FormalParameters(it.list()));
//
//    rule untyped_params =
//        iden.sep(1, COMMA).bracketed("()")
//        .push((p,xs) -> new UntypedParameters(it.list()));
//
//    rule single_param =
//        iden
//        .push((p,xs) -> new UntypedParameters(this.<String>list(xs)));
//
//    rule lambda_params =
//        choice(formal_params, untyped_params, single_param);
//
//    /// NON-TYPE DECLARATIONS ======================================================================

    rule var_init =
        choice(lazy(() -> this.expr), lazy(() -> this.array_init));

    rule array_init =
        seq(LBRACE, var_init.sep_trailing(0, COMMA), RBRACE)
        .push((p,xs) -> ArrayInitializer.mk(list(xs)));

//    rule var_declarator_id =
//        seq(iden, dims)
//        .push((p,xs) -> new VarDeclaratorID($(xs,0), $(xs,1)));
//
//    rule var_declarator =
//        seq(var_declarator_id, seq(EQ, var_init).maybe())
//        .push((p,xs) -> new VarDeclarator($(xs,0), $(xs,1)));
//
//    rule var_decl_no_semi =
//        seq(type, var_declarator.sep(1, COMMA))
//        .push((p,xs) -> new VarDecl($(xs,0), $(xs,1), it.list(2)));
//
//    rule var_decl_suffix =
//        seq(var_decl_no_semi, SEMI);
//
//    rule var_decl =
//        seq(modifiers, var_decl_suffix);
//
//    rule throws_clause =
//        seq(throws, type.sep(1, COMMA)).opt()
//        .push((p,xs) -> this.<Type>list(xs));
//
//    rule block_or_semi =
//        choice(lazy(() -> this.block), SEMI.as_val(null));
//
//    rule method_decl_suffix =
//        seq(type_params, type, iden, formal_params, dims, throws_clause, block_or_semi)
//        .push((p,xs) -> new MethodDecl($(xs,0), $(xs,1), $(xs,2), $(xs,3), $(xs,4), $(xs,5), $(xs,6), $(xs,7)));
//
//    rule constructor_decl_suffix =
//        seq(type_params, iden, formal_params, throws_clause, lazy(() -> this.block))
//        .push((p,xs) -> new ConstructorDecl($(xs,0), $(xs,1), $(xs,2), $(xs,3), $(xs,4), $(xs,5)));
//
//    rule init_block =
//        seq(static.as_bool(), lazy(() -> this.block))
//        .push((p,xs) -> new InitBlock($(xs,0), $(xs,1)));
//
//    /// TYPE DECLARATIONS ==========================================================================
//
//    // Common -----------------------------------------------------------------
//
//    rule extends_clause =
//        seq(extends, type.sep(0, COMMA)).opt()
//        .push((p,xs) -> this.<Type>list(xs));
//
//    rule implements_clause =
//        seq(implements, type.sep(0, COMMA)).opt()
//        .push((p,xs) -> this.<Type>list(xs));
//
//    rule type_sig =
//        seq(iden, type_params, extends_clause, implements_clause);
//
//    rule class_modified_decl =
//        seq(modifiers, choice(var_decl_suffix, method_decl_suffix, constructor_decl_suffix, lazy(() -> this.type_decl_suffix)));
//
//    rule class_body_decl =
//        choice(class_modified_decl, init_block, SEMI);
//
//    rule class_body_decls =
//        class_body_decl.at_least(0)
//        .push((p,xs) -> this.<Decl>list(xs));
//
//    rule type_body =
//        class_body_decls.bracketed("{}");
//
//    // Enum -------------------------------------------------------------------
//
//    rule enum_constant =
//        seq(annotations, iden, args.maybe(), type_body.maybe())
//        .push((p,xs) -> new EnumConstant($(xs,0), $(xs,1), $(xs,2), $(xs,3)));
//
//    rule enum_class_decls =
//        seq(SEMI, class_body_decl.at_least(0)).opt()
//        .push((p, xs) -> this.<Decl>list(xs));
//
//    rule enum_constants =
//        enum_constant.sep(1, COMMA).opt()
//        .push((p,xs) -> this.<EnumConstant>list(xs));
//
        // TODO peek-only
//    rule enum_body =
//        seq(enum_constants, enum_class_decls).bracketed("{}").collect((p,xs) -> stack
//        .push($(xs,1)) ; stack
//        .push($(xs,0)) /* swap */);
//
//    rule enum_decl =
//        seq(enum, type_sig, enum_body)
//        .push((p,xs) -> new val td = TypeDecl(input, ENUM, $(xs,0), $(xs,1), $(xs,2), $(xs,3), $(xs,4), $(xs,5))
//    EnumDecl(td, $(xs,6)));
//
//    // Annotations ------------------------------------------------------------
//
//    rule annot_default_clause =
//        seq(default, annotation_element)
//        .push((p,xs) -> {$(xs,1));
//
//    rule annot_elem_decl =
//        seq(modifiers, type, iden, seq(LPAREN, RPAREN), dims, annot_default_clause.maybe(), SEMI)
//        .push((p, xs) -> new AnnotationElemDecl($(xs,0), $(xs,1), $(xs,2), $(xs,3), $(xs,4)));
//
//    rule annot_body_decls =
//        choice(annot_elem_decl, class_body_decl).at_least(0)
//        .push((p,xs) -> this.<Decl>list(xs));
//
//    rule annotation_decl =
//        seq(MONKEYS_AT, _interface, type_sig, annot_body_decls.bracketed("{}"))
//        .push((p,xs) -> new TypeDecl(input, ANNOTATION, $(xs,0), $(xs,1), $(xs,2), $(xs,3), $(xs,4), $(xs,5)));
//
//    //// ------------------------------------------------------------------------
//
//    rule class_decl =
//        seq(_class, type_sig, type_body)
//        .push((p,xs) -> new TypeDecl(input, CLASS, $(xs,0), $(xs,1), $(xs,2), $(xs,3), $(xs,4), $(xs,5)));
//
//    rule interface_declaration =
//        seq(_interface, type_sig, type_body)
//        .push((p,xs) -> new TypeDecl(input, INTERFACE, $(xs,0), $(xs,1), $(xs,2), $(xs,3), $(xs,4), $(xs,5)));
//
//    rule type_decl_suffix =
//        choice(class_decl, interface_declaration, enum_decl, annotation_decl);
//
//    rule type_decl =
//        seq(modifiers, type_decl_suffix);
//
//    rule type_decls =
//        choice(type_decl, SEMI).at_least(0)
//        .push((p, xs) -> this.<Decl>list(xs));
//
    /// EXPRESSIONS ================================================================================

    // Array Constructor ------------------------------------------------------

    rule dim_expr =
        seq(annotations, LBRACKET, lazy(() -> this.expr), RBRACKET)
        .push((p,xs) -> DimExpression.mk($(xs,0), $(xs,1)));

    rule dim_exprs =
        dim_expr.at_least(1)
        .push((p,xs) -> this.<DimExpression>list(xs));

    rule dim_expr_array_creator =
        seq(stem_type, dim_exprs, dims)
        .push((p,xs) -> ArrayConstructorCall.mk($(xs,0), $(xs,1), $(xs,2), null));

    rule init_array_creator =
        seq(stem_type, dims1, array_init)
        .push((p,xs) -> ArrayConstructorCall.mk($(xs,0), emptyList(), $(xs,1), $(xs,2)));

    rule array_ctor_call =
        seq(_new, choice(dim_expr_array_creator, init_array_creator));

    // Lambda Expression ------------------------------------------------------

//    rule lambda =
//        seq(lambda_params, ARROW, choice(lazy(() -> this.block), lazy(() -> this.expr)))
//        .push((p, xs) -> new Lambda($(xs,0), $(xs,1)));

    // Expression - Primary ---------------------------------------------------

    public rule par_expr =
        seq(LPAREN, lazy(() -> this.expr), RPAREN)
        .push((p,xs) -> ParenExpression.mk($(xs,0)));

    // TODO (type_body undefined yet)
    public rule ctor_call =
        // seq(_new, type_args, stem_type, args, type_body.maybe())
        seq(_new, type_args, stem_type, args, str("").push((p, xs) -> null))
            .push((p,xs) -> ConstructorCall.mk($(xs,0), $(xs,1), $(xs,2), $(xs,3)));

    public rule new_ref_suffix =
        _new
        .lookback(2).push((p,xs) -> NewReference.mk($(xs,0), $(xs,1)));

    public rule method_ref_suffix =
        iden
        .lookback(2).push((p,xs) -> TypeMethodReference.mk($(xs,0), $(xs,1), $(xs,2)));

    public rule ref_suffix =
        seq(COLCOL, type_args, choice(new_ref_suffix, method_ref_suffix));

    public rule class_expr_suffix =
        seq(DOT, _class)
        .lookback(1).push((p, xs) -> ClassExpression.mk($(xs,0)));

    public rule type_suffix_expr =
        seq(type, choice(ref_suffix, class_expr_suffix));

    public rule iden_or_method_expr =
        seq(iden, args.maybe())
        .push((p,xs) -> $(xs,1) == null ? $(xs,0) : MethodCall.mk(null, list(), $(xs,0), $(xs,1)));

    public rule this_expr =
        seq(_this, args.maybe())
        .push((p,xs) -> $(xs,0) == null ? This.mk() : ThisCall.mk($(xs,0)));

    public rule super_expr =
        seq(_super, args.maybe())
        .push((p,xs) -> $(xs,0) == null ? Super.mk() : SuperCall.mk($(xs,0)));

    public rule primary_expr = choice(
        par_expr, array_ctor_call, ctor_call, type_suffix_expr, iden_or_method_expr,
        this_expr, super_expr, literal);

    // Expression - Postfix ---------------------------------------------------

    public rule dot_this =
        _this
        .lookback(1).push((p,xs) -> UnaryExpression.mk(DOT_THIS, $(xs,0)));

    public rule dot_super =
        _super
        .lookback(1).push((p,xs) -> UnaryExpression.mk(DOT_SUPER, $(xs,0)));

    public rule dot_iden =
        iden
        .lookback(1).push((p,xs) -> DotIden.mk($(xs,0), $(xs,1)));

    public rule dot_new =
        ctor_call
        .lookback(1).push((p,xs) -> DotNew.mk($(xs,0), $(xs,1)));

    public rule dot_method =
        seq(type_args, iden, args)
        .lookback(1).push((p,xs) -> MethodCall.mk($(xs,0), $(xs,1), $(xs,2), $(xs,3)));

    public rule dot_postfix =
        choice(dot_method, dot_iden, dot_this, dot_super, dot_new);

    public rule ref_postfix =
        seq(COLCOL, type_args, iden)
        .lookback(1).push((p, xs) -> BoundMethodReference.mk($(xs,0), $(xs,1), $(xs,2)));

    public rule array_postfix =
        seq(LBRACKET, lazy(() -> this.expr), RBRACKET)
        .lookback(1).push((p,xs) -> ArrayAccess.mk($(xs,0), $(xs,1)));

    public rule inc_suffix =
        PLUSPLUS
        .lookback(1).push((p,xs) -> UnaryExpression.mk(POSTFIX_INCREMENT, $(xs,0)));

    public rule dec_suffix =
        MINUSMINUS
        .lookback(1).push((p,xs) -> UnaryExpression.mk(POSTFIX_DECREMENT, $(xs,0)));

    public rule postfix =
        choice(seq(DOT, dot_postfix), array_postfix, inc_suffix, dec_suffix, ref_postfix);

    public rule postfix_expr =
        seq(primary_expr, postfix.at_least(0));

    public rule inc_prefix =
        seq(PLUSPLUS, lazy(() -> this.prefix_expr))
        .push((p,xs) -> UnaryExpression.mk(PREFIX_INCREMENT, $(xs,0)));

    public rule dec_prefix =
        seq(MINUSMINUS, lazy(() -> this.prefix_expr))
        .push((p,xs) -> UnaryExpression.mk(PREFIX_DECREMENT, $(xs,0)));

    public rule unary_plus =
        seq(PLUS, lazy(() -> this.prefix_expr))
        .push((p,xs) -> UnaryExpression.mk(UNARY_PLUS, $(xs,0)));

    public rule unary_minus =
        seq(MINUS, lazy(() -> this.prefix_expr))
        .push((p,xs) -> UnaryExpression.mk(UNARY_MINUS, $(xs,0)));

    public rule complement =
        seq(TILDE, lazy(() -> this.prefix_expr))
        .push((p,xs) -> UnaryExpression.mk(COMPLEMENT, $(xs,0)));

    public rule not =
        seq(BANG, lazy(() -> this.prefix_expr))
        .push((p,xs) -> UnaryExpression.mk(NOT, $(xs,0)));

    // TODO lambda not defined yet
    public rule cast =
        //seq(LPAREN, type_union, RPAREN, choice(lambda, lazy(() -> this.prefix_expr)))
        seq(LPAREN, type_union, RPAREN, choice(lazy(() -> this.prefix_expr)))
        .push((p,xs) -> Cast.mk($(xs,0), $(xs,1)));

    public rule prefix_expr =
        choice(inc_prefix, dec_prefix, unary_plus, unary_minus, complement, not, cast, postfix_expr);

    // Expression - Binary ----------------------------------------------------

    BiConsumer<Parse, Object[]> binary_push
        = (p,xs) -> p.push(BinaryExpression.mk($(xs,1), $(xs,0), $(xs,2)));

    public rule mult_op = choice(
        STAR    .as_val(MULTIPLY),
        DIV     .as_val(DIVIDE),
        PERCENT .as_val(MODULUS));

    public rule mult_expr = left(
        prefix_expr, mult_op, binary_push);

    public rule add_op = choice(
        PLUS    .as_val(ADD),
        MINUS   .as_val(SUBTRACT));

    public rule add_expr = left(
        mult_expr, add_op, binary_push);

    public rule shift_op = choice(
        LTLT    .as_val(SHIFT_LEFT),
        GTGTGT  .as_val(BINARY_SHIFT_RIGHT),
        GTGT    .as_val(SHIFT_RIGHT));

    public rule shift_expr = left(
        add_expr, shift_op, binary_push);

    public rule order_op = choice(
        LT      .as_val(LOWER),
        LTEQ    .as_val(LOWER_OR_EQUAL),
        GT      .as_val(GREATER),
        GTEQ    .as_val(GREATER_OR_EQUAL));

    public rule order_expr = left(
        shift_expr, order_op, binary_push);

    public rule instanceof_expr = seq(
        order_expr,
        seq(_instanceof, type)
            .lookback(1)
            .push((p,xs) -> InstanceOf.mk($(xs,0), $(xs,1)))
            .opt());

    // Note: instanceof has officially the same precedence as order expressions
    // But due to the Java spec, instanceof cannot be nested within other order expressions,
    // or within itself: the operand would have primitive type boolean, and Java will not
    // autobox it to Boolean in this context.
    //
    // Modelling full nesting is possible and would be done as follow:
    //
    //    private rule order_suffix (rule token, BinaryOperator op) {
    //        return seq(token, shift_expr)
    //            .lookback(1)
    //            .push((p,xs) -> BinaryExpression.mk(op, $(xs,0), $(xs,1)));
    //    }
    //
    //    public rule order_op2 = choice(
    //        order_suffix(LT,   LOWER),
    //        order_suffix(LTEQ, LOWER_OR_EQUAL),
    //        order_suffix(GT,   GREATER),
    //        order_suffix(GTEQ, GREATER_OR_EQUAL),
    //        seq(_instanceof, type)
    //            .lookback(1).push((p,xs) -> InstanceOf.mk($(xs,0), $(xs,1))));
    //
    //    public rule order_expr2 = left(shift_expr, order_op2);

    public rule eq_op = choice(
        EQEQ    .as_val(EQUALS),
        BANGEQ  .as_val(NOT_EQUALS));

    public rule eq_expr = left(
        order_expr, eq_op, order_expr, binary_push);

    public rule binary_and_expr = left(
        eq_expr, AMP.as_val(BINARY_AND), eq_expr, binary_push);

    public rule xor_expr = left(
        binary_and_expr, CARET.as_val(XOR), binary_and_expr, binary_push);

    public rule binary_or_expr = left(
        xor_expr, BAR.as_val(BINARY_OR), xor_expr, binary_push);

    public rule and_expr = left(
        binary_or_expr, AMPAMP.as_val(LOGICAL_AND), binary_or_expr, binary_push);

    public rule or_expr = left(
        and_expr, BARBAR.as_val(LOGICAL_OR), and_expr, binary_push);

//    public rule ternary_suffix =
//        seq(QUES, lazy(() -> this.expr), COL, lazy(() -> this.expr))
//        .push((p, xs) -> new Ternary($(xs,0), $(xs,1), $(xs,2)));
//
//    public rule ternary =
//        seq(or_expr, ternary_suffix.opt());
//
//    public rule assignment_suffix =
//        choice(seq(EQ, lazy(() -> this.expr))
//        .push((p,xs) -> new Assign($(xs,0), $(xs,1), "=")}, seq(PLUSEQ, lazy(() -> this.expr))
//        .push((p,xs) -> new Assign($(xs,0), $(xs,1), "+=")}, seq(SUBEQ, lazy(() -> this.expr))
//        .push((p,xs) -> new Assign($(xs,0), $(xs,1), "-=")}, seq(STAREQ, lazy(() -> this.expr))
//        .push((p,xs) -> new Assign($(xs,0), $(xs,1), "*=")}, seq(DIVEQ, lazy(() -> this.expr))
//        .push((p, xs) -> new Assign($(xs,0), $(xs,1), "/=")}, seq(PERCENTEQ, lazy(() -> this.expr))
//        .push((p, xs) -> new Assign($(xs,0), $(xs,1), "%=")}, seq(LTLTEQ, lazy(() -> this.expr))
//        .push((p, xs) -> new Assign($(xs,0), $(xs,1), "<<=")}, seq(GTGTEQ, lazy(() -> this.expr))
//        .push((p, xs) -> new Assign($(xs,0), $(xs,1), ">>=")}, seq(GTGTGTEQ, lazy(() -> this.expr))
//        .push((p, xs) -> new Assign($(xs,0), $(xs,1), ">>>=")}, seq(AMPEQ, lazy(() -> this.expr))
//        .push((p, xs) -> new Assign($(xs,0), $(xs,1), "&=")}, seq(CARETEQ, lazy(() -> this.expr))
//        .push((p, xs) -> new Assign($(xs,0), $(xs,1), "^=")}, seq(BAREQ, lazy(() -> this.expr))
//        .push((p, xs) -> new Assign($(xs,0), $(xs,1), "|=")});
//
//    public rule assignment =
//        seq(ternary, assignment_suffix.opt());
//
    // TODO
    public rule expr =
        or_expr;
    //    choice(lambda, assignment);
//
//    /// STATEMENTS =================================================================================
//
//    public rule if_stmt =
//        seq(_if, par_expr, lazy(() -> this.stmt), seq(_else, lazy(() -> this.stmt)).maybe())
//        .push((p,xs) -> new If($(xs,0), $(xs,1), $(xs,2)));
//
//    public rule expr_stmt_list =
//        expr.sep(0, COMMA)
//        .push((p,xs) -> this.<Stmt>list(xs));
//
//    public rule for_init_decl =
//        seq(modifiers, var_decl_no_semi)
//        .push((p,xs) -> this.<Stmt>list(xs));
//
//    public rule for_init =
//        choice(for_init_decl, expr_stmt_list);
//
//    public rule basic_for_paren_part =
//        seq(for_init, SEMI, expr.maybe(), SEMI, expr_stmt_list.opt());
//
//    public rule basic_for_stmt =
//        seq(_for, basic_for_paren_part.bracketed("()"), lazy(() -> this.stmt))
//        .push((p,xs) -> new BasicFor($(xs,0), $(xs,1), $(xs,2), $(xs,3)));
//
//    public rule for_val_decl =
//        seq(modifiers, type, var_declarator_id, COL, expr);
//
//    public rule enhanced_for_stmt =
//        seq(_for, for_val_decl.bracketed("()"), lazy(() -> this.stmt))
//        .push((p,xs) -> new EnhancedFor($(xs,0), $(xs,1), $(xs,2), $(xs,3), $(xs,4)));
//
//    public rule while_stmt =
//        seq(_while, par_expr, lazy(() -> this.stmt))
//        .push((p,xs) -> new WhileStmt($(xs,0), $(xs,1)));
//
//    public rule do_while_stmt =
//        seq(_do, lazy(() -> this.stmt), _while, par_expr, SEMI)
//        .push((p, xs) -> new DoWhileStmt($(xs,0), $(xs,1)));
//
//    public rule catch_parameter_types =
//        type.sep(0, BAR)
//        .push((p,xs) -> this.<Type>list(xs));
//
//    public rule catch_parameter =
//        seq(modifiers, catch_parameter_types, var_declarator_id);
//
//    public rule catch_clause =
//        seq(catch, catch_parameter.bracketed("()"), lazy(() -> this.block))
//        .push((p,xs) -> new CatchClause($(xs,0), $(xs,1), $(xs,2), $(xs,3)));
//
//    public rule catch_clauses =
//        catch_clause.at_least(0)
//        .push((p,xs) -> this.<CatchClause>list(xs));
//
//    public rule finally_clause =
//        seq(finally, lazy(() -> this.block));
//
//    public rule resource =
//        seq(modifiers, type, var_declarator_id, EQ, expr)
//        .push((p,xs) -> new TryResource($(xs,0), $(xs,1), $(xs,2), $(xs,3)));
//
//    public rule resources =
//        resource.sep(1, SEMI).bracketed("()").opt()
//        .push((p, xs) -> this.<TryResource>list(xs));
//
//    public rule try_stmt =
//        seq(_try, resources, lazy(() -> this.block), catch_clauses, finally_clause.maybe())
//        .push((p,xs) -> new TryStmt($(xs,0), $(xs,1), $(xs,2), $(xs,3)));
//
//    public rule default_label =
//        seq(default, COL)
//        .push((p, xs) -> new DefaultLabel);
//
//    public rule case_label =
//        seq(case, expr, COL)
//        .push((p, xs) -> new CaseLabel($(xs,0)));
//
//    public rule switch_label =
//        choice(case_label, default_label);
//
//    public rule switch_clause =
//        seq(switch_label, lazy(() -> this.stmts))
//        .push((p,xs) -> new SwitchClause($(xs,0), $(xs,1)));
//
//    public rule switch_stmt =
//        seq(switch, par_expr, switch_clause.at_least(0).bracketed("{}"))
//        .push((p,xs) -> new SwitchStmt($(xs,0), it.list(1)));
//
//    public rule synchronized_stmt =
//        seq(synchronized, par_expr, lazy(() -> this.block))
//        .push((p,xs) -> new SynchronizedStmt($(xs,1), $(xs,2)));
//
//    public rule return_stmt =
//        seq(_return, expr.maybe(), SEMI)
//        .push((p, xs) -> new ReturnStmt($(xs,0)));
//
//    public rule throw_stmt =
//        seq(_throw, expr, SEMI)
//        .push((p, xs) -> new ThrowStmt($(xs,0)));
//
//    public rule break_stmt =
//        seq(_break, iden.maybe(), SEMI)
//        .push((p, xs) -> new BreakStmt($(xs,0)));
//
//    public rule continue_stmt =
//        seq(_continue, iden.maybe(), SEMI)
//        .push((p, xs) -> new ContinueStmt($(xs,0)));
//
//    public rule assert_stmt =
//        seq(assert, expr, seq(COL, expr).maybe(), semi)
//        .push((p, xs) -> new AssertStmt($(xs,0), $(xs,1)));
//
//    public rule semi_stmt =
//        SEMI
//        .push((p, xs) -> new SemiStmt);
//
//    public rule expr_stmt =
//        seq(expr, SEMI);
//
//    public rule labelled_stmt =
//        seq(iden, COL, lazy(() -> this.stmt))
//        .push((p, xs) -> new LabelledStmt($(xs,0), $(xs,1)));
//
//    public rule stmt =
//        choice(lazy(() -> this.block), if_stmt, basic_for_stmt, enhanced_for_stmt, while_stmt, do_while_stmt, try_stmt, switch_stmt, synchronized_stmt, return_stmt, throw_stmt, break_stmt, continue_stmt, assert_stmt, semi_stmt, expr_stmt, labelled_stmt, var_decl, type_decl);
//
//    public rule block =
//        stmt.at_least(0).bracketed("{}")
//        .push((p,xs) -> new Block(it.list()));
//
//    public rule stmts =
//        stmt.at_least(0)
//        .push((p,xs) -> this.<Stmt>list(xs));
//
//    /// TOP-LEVEL ==================================================================================
//
//    public rule package_decl =
//        seq(annotations, _package, qualified_iden, SEMI)
//        .push((p, xs) -> new Package($(xs,0), $(xs,1)));
//
//    public rule import_decl =
//        seq(import, static.as_bool(), qualified_iden, seq(DOT, STAR).as_bool(), semi)
//        .push((p, xs) -> new Import($(xs,0), $(xs,1), $(xs,2)));
//
//    public rule import_decls =
//        import_decl.at_least(0)
//        .push((p,xs) -> this.<Import>list(xs));
//
//    public rule root =
//        seq(lazy(() -> this.whitespace), package_decl.maybe(), import_decls, type_decls)
//        .push((p,xs) -> new File(input, $(xs,0), $(xs,1), $(xs,2)));

    public Grammar()
    {
        make_rule_names(this);
    }
}

