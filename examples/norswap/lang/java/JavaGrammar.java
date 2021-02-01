package norswap.lang.java;

import norswap.autumn.Grammar;
import norswap.autumn.actions.StackPush;
import norswap.lang.java.ast.*;
import norswap.lang.java.ast.TypeDeclaration.Kind;
import norswap.utils.data.wrappers.Pair;

import static java.util.Collections.emptyList;
import static norswap.lang.java.LexUtils.*;
import static norswap.lang.java.ast.BinaryOperator.*;
import static norswap.lang.java.ast.UnaryOperator.*;
import static norswap.utils.Vanilla.list;

/**
 * A full grammar for the Java languages.
 *
 * <p>Tested over a corpus of 38MB (the source code of Spring) and partially unit tested
 * ({@link lang.java.TestGrammar}).
 *
 * <p>The grammar was optimized (for a 16% reduction in run time) by strategically inserting
 * memoization caches on crucial rules via {@link rule#memo(int)}. Namely, we use the cache on the
 * following rules:
 * <ul>
 *     <li>{@link JavaGrammar#iden}</li>
 *     <li>{@link JavaGrammar#literal}</li>
 *     <li>{@link JavaGrammar#annotations}</li>
 *     <li>{@link JavaGrammar#basic_type}</li>
 *     <li>{@link JavaGrammar#modifiers}</li>
 * </ul>
 */
public final class JavaGrammar extends Grammar
{
    /// LEXICAL ====================================================================================

    // Whitespace ----------------------------------------------------------------------------------

    public rule space_char          = cpred(Character::isWhitespace);
    public rule not_line            = seq(str("\n").not(), any);
    public rule line_comment        = seq("//", not_line.at_least(0), str("\n").opt());

    public rule not_comment_term    = seq(str("*/").not(), any);
    public rule multi_comment       = seq("/*", not_comment_term.at_least(0), "*/");

    public rule whitespace          = choice(space_char, line_comment, multi_comment);

    { ws = whitespace.at_least(0); }

    // Identifiers ---------------------------------------------------------------------------------

    public rule id_start = cpred(Character::isJavaIdentifierStart);
    {           id_part  = cpred(c -> c != 0 && Character.isJavaIdentifierPart(c)); }

    /** Rule for parsing Identifiers, ensuring we do not match keywords, and memoized. */
    public rule iden = identifier(seq(id_start, id_part.at_least(0)))
        .push($ -> Identifier.mk($.str()))
        .memo(32);

    // Keywords and Operators ----------------------------------------------------------------------

    public rule _boolean        = reserved("boolean");
    public rule _byte           = reserved("byte");
    public rule _char           = reserved("char");
    public rule _double         = reserved("double");
    public rule _float          = reserved("float");
    public rule _int            = reserved("int");
    public rule _long           = reserved("long");
    public rule _short          = reserved("short");
    public rule _void           = reserved("void");
    public rule _abstract       = reserved("abstract");
    public rule _default        = reserved("default");
    public rule _final          = reserved("final");
    public rule _native         = reserved("native");
    public rule _private        = reserved("private");
    public rule _protected      = reserved("protected");
    public rule _public         = reserved("public");
    public rule _static         = reserved("static");
    public rule _strictfp       = reserved("strictfp");
    public rule _synchronized   = reserved("synchronized");
    public rule _transient      = reserved("transient");
    public rule _volatile       = reserved("volatile");
    public rule _assert         = reserved("assert");
    public rule _break          = reserved("break");
    public rule _case           = reserved("case");
    public rule _catch          = reserved("catch");
    public rule _class          = reserved("class");
    public rule _const          = reserved("const");
    public rule _continue       = reserved("continue");
    public rule _do             = reserved("do");
    public rule _else           = reserved("else");
    public rule _enum           = reserved("enum");
    public rule _extends        = reserved("extends");
    public rule _finally        = reserved("finally");
    public rule _for            = reserved("for");
    public rule _goto           = reserved("goto");
    public rule _if             = reserved("if");
    public rule _implements     = reserved("implements");
    public rule _import         = reserved("import");
    public rule _interface      = reserved("interface");
    public rule _instanceof     = reserved("instanceof");
    public rule _new            = reserved("new");
    public rule _package        = reserved("package");
    public rule _return         = reserved("return");
    public rule _super          = reserved("super");
    public rule _switch         = reserved("switch");
    public rule _this           = reserved("this");
    public rule _throws         = reserved("throws");
    public rule _throw          = reserved("throw");
    public rule _try            = reserved("try");
    public rule _while          = reserved("while");

    public rule _false          = reserved("false")  .as_val(false);
    public rule _true           = reserved("true")   .as_val(true);
    public rule _null           = reserved("null")   .as_val(Null.NULL);

    // Names are taken from the javac8 lexer.
    // https://github.com/dmlloyd/openjdk/blob/jdk8u/jdk8u/langtools/src/share/classes/com/sun/tools/javac/parser/Tokens.java
    // ordering matters when there are shared prefixes!

    public rule BANG            = word("!");
    public rule BANGEQ          = word("!=");
    public rule PERCENT         = word("%");
    public rule PERCENTEQ       = word("%=");
    public rule AMP             = word("&");
    public rule AMPAMP          = word("&&");
    public rule AMPEQ           = word("&=");
    public rule LPAREN          = word("(");
    public rule RPAREN          = word(")");
    public rule STAR            = word("*");
    public rule STAREQ          = word("*=");
    public rule PLUS            = word("+");
    public rule PLUSPLUS        = word("++");
    public rule PLUSEQ          = word("+=");
    public rule COMMA           = word(",");
    public rule SUB             = word("-");
    public rule SUBSUB          = word("--");
    public rule SUBEQ           = word("-=");
    public rule EQ              = word("=");
    public rule EQEQ            = word("==");
    public rule QUES            = word("?");
    public rule CARET           = word("^");
    public rule CARETEQ         = word("^=");
    public rule LBRACE          = word("{");
    public rule RBRACE          = word("}");
    public rule BAR             = word("|");
    public rule BARBAR          = word("||");
    public rule BAREQ           = word("|=");
    public rule TILDE           = word("~");
    public rule MONKEYS_AT      = word("@");
    public rule DIV             = word("/");
    public rule DIVEQ           = word("/=");
    public rule GTEQ            = word(">=");
    public rule LTEQ            = word("<=");
    public rule LTLTEQ          = word("<<=");
    public rule LTLT            = word("<<");
    public rule GTGTEQ          = word(">>=");
    public rule GTGTGTEQ        = word(">>>=");
    public rule GT              = word(">");
    public rule LT              = word("<");
    public rule LBRACKET        = word("[");
    public rule RBRACKET        = word("]");
    public rule ARROW           = word("->");
    public rule COL             = word(":");
    public rule COLCOL          = word("::");
    public rule SEMI            = word(";");
    public rule DOT             = word(".");
    public rule ELLIPSIS        = word("...");

    public rule GTGT            = word(">>");
    public rule GTGTGT          = word(">>>");

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
        .push($ -> parse_floating($.str()).unwrap());

    // Numerals - Integral -------------------------------------------------------------------------

    public rule bit             = set("01");
    public rule binary_prefix   = choice("0b", "0B");
    public rule binary_num      = seq(binary_prefix, bit.at_least(1).sep(1, underscores));
    public rule octal_num       = seq("0", seq(underscores, octal_digit).at_least(1));
    public rule decimal_num     = choice("0", digits1);
    public rule integer_num     = choice(hex_num, binary_num, octal_num, decimal_num);

    public rule integer_literal = seq(integer_num, set("lL").opt())
        .push($ -> parse_integer($.str()).unwrap());

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
        .push($ -> parse_char($.str()).unwrap());

    public rule string_literal = seq("\"", nake_str_char.at_least(0), "\"")
        .push($ -> parse_string($.str()).unwrap());

    // Literal ----------------------------------------------------------------

    public rule literal = choice(
        float_literal, integer_literal, string_literal, _null, _true, _false, char_literal)
        .word()
        .push($ -> Literal.mk($.$[0]))
        .memo(32);

    //// LAZY FORWARD REFS =========================================================================

    public rule _stmt =
        lazy(() -> this.stmt);

    public rule _expr =
        lazy(() -> this.expr);

    public rule _block =
        lazy(() -> this.block);

    /// ANNOTATIONS ================================================================================

    public rule annotation_element = choice(
        lazy(() -> this.ternary_expr),
        lazy(() -> this.annotation_element_list),
        lazy(() -> this.annotation));

    public rule annotation_inner_list =
        lazy(() -> this.annotation_element).sep_trailing(0, COMMA);

    public rule annotation_element_list =
        seq(LBRACE, annotation_inner_list, RBRACE)
        .push($ -> AnnotationElementList.mk($.$list()));

    public rule annotation_element_pair =
        seq(iden, EQ, annotation_element)
        .push($ -> new Pair<Identifier, AnnotationElement>($.$0(), $.$1()));

    public rule normal_annotation_element_pairs =
        annotation_element_pair.sep(1, COMMA)
        .push($ -> $.$list());

    public rule normal_annotation_suffix =
        seq(LPAREN, normal_annotation_element_pairs, RPAREN)
        .push($ -> NormalAnnotation.mk($.$0(), $.$1()),
            LOOKBACK(1));

    public rule single_element_annotation_suffix =
        seq(LPAREN, annotation_element, RPAREN)
        .push($ -> SingleElementAnnotation.mk($.$0(), $.$1()),
            LOOKBACK(1));

    public rule marker_annotation_suffix =
        seq(LPAREN, RPAREN).opt()
         .push($ -> MarkerAnnotation.mk($.$0()),
            LOOKBACK(1));

    public rule annotation_suffix = choice(
        normal_annotation_suffix,
        single_element_annotation_suffix,
        marker_annotation_suffix);

    public rule qualified_iden =
        iden.sep(1, DOT)
        .as_list(Identifier.class);

    public rule annotation =
        seq(MONKEYS_AT, qualified_iden, annotation_suffix);

    public rule annotations =
        annotation.at_least(0)
        .as_list(TAnnotation.class)
        .memo(32);

    /// TYPES ======================================================================================

    public rule basic_type =
        choice(_byte, _short, _int, _long, _char, _float, _double, _boolean, _void)
        .push($ -> BasicType.valueOf("_" + trim_trailing_whitespace($.str())))
        .memo(32);

    public rule primitive_type =
        seq(annotations, basic_type)
        .push($ -> PrimitiveType.mk($.$0(), $.$1()));

    public rule extends_bound =
        seq(_extends, lazy(() -> this.type))
        .push($ -> ExtendsBound.mk($.$0()));

    public rule super_bound =
        seq(_super, lazy(() -> this.type))
        .push($ -> SuperBound.mk($.$0()));

    public rule type_bound =
        choice(extends_bound, super_bound).or_push_null();

    public rule wildcard =
        seq(annotations, QUES, type_bound)
        .push($ -> Wildcard.mk($.$0(), $.$1()));

    public rule opt_type_args =
        seq(LT, choice(lazy(() -> this.type), wildcard).sep(0, COMMA), GT).opt()
        .as_list(TType.class);

    public rule class_type_part =
        seq(annotations, iden, opt_type_args)
        .push($ -> ClassTypePart.mk($.$0(), $.$1(), $.$2()));

    public rule class_type =
        class_type_part.sep(1, DOT)
        .push($ -> ClassType.mk($.$list()));

    public rule stem_type =
        choice(primitive_type, class_type);

    public rule dim =
        seq(annotations, seq(LBRACKET, RBRACKET))
        .push($ -> Dimension.mk($.$0()));

    public rule dims =
        dim.at_least(0)
        .as_list(Dimension.class);

    public rule dims1 =
        dim.at_least(1)
        .as_list(Dimension.class);

    public rule type_dim_suffix =
        dims1
        .push($ -> ArrayType.mk($.$0(), $.$1()),
            LOOKBACK(1));

    public rule type =
        seq(stem_type, type_dim_suffix.opt());

    public rule type_union_syntax =
        lazy(() -> this.type).sep(1, AMP);

    public rule type_union =
        type_union_syntax
        .as_list(TType.class);

    public rule type_bounds =
        seq(_extends, type_union_syntax).opt()
        .as_list(TType.class);

    public rule type_param =
        seq(annotations, iden, type_bounds)
        .push($ -> TypeParameter.mk($.$0(), $.$1(), $.$2()));

    public rule type_params =
        seq(LT, type_param.sep(0, COMMA), GT).opt()
        .as_list(TypeParameter.class);

    /// EXPRESSIONS ================================================================================

    // Initializers -----------------------------------------------------------

    public rule var_init =
        choice(_expr, lazy(() -> this.array_init));

    public rule array_init =
        seq(LBRACE, var_init.sep_trailing(0, COMMA), RBRACE)
        .push($ -> ArrayInitializer.mk($.$list()));

    // Array Constructor ------------------------------------------------------

    public rule dim_expr =
        seq(annotations, LBRACKET, _expr, RBRACKET)
        .push($ -> DimExpression.mk($.$0(), $.$1()));

    public rule dim_exprs =
        dim_expr.at_least(1)
        .as_list(DimExpression.class);

    public rule dim_expr_array_creator =
        seq(stem_type, dim_exprs, dims)
        .push($ -> ArrayConstructorCall.mk($.$0(), $.$1(), $.$2(), null));

    public rule init_array_creator =
        seq(stem_type, dims1, array_init)
        .push($ -> ArrayConstructorCall.mk($.$0(), emptyList(), $.$1(), $.$2()));

    public rule array_ctor_call =
        seq(_new, choice(dim_expr_array_creator, init_array_creator));

    // Lambda Expression ------------------------------------------------------

    public rule lambda = lazy(() ->
        seq(this.lambda_params, ARROW, choice(this.block, this.expr)))
        .push($ -> Lambda.mk($.$0(), $.$1()));

    // Expression - Primary ---------------------------------------------------

    public rule args =
        seq(LPAREN, _expr.sep(0, COMMA), RPAREN)
        .as_list(Expression.class);

    public rule par_expr =
        seq(LPAREN, _expr, RPAREN)
        .push($ -> ParenExpression.mk($.$0()));

    public rule ctor_call =
        seq(_new, opt_type_args, stem_type, args, lazy(() -> this.type_body).or_push_null())
        .push($ -> ConstructorCall.mk($.$0(), $.$1(), $.$2(), $.$3()));

    public rule new_ref_suffix =
        _new
        .push($ -> NewReference.mk($.$0(), $.$1()),
            LOOKBACK(2));

    public rule method_ref_suffix =
        iden
        .push($ -> TypeMethodReference.mk($.$0(), $.$1(), $.$2()),
            LOOKBACK(2));

    public rule ref_suffix =
        seq(COLCOL, opt_type_args, choice(method_ref_suffix, new_ref_suffix));

    public rule class_expr_suffix =
        seq(DOT, _class)
        .push($ -> ClassExpression.mk($.$0()),
            LOOKBACK(1));

    public rule type_suffix_expr =
        seq(type, choice(ref_suffix, class_expr_suffix));

    public rule iden_or_method_expr =
        seq(iden, args.or_push_null())
        .push($ -> $.$1() == null ? $.$0() : MethodCall.mk(null, list(), $.$0(), $.$1()));

    public rule this_expr =
        seq(_this, args.or_push_null())
        .push($ -> $.$0() == null ? This.mk() : ThisCall.mk($.$0()));

    public rule super_expr =
        seq(_super, args.or_push_null())
        .push($ -> $.$0() == null ? Super.mk() : SuperCall.mk($.$0()));

    public rule primary_expr = choice(
        lambda, par_expr, array_ctor_call, ctor_call, type_suffix_expr, iden_or_method_expr,
        this_expr, super_expr, literal);

    // Expression - Postfix & Prefix ------------------------------------------

    public rule prefix_op = choice(
        PLUSPLUS    .as_val(PREFIX_INCREMENT),
        SUBSUB      .as_val(PREFIX_DECREMENT),
        PLUS        .as_val(UNARY_PLUS),
        SUB         .as_val(UNARY_MINUS),
        TILDE       .as_val(BITWISE_COMPLEMENT),
        BANG        .as_val(LOGICAL_COMPLEMENT));

    public rule postfix_expr = left_expression()
        .left(primary_expr)
        .suffix(seq(DOT, opt_type_args, iden, args),
            $ -> MethodCall.mk($.$0(), $.$1(), $.$2(), $.$3()))
        .suffix(seq(DOT, iden),
            $ -> DotIden.mk($.$0(), $.$1()))
        .suffix(seq(DOT, _this),
            $ -> UnaryExpression.mk(DOT_THIS, $.$0()))
        .suffix(seq(DOT, _super),
            $ -> UnaryExpression.mk(DOT_SUPER, $.$0()))
        .suffix(seq(DOT, ctor_call),
            $ -> DotNew.mk($.$0(), $.$1()))
        .suffix(seq(LBRACKET, _expr, RBRACKET),
            $ -> ArrayAccess.mk($.$0(), $.$1()))
        .suffix(PLUSPLUS,
            $ -> UnaryExpression.mk(POSTFIX_INCREMENT, $.$0()))
        .suffix(SUBSUB,
            $ -> UnaryExpression.mk(POSTFIX_DECREMENT, $.$0()))
        .suffix(seq(COLCOL, opt_type_args, iden),
            $ -> BoundMethodReference.mk($.$0(), $.$1(), $.$2()));

    public rule prefix_expr = right_expression()
        .right(postfix_expr)
        .prefix(prefix_op,
            $ -> UnaryExpression.mk($.$0(), $.$1()))
        .prefix(seq(LPAREN, type_union, RPAREN),
            $ -> Cast.mk($.$0(), $.$1()));

    // Expression - Binary ----------------------------------------------------

    StackPush binary_push =
        $ -> BinaryExpression.mk($.$1(), $.$0(), $.$2());

    public rule mult_op = choice(
        STAR        .as_val(MULTIPLY),
        DIV         .as_val(DIVIDE),
        PERCENT     .as_val(REMAINDER));

    public rule add_op = choice(
        PLUS        .as_val(ADD),
        SUB         .as_val(SUBTRACT));

    public rule shift_op = choice(
        LTLT        .as_val(LEFT_SHIFT),
        GTGTGT      .as_val(UNSIGNED_RIGHT_SHIFT),
        GTGT        .as_val(RIGHT_SHIFT));

    public rule order_op = choice(
        LTEQ        .as_val(LESS_THAN_EQUAL),
        LT          .as_val(LESS_THAN),
        GTEQ        .as_val(GREATER_THAN_EQUAL),
        GT          .as_val(GREATER_THAN));

    public rule eq_op = choice(
        EQEQ        .as_val(EQUAL_TO),
        BANGEQ      .as_val(NOT_EQUAL_TO));

    public rule assignment_op = choice(
        EQ          .as_val(ASSIGNMENT),
        PLUSEQ      .as_val(ADD_ASSIGNMENT),
        SUBEQ       .as_val(SUBTRACT_ASSIGNMENT),
        STAREQ      .as_val(MULTIPLY_ASSIGNMENT),
        DIVEQ       .as_val(DIVIDE_ASSIGNMENT),
        PERCENTEQ   .as_val(REMAINDER_ASSIGNMENT),
        LTLTEQ      .as_val(LEFT_SHIFT_ASSIGNMENT),
        GTGTEQ      .as_val(RIGHT_SHIFT_ASSIGNMENT),
        GTGTGTEQ    .as_val(UNSIGNED_RIGHT_SHIFT_ASSIGNMENT),
        AMPEQ       .as_val(AND_ASSIGNMENT),
        CARETEQ     .as_val(XOR_ASSIGNMENT),
        BAREQ       .as_val(OR_ASSIGNMENT));

    public rule mult_expr = left_expression()
        .operand(prefix_expr)
        .infix(mult_op, binary_push);

    public rule add_expr = left_expression()
        .operand(mult_expr)
        .infix(add_op, binary_push);

    public rule shift_expr = left_expression()
        .operand(add_expr)
        .infix(shift_op, binary_push);

    public rule order_expr = left_expression()
        .operand(shift_expr)
        .suffix(seq(_instanceof, type),
            $ -> InstanceOf.mk($.$0(), $.$1()))
        .infix(order_op, binary_push);

    public rule eq_expr = left_expression()
        .operand(order_expr)
        .infix(eq_op, binary_push);

    public rule binary_and_expr = left_expression()
        .operand(eq_expr)
        .infix(AMP.as_val(AND), binary_push);

    public rule xor_expr = left_expression()
        .operand(binary_and_expr)
        .infix(CARET.as_val(XOR), binary_push);

    public rule binary_or_expr = left_expression()
        .operand(xor_expr)
        .infix(BAR.as_val(OR), binary_push);

    public rule conditional_and_expr = left_expression()
        .operand(binary_or_expr)
        .infix(AMPAMP.as_val(CONDITIONAL_AND), binary_push);

    public rule conditional_or_expr = left_expression()
        .operand(conditional_and_expr)
        .infix(BARBAR.as_val(CONDITIONAL_OR), binary_push);

    public rule ternary_expr = right_expression()
        .operand(conditional_or_expr)
        .infix(seq(QUES, _expr, COL),
            $ -> TernaryExpression.mk($.$0(), $.$1(), $.$2()));

    public rule expr = right_expression()
        .operand(ternary_expr)
        .infix(assignment_op, binary_push);

    /// MODIFIERS ==================================================================================

    public rule keyword_modifier =
        choice(
            _public, _protected, _private, _abstract, _static, _final, _synchronized,
            _native, _strictfp, _default, _transient, _volatile)
            .push($ -> Keyword.valueOf("_" + trim_trailing_whitespace($.str())));

    public rule modifier =
        choice(annotation, keyword_modifier);

    public rule modifiers =
        modifier.at_least(0)
        .as_list(Modifier.class).memo(32);

    /// PARAMETERS =================================================================================

    public rule this_parameter_qualifier =
        seq(iden, DOT).at_least(0)
        .as_list(String.class);

    public rule this_param_suffix =
        seq(this_parameter_qualifier, _this)
        .push($ -> ThisParameter.mk($.$0(), $.$1(), $.$2()),
            LOOKBACK(2));

    public rule iden_param_suffix =
        seq(iden, dims)
        .push($ -> IdenParameter.mk($.$0(), $.$1(), $.$2(), $.$3()),
            LOOKBACK(2));

    public rule variadic_param_suffix =
        seq(annotations, ELLIPSIS, iden)
        .push($ -> VariadicParameter.mk($.$0(), $.$1(), $.$2(), $.$3()),
            LOOKBACK(2));

    public rule formal_param_suffix =
        choice(iden_param_suffix, this_param_suffix, variadic_param_suffix);

    public rule formal_param =
        seq(modifiers, type, formal_param_suffix);

    public rule formal_params =
        seq(LPAREN, formal_param.sep(0, COMMA), RPAREN)
        .push($ -> FormalParameters.mk(list()));

    public rule untyped_params =
        seq(LPAREN, iden.sep(1, COMMA), RPAREN)
        .push($ -> UntypedParameters.mk(list()));

    public rule single_param =
        iden
        .push($ -> UntypedParameters.mk($.$list()));

    public rule lambda_params =
        choice(formal_params, untyped_params, single_param);

    /// NON-TYPE DECLARATIONS ======================================================================

    public rule var_declarator_id =
        seq(iden, dims)
        .push($ -> VarDeclaratorID.mk($.$0(), $.$1()));

    public rule var_declarator =
        seq(var_declarator_id, seq(EQ, var_init).or_push_null())
        .push($ -> VarDeclarator.mk($.$0(), $.$1()));

    public rule var_declarators =
        var_declarator.sep(1, COMMA)
        .as_list(VarDeclarator.class);

    public rule var_decl_suffix_no_semi =
        seq(type, var_declarators)
        .push($ -> VarDeclaration.mk($.$0(), $.$1(), $.$2()),
            LOOKBACK(1));

    public rule var_decl_suffix =
        seq(var_decl_suffix_no_semi, SEMI);

    public rule var_decl =
        seq(modifiers, var_decl_suffix);

    public rule throws_clause =
        seq(_throws, type.sep(1, COMMA)).opt()
        .as_list(TType.class);

    public rule block_or_semi =
        choice(_block, SEMI.as_val(null));

    public rule method_decl_suffix =
        seq(type_params, type, iden, formal_params, dims, throws_clause, block_or_semi)
        .push($ -> MethodDeclaration.mk(
            $.$0(), $.$1(), $.$2(), $.$3(), $.$4(), $.$5(), $.$6(), $.$7()),
                LOOKBACK(1));

    public rule constructor_decl_suffix =
        seq(type_params, iden, formal_params, throws_clause, _block)
        .push($ -> ConstructorDeclaration.mk(
            $.$0(), $.$1(), $.$2(), $.$3(), $.$4(), $.$5()),
                LOOKBACK(1));

    public rule init_block =
        seq(_static.as_bool(), _block)
        .push($ -> InitBlock.mk($.$0(), $.$1()));

    /// TYPE DECLARATIONS ==========================================================================

    // Common -----------------------------------------------------------------

    public rule extends_clause =
        seq(_extends, type.sep(0, COMMA)).opt()
        .as_list(TType.class);

    public rule implements_clause =
        seq(_implements, type.sep(0, COMMA)).opt()
        .as_list(TType.class);

    public rule type_sig =
        seq(iden, type_params, extends_clause, implements_clause);

    public rule class_modifierized_decl = seq(
        modifiers,
        choice(
            var_decl_suffix,
            method_decl_suffix,
            constructor_decl_suffix,
            lazy(() -> this.type_decl_suffix)));

    public rule class_body_decl =
        choice(class_modifierized_decl, init_block, SEMI);

    public rule class_body_decls =
        class_body_decl.at_least(0)
        .as_list(Declaration.class);

    public rule type_body =
        seq(LBRACE, class_body_decls, RBRACE);

    // Enum -------------------------------------------------------------------

    public rule enum_constant =
        seq(annotations, iden, args.or_push_null(), type_body.or_push_null())
        .push($ -> EnumConstant.mk($.$0(), $.$1(), $.$2(), $.$3()));

    public rule enum_class_decls =
        seq(SEMI, class_body_decl.at_least(0)).opt();

    public rule enum_constants =
        enum_constant.sep_trailing(1, COMMA).opt();

    public rule enum_body =
        seq(LBRACE, enum_constants, enum_class_decls, RBRACE)
        .as_list(Declaration.class);

    public rule enum_decl_suffix =
        seq(_enum, type_sig, enum_body)
        .push($ -> TypeDeclaration.mk(Kind.ENUM,
            $.$0(), $.$1(), $.$2(), $.$3(), $.$4(), $.$5()),
                LOOKBACK(1));

    // Annotations ------------------------------------------------------------

    public rule annot_default_clause =
        seq(_default, annotation_element)
        .push($ -> $.$0());

    public rule annot_elem_decl =
        seq(modifiers, type, iden, LPAREN, RPAREN, dims, annot_default_clause.or_push_null(), SEMI)
        .push($ -> AnnotationElementDeclaration.mk(
            $.$0(), $.$1(), $.$2(), $.$3(), $.$4()));

    public rule annot_body_decls =
        choice(annot_elem_decl, class_body_decl).at_least(0)
        .as_list(Declaration.class);

    public rule annotation_decl_suffix =
        seq(MONKEYS_AT, _interface, type_sig, LBRACE, annot_body_decls, RBRACE)
        .push($ -> TypeDeclaration.mk(Kind.ANNOTATION,
            $.$0(), $.$1(), $.$2(), $.$3(), $.$4(), $.$5()),
                LOOKBACK(1));

    //// ------------------------------------------------------------------------

    public rule class_decl_suffix =
        seq(_class, type_sig, type_body)
        .push($ -> TypeDeclaration.mk(Kind.CLASS,
            $.$0(), $.$1(), $.$2(), $.$3(), $.$4(), $.$5()),
                LOOKBACK(1));

    public rule interface_declaration_suffix =
        seq(_interface, type_sig, type_body)
        .push($ -> TypeDeclaration.mk(Kind.INTERFACE,
            $.$0(), $.$1(), $.$2(), $.$3(), $.$4(), $.$5()),
                LOOKBACK(1));

    public rule type_decl_suffix = choice(
        class_decl_suffix,
        interface_declaration_suffix,
        enum_decl_suffix,
        annotation_decl_suffix);

    public rule type_decl =
        seq(modifiers, type_decl_suffix);

    public rule type_decls =
        choice(type_decl, SEMI).at_least(0)
        .as_list(Declaration.class);

    /// STATEMENTS =================================================================================

    public rule if_stmt =
        seq(_if, par_expr, _stmt, seq(_else, _stmt).or_push_null())
        .push($ -> IfStatement.mk($.$0(), $.$1(), $.$2()));

    public rule expr_stmt_list =
        expr.sep(0, COMMA)
        .as_list(Statement.class);

    public rule for_init_decl =
        seq(modifiers, var_decl_suffix_no_semi)
        .as_list(Statement.class);

    public rule for_init =
        choice(for_init_decl, expr_stmt_list);

    public rule basic_for_paren_part =
        seq(for_init, SEMI, expr.or_push_null(), SEMI, expr_stmt_list.opt());

    public rule basic_for_stmt =
        seq(_for, LPAREN, basic_for_paren_part, RPAREN, _stmt)
        .push($ -> BasicForStatement.mk($.$0(), $.$1(), $.$2(), $.$3()));

    public rule for_val_decl =
        seq(modifiers, type, var_declarator_id, COL, expr);

    public rule enhanced_for_stmt =
        seq(_for, LPAREN, for_val_decl, RPAREN, _stmt)
        .push($ -> EnhancedForStatement.mk($.$0(), $.$1(), $.$2(), $.$3(), $.$4()));

    public rule while_stmt =
        seq(_while, par_expr, _stmt)
        .push($ -> WhileStatement.mk($.$0(), $.$1()));

    public rule do_while_stmt =
        seq(_do, _stmt, _while, par_expr, SEMI)
        .push($ -> DoWhileStatement.mk($.$0(), $.$1()));

    public rule catch_parameter_types =
        type.sep(0, BAR)
        .as_list(TType.class);

    public rule catch_parameter =
        seq(modifiers, catch_parameter_types, var_declarator_id);

    public rule catch_clause =
        seq(_catch, LPAREN, catch_parameter, RPAREN, _block)
        .push($ -> CatchClause.mk($.$0(), $.$1(), $.$2(), $.$3()));

    public rule catch_clauses =
        catch_clause.at_least(0)
        .as_list(CatchClause.class);

    public rule finally_clause =
        seq(_finally, _block);

    public rule resource =
        seq(modifiers, type, var_declarator_id, EQ, expr)
        .push($ -> TryResource.mk($.$0(), $.$1(), $.$2(), $.$3()));

    public rule resources =
        seq(LPAREN, resource.sep(1, SEMI), RPAREN).opt()
        .as_list(TryResource.class);

    public rule try_stmt =
        seq(_try, resources, _block, catch_clauses, finally_clause.or_push_null())
        .push($ -> TryStatement.mk($.$0(), $.$1(), $.$2(), $.$3()));

    public rule default_label =
        seq(_default, COL)
        .push($ -> DefaultLabel.mk());

    public rule case_label =
        seq(_case, expr, COL)
        .push($ -> CaseLabel.mk($.$0()));

    public rule switch_label =
        choice(case_label, default_label);

    public rule switch_clause =
        seq(switch_label, lazy(() -> this.statements))
        .push($ -> SwitchClause.mk($.$0(), $.$1()));

    public rule switch_clauses =
        switch_clause.at_least(0)
        .as_list(SwitchClause.class);

    public rule switch_stmt =
        seq(_switch, par_expr, LBRACE, switch_clauses, RBRACE)
        .push($ -> SwitchStatement.mk($.$0(), $.$1()));

    public rule synchronized_stmt =
        seq(_synchronized, par_expr, _block)
        .push($ -> SynchronizedStatement.mk($.$0(), $.$1()));

    public rule return_stmt =
        seq(_return, expr.or_push_null(), SEMI)
        .push($ -> ReturnStatement.mk($.$0()));

    public rule throw_stmt =
        seq(_throw, expr, SEMI)
        .push($ -> ThrowStatement.mk($.$0()));

    public rule break_stmt =
        seq(_break, iden.or_push_null(), SEMI)
        .push($ -> BreakStatement.mk($.$0()));

    public rule continue_stmt =
        seq(_continue, iden.or_push_null(), SEMI)
        .push($ -> ContinueStatement.mk($.$0()));

    public rule assert_stmt =
        seq(_assert, expr, seq(COL, expr).or_push_null(), SEMI)
        .push($ -> AssertStatement.mk($.$0(), $.$1()));

    public rule semi_stmt =
        SEMI
        .push($ -> SemiStatement.mk());

    public rule expr_stmt =
        seq(expr, SEMI);

    public rule labelled_stmt =
        seq(iden, COL, _stmt)
        .push($ -> LabelledStatement.mk($.$0(), $.$1()));

    public rule stmt = choice(
        _block,
        if_stmt,
        basic_for_stmt,
        enhanced_for_stmt,
        while_stmt,
        do_while_stmt,
        try_stmt,
        switch_stmt,
        synchronized_stmt,
        return_stmt,
        throw_stmt,
        break_stmt,
        continue_stmt,
        assert_stmt,
        semi_stmt,
        expr_stmt,
        labelled_stmt,
        var_decl,
        type_decl);

    public rule block =
        seq(LBRACE, stmt.at_least(0), RBRACE)
        .push($ -> Block.mk($.$list()));

    public rule statements =
        stmt.at_least(0)
        .as_list(Statement.class);

    /// TOP-LEVEL ==================================================================================

    public rule package_decl =
        seq(annotations, _package, qualified_iden, SEMI)
        .push($ -> PackageDeclaration.mk($.$0(), $.$1()));

    public rule import_decl =
        seq(_import, _static.as_bool(), qualified_iden, seq(DOT, STAR).as_bool(), SEMI)
        .push($ -> ImportDeclaration.mk($.$0(), $.$1(), $.$2()));

    public rule import_decls =
        import_decl.at_least(0)
        .as_list(ImportDeclaration.class);

    public rule root =
        seq(ws, package_decl.or_push_null(), import_decls, type_decls)
        .push($ -> JavaFile.mk($.$0(), $.$1(), $.$2()));

    @Override public rule root() {
        return root;
    }

    // =============================================================================================
}