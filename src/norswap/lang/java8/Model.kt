package norswap.lang.java8
import norswap.autumn.model.*

class Java8Model
{
    /// LEXICAL ====================================================================================
    val LEXICAL = section(1)

    //// Whitespace ------------------------------------------------------------
    val `Whitespace` = section(2)

    val line_comment
        =  ("//".str .. (!"char_any" until0 "\n".str)).end

    val multi_comment
        =  ("/*".str .. (!"char_any" until0 "*/".str)).end

    val whitespace
        = (!"space_char" / line_comment / multi_comment).repeat0

    //// Keywords and Operators ------------------------------------------------
    val `Keywords and Operators`= section(2)

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

    val `false` = "false".str.token("false")
    val `true` = "true".str.token("true")
    val `null` = "null".str.token("Null")

    val `assert` = "assert".keyword
    val `break` = "break".keyword
    val `case` = "case".keyword
    val `catch` = "catch".keyword
    val `class` = "class".keyword
    val `const` = "const".keyword
    val `continue` = "continue".keyword
    val `do` = "do".keyword
    val `else` = "else".keyword
    val `enum` = "enum".keyword
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
    val ellipsis = "...".keyword
    val dcolon = "::".keyword

    //// Identifiers -----------------------------------------------------------
    val `Identifiers` = section(2) // (must come after keywords)

    val iden = (!"java_iden").token

    val `_`
        = "_".str

    val dlit
        = ".".str

    val hex_prefix
        =  ("0x".str / "0x".str).end

    val underscores
        = `_`.repeat0

    val digits1
        = !"digit" around0 underscores

    val digits0
        = !"digit" around0 underscores

    val hex_digits
        = !"hex_digit" around1 underscores

    val hex_num
        = (hex_prefix .. hex_digits).end

    //// Numerals - Floating Point ---------------------------------------------
    val `Numerals - Floating Point` = section(2)

    val hex_significand =
        ((hex_prefix .. hex_digits.opt .. dlit .. hex_digits) / (hex_num .. dlit.opt)).end

    val exp_sign_opt
        = "+-".set.opt

    val exponent
        =  ("eE".set ..exp_sign_opt .. digits1).end

    val binary_exponent
        =  ("pP".set .. exp_sign_opt .. digits1).end

    val float_suffix
        = "fFdD".set

    val float_suffix_opt
        = float_suffix.opt

    val hex_float_lit
        =  (hex_significand .. binary_exponent .. float_suffix_opt).end

    val decimal_float_lit = (
        (digits1 .. dlit .. digits0.. exponent.opt .. float_suffix_opt) /
        (dlit .. digits1.. exponent.opt .. float_suffix_opt) /
        (digits1 .. exponent .. float_suffix_opt) /
        (digits1 .. exponent.opt ..float_suffix)
    ).end

    val float_literal
        = (hex_float_lit / decimal_float_lit)
        .token("parse_float(it)")

    //// Numerals - Integral ---------------------------------------------------
    val `Numerals - Integral` = section(2)

    val bit
        = "01".set

    val binary_prefix
        = "0b".str / "0B".str

    val binary_num
        =  (binary_prefix .. (bit.repeat1 around1 underscores)).end

    val octal_num
        =  ("0".str .. (underscores .. !"octal_digit").repeat1).end

    val decimal_num
        = ("0".str / digits1).end

    val integer_num
        = (hex_num / binary_num / octal_num / decimal_num).end

    val integer_literal
        = (integer_num .. "lL".set.opt)
        .token("parse_int(it)")

    //// Characters and Strings ------------------------------------------------
    val `Characters and Strings` = section(2)

    val octal_escape = (
        (('0' upto '3') .. !"octal_digit" .. !"octal_digit") /
        (!"octal_digit" .. (!"octal_digit").opt)
    ).end

    val unicode_escape
        =  ("u".str.repeat1 .. (!"hex_digit").repeat(4)).end

    val escape
        =  ("\\".str .. ("btnfr\"'\\".set / octal_escape / unicode_escape)).end

        val naked_char
        = (escape / ("'\\\n\r".set.not .. !"char_any")).end

    val char_literal
        = ("'".str .. naked_char .. "'".str)
        .token("parse_char(it)")

    val naked_string_char
        = (escape / ("\"\\\n\r".set.not .. !"char_any")).end

    val string_literal
        = ("\"".str .. naked_string_char.repeat0 .. "\"".str)
        .token("parse_string(it)")

    //// Literal ---------------------------------------------------------------
    val `Literal` = section(2)

    val literal_syntax = token_choice(
        integer_literal,
        string_literal,
        `null`,
        float_literal,
        `true`,
        `false`,
        char_literal)

    val literal
        = literal_syntax.build("Literal(it(0))")

    /// ANNOTATIONS ================================================================================
    val ANNOTATIONS = section(1)

    val annotation_element
        = (!"ternary" / !"annotation_element_list" / !"annotation")
        .with(TypeHint)

    val annotation_inner_list
        = (!"annotation_element").comma_list_term0

    val annotation_element_list
        = annotation_inner_list.curlies
        .build("AnnotationElementList(it.list())")

    val annotation_element_pair
        = (iden .. `=` ..annotation_element)
        .build ("Pair<String, AnnotationElement>(it(0), it(1))")

    val normal_annotation_suffix
        = annotation_element_pair.comma_list1.parens
        .build (1, "NormalAnnotation(it(0), it.list<Pair<String, AnnotationElement>>(1))")

    val single_element_annotation_suffix
        = annotation_element.parens
        .build (1, "SingleElementAnnotation(it(0), it(1))")

    val marker_annotation_suffix
        = parens.opt
        .build (1, "MarkerAnnotation(it(0))")

    val annotation_suffix = (
        normal_annotation_suffix /
        single_element_annotation_suffix /
        marker_annotation_suffix
    ).end

    val qualified_iden
        = (iden around1 dot)
        .build ("it.list<String>()")

    val annotation
        = (`@` .. qualified_iden .. annotation_suffix).end

    val annotations
        = annotation.repeat0
        .build ("it.list<Annotation>()")

    /// TYPES ======================================================================================
    val TYPES = section(1)

    val basic_type
        = token_choice(`byte`, `short`, `int`, `long`, `char`, `float`, `double`, `boolean`, `void`)

    val primitive_type
        = (annotations .. basic_type)
        .build ("PrimitiveType(it(0), it(1))")

    val extends_bound
        = (`extends` .. !"type")
        .build ("ExtendsBound(it(0))")

    val super_bound
        = (`super` .. !"type")
        .build ("SuperBound(it(0))")

    val type_bound
        = (extends_bound / super_bound).maybe

    val wildcard
        = (annotations .. `?` .. type_bound)
        .build ("Wildcard(it(0), it(1))")

    val type_args
        = (!"type" / wildcard).comma_list0.angles.opt
        .build ("it.list<Type>()")

    val class_type_part
        = (annotations .. iden .. type_args)
        .build ("ClassTypePart(it(0), it(1), it(2))")

    val class_type
        = (class_type_part around1 dot)
        .build ("ClassType(it.list<ClassTypePart>())")

    val stem_type
        = (primitive_type / class_type).end

    val dim
        = (annotations .. squares)
        .build ("Dimension(it(0))")

    val dims
        = dim.repeat0
        .build ("it.list<Dimension>()")

    val dims1
        = dim.repeat1
        .build ("it.list<Dimension>()")

    val type_dim_suffix
        = dims1
        .build (1, "ArrayType(it(0), it(1))")

    val type
        = (stem_type .. type_dim_suffix.opt).end
        .with(TypeHint)

    val type_union_syntax
        = !"type" around1 `&`

    val type_union
        = type_union_syntax
        .build ("it.list<Type>()")

    val type_bounds
        = (`extends` .. type_union_syntax).opt
        .build ("it.list<Type>()")

    val type_param
        = (annotations .. iden .. type_bounds)
        .build ("TypeParam(it(0), it(1), it(2))")

    val type_params
        = type_param.comma_list0.angles.opt
        .build ("it.list<TypeParam>()")

    /// MODIFIERS ==================================================================================
    val MODIFIERS = section(1)

    val keyword_modifier = (
        public /
        protected /
        private /
        abstract /
        static /
        final /
        synchronized /
        native /
        strictfp /
        default /
        transient /
        volatile
    )
        .build ("Keyword.valueOf(it(0))")

    val modifier
        = (annotation / keyword_modifier).end

    val modifiers
        = modifier.repeat0
        .build ("it.list<Modifier>()")

    /// PARAMETERS =================================================================================
    val PARAMETERS = section(1)

    val args
        = (!"expr").comma_list0.parens
        .build ("it.list<Expr>()")

    val this_parameter_qualifier
        = (iden .. dot).repeat0
        .build ("it.list<String>()")

    val this_param_suffix
        = (this_parameter_qualifier .. `this`)
        .build (2, "ThisParameter(it(0), it(1), it(2))")

    val iden_param_suffix
        = (iden .. dims)
        .build (2, "IdenParameter(it(0), it(1), it(2), it(3))")

    val variadic_param_suffix
        = (annotations .. ellipsis .. iden)
        .build (2, "VariadicParameter(it(0), it(1), it(2), it(3))")

    val formal_param_suffix = (
        iden_param_suffix /
        this_param_suffix /
        variadic_param_suffix
    ).end

    val formal_param
        = (modifiers .. type .. formal_param_suffix).end

    val formal_params
        = formal_param.comma_list0.parens
        .build ("FormalParameters(it.list())")

    val untyped_params
        = iden.comma_list1.parens
        .build ("UntypedParameters(it.list())")

    val single_param
        = iden
        .build ("UntypedParameters(it.list<String>())")

    val lambda_params
        = (formal_params / untyped_params / single_param).end

    ///  NON-TYPE DECLARATIONS =====================================================================
    val `NON-TYPE DECLARATIONS` = section(1)

    val var_init
        = (!"expr" / !"array_init")
        .with(TypeHint)

    val array_init
        = var_init.comma_list_term0.curlies
        .build ("ArrayInit(it.list())")

    val var_declarator_id
        = (iden .. dims)
        .build ("VarDeclaratorID(it(0), it(1))")

    val var_declarator
        = (var_declarator_id.. (`=` .. var_init).maybe)
        .build ("VarDeclarator(it(0), it(1))")

    val var_decl_no_semi
        = (type .. var_declarator.comma_list1)
        .build (1, "VarDecl(it(0), it(1), it.list(2))")

    val var_decl_suffix
        = (var_decl_no_semi .. semi).end

    val var_decl
        = (modifiers .. var_decl_suffix).end

    val throws_clause
        = (throws .. type.comma_list1).opt
        .build ("it.list<Type>()")

    val block_or_semi
        = (!"block" / semi.as_val(null)).end

    val method_decl_suffix
        = (type_params .. type .. iden .. formal_params .. dims .. throws_clause .. block_or_semi)
        .build (1, "MethodDecl(it(0), it(1), it(2), it(3), it(4), it(5), it(6), it(7))")

    val constructor_decl_suffix
        = (type_params .. iden .. formal_params .. throws_clause .. !"block")
        .build (1, "ConstructorDecl(it(0), it(1), it(2), it(3), it(4), it(5))")

    val init_block
        = (`static`.as_bool .. !"block")
        .build ("InitBlock(it(0), it(1))")

    ///  TYPE DECLARATIONS =========================================================================
    val `TYPE DECLARATIONS` = section(1)

    //// Common ----------------------------------------------------------------
    val `Common` = section(2)

    val extends_clause
        = (`extends` .. type.comma_list0).opt
        .build ("it.list<Type>()")

    val implements_clause
        = (`implements` .. type.comma_list0).opt
        .build ("it.list<Type>()")

    val type_sig
        = (iden .. type_params .. extends_clause .. implements_clause).end

    val class_modified_decl
        = (modifiers .. (var_decl_suffix / method_decl_suffix / constructor_decl_suffix / !"type_decl_suffix")).end

    val class_body_decl
        = (class_modified_decl / init_block / semi)
        .with(TypeHint)

    val class_body_decls
        = class_body_decl.repeat0
        .build ("it.list<Decl>()")

    val type_body
        =  class_body_decls.curlies

    //// Enum ------------------------------------------------------------------
    val `Enum` = section(2)

    val enum_constant
        = (annotations .. iden .. args.maybe .. type_body.maybe)
        .build ("EnumConstant(it(0), it(1), it(2), it(3))")

    val enum_class_decls
        = (semi .. class_body_decl.repeat0).opt
        .build ("it.list<Decl>()")

    val enum_constants
        = enum_constant.comma_list1.opt
        .build ("it.list<EnumConstant>()")

    val enum_body
        = (enum_constants .. enum_class_decls).curlies
        .affect("stack.push(it(1)) ; stack.push(it(0)) /* swap */")

    val enum_decl
        = (`enum` .. type_sig .. enum_body)
        .build (1,
            "val td = TypeDecl(ENUM, it(0), it(1), it(2), it(3), it(4), it(5))\n" +
            "EnumDecl(td, it(6))")

    //// Annotations -----------------------------------------------------------
    val `Annotations` = section(2)

    val annot_default_clause
        = (`default` .. annotation_element)
        .build ("it(1)")

    val annot_elem_decl
        = (modifiers .. type .. iden .. parens .. dims .. annot_default_clause.maybe .. semi)
        .build ("AnnotationElemDecl(it(0), it(1), it(2), it(3), it(4))")

    val annot_body_decls
        = (annot_elem_decl / class_body_decl).repeat0
        .build ("it.list<Decl>()")

    val annotation_decl
        = (`@` .. `interface` .. type_sig .. annot_body_decls.curlies)
        .build (1, "TypeDecl(ANNOTATION, it(0), it(1), it(2), it(3), it(4), it(5))")

    //// -----------------------------------------------------------------------
    val s1 = separator(2)

    val class_decl
        = (`class` .. type_sig .. type_body)
        .build (1, "TypeDecl(CLASS, it(0), it(1), it(2), it(3), it(4), it(5))")

    val interface_declaration
        = (`interface` .. type_sig .. type_body)
        .build (1, "TypeDecl(INTERFACE, it(0), it(1), it(2), it(3), it(4), it(5))")

    val type_decl_suffix = (
         class_decl /
         interface_declaration /
         enum_decl /
         annotation_decl
    ).end

    val type_decl
        = (modifiers .. type_decl_suffix).end

    val type_decls
        = (type_decl / semi).repeat0
        .build ("it.list<Decl>()")

    /// EXPRESSIONS ================================================================================
    val EXPRESSIONS = section(1)

    //// Array Constructor -----------------------------------------------------
    val `Array Constructor` = section(2)

    val dim_expr
        = (annotations .. (!"expr").squares)
        .build ("DimExpr(it(0), it(1))")

    val dim_exprs
        = dim_expr.repeat1
        .build ("it.list<DimExpr>()")

    val dim_expr_array_creator
        = (stem_type .. dim_exprs .. dims)
        .build ("ArrayCtorCall(it(0), it(1), it(2), null)")

    val init_array_creator
        = (stem_type .. dims1 .. array_init)
        .build ("ArrayCtorCall(it(0), emptyList(), it(1), it(2))")

    val array_ctor_call
        =  (`new` .. (dim_expr_array_creator / init_array_creator)).end

    //// Lambda Expression -----------------------------------------------------
    val `Lambda Expression` = section(2)

    val lambda
        = (lambda_params .. arrow .. (!"block" / !"expr"))
        .build ("Lambda(it(0), it(1))")

    //// Expression - Primary --------------------------------------------------
    val `Expression - Primary` = section(2)

    val par_expr
        =  (!"expr").parens
        .build ("ParenExpr(it(0))")

    val ctor_call
        = (`new` .. type_args .. stem_type .. args .. type_body.maybe)
        .build ("CtorCall(it(0), it(1), it(2), it(3))")

    val new_ref_suffix
        = `new`
        .build (2, "NewReference(it(0), it(1))")

    val method_ref_suffix
        = iden
        .build (2, "MaybeBoundMethodReference(it(0), it(1), it(2))")

    val ref_suffix
        = (dcolon .. type_args .. (new_ref_suffix / method_ref_suffix)).end

    val class_expr_suffix
        = (dot .. `class`)
        .build (1, "ClassExpr(it(0))")

    val type_suffix_expr
        = type .. (ref_suffix / class_expr_suffix)

    val iden_or_method_expr
        = (iden .. args.maybe)
        .build ("it[1] ?. let { MethodCall(null, listOf(), it(0), it(1)) } ?: Identifier(it(0))")

    val this_expr
        = (`this` .. args.maybe)
        .build ("it[0] ?. let { ThisCall(it(0)) } ?: This")

    val super_expr
        = (`super` .. args.maybe)
        .build ("it[0] ?. let { SuperCall(it(0)) } ?: Super")

    val class_expr
        = (type .. dot .. `class`)
        .build ("ClassExpr(it(0))")

    val primary_expr = (
        par_expr /
        array_ctor_call /
        ctor_call /
        type_suffix_expr /
        iden_or_method_expr /
        this_expr /
        super_expr /
        literal
    ).end

    //// Expression - Postfix --------------------------------------------------
    val `Expression - Postfix` = section(2)

    val dot_this
        = `this`
        .build (1, "DotThis(it(0))")

    val dot_super
        = `super`
        .build (1, "DotSuper(it(0))")

    val dot_iden
        = iden
        .build (1, "DotIden(it(0), it(1))")

    val dot_new
        = ctor_call
        .build (1, "DotNew(it(0), it(1))")

    val dot_method
        = (type_args .. iden .. args)
        .build (1, "MethodCall(it(0), it(1), it(2), it(3))")

    val dot_postfix = (
        dot_method /
        dot_iden /
        dot_this /
        dot_super /
        dot_new
    ).end

    val ref_postfix
        = (dcolon .. type_args .. iden)
        .build (1, "BoundMethodReference(it(0), it(1), it(2))")

    val array_postfix
        = (!"expr").squares
        .build (1, "ArrayAccess(it(0), it(1))")

    val inc_suffix
        = `++`
        .build (1, "PostIncrement(it(0))")

    val dec_suffix
        = `--`
        .build (1, "PostDecrement(it(0))")

    val postfix = (
        (dot .. dot_postfix) /
        array_postfix /
        inc_suffix /
        dec_suffix /
        ref_postfix
    ).end

    val postfix_expr
        = (primary_expr .. postfix.repeat0).end

    val inc_prefix
        = (`++` .. !"prefix_expr")
        .build ("PreIncrement(it(0))")

    val dec_prefix
        = (`--` .. !"prefix_expr")
        .build ("PreDecrement(it(0))")

    val unary_plus
        = (`+`  .. !"prefix_expr")
        .build ("UnaryPlus(it(0))")

    val unary_minus
        = (`-`  .. !"prefix_expr")
        .build ("UnaryMinus(it(0))")

    val complement
        = (`~`  .. !"prefix_expr")
        .build ("Complement(it(0))")

    val not
        = (`!`  .. !"prefix_expr")
        .build ("Not(it(0))")

    val cast
        = (type_union.parens .. (lambda / !"prefix_expr"))
        .build ("Cast(it(0), it(1))")

    val prefix_expr = (
        inc_prefix /
        dec_prefix /
        unary_plus /
        unary_minus /
        complement /
        not /
        cast /
        postfix_expr
    ).with(TypeHint)

    //// Expression - Binary ---------------------------------------------------
    val `Expression - Binary` = section(2)

    val mult_expr = assoc_left {
        operands = prefix_expr
        op(`*`).effect("Product(it(0), it(1))")
        op(div).effect("Division(it(0), it(1))")
        op(`%`).effect("Remainder(it(0), it(1))")
    }

    val add_expr = assoc_left {
        operands = mult_expr
        op(`+`).effect("Sum(it(0), it(1))")
        op(`-`).effect("Diff(it(0), it(1))")
    }

    val shift_expr = assoc_left {
        operands = add_expr
        op(sl).effect("ShiftLeft(it(0), it(1))")
        op(sr).effect("ShiftRight(it(0), it(1))")
        op(bsr).effect("BinaryShiftRight(it(0), it(1))")
    }

    val order_expr = assoc_left {
        operands = shift_expr
        op(lt).effect("Lower(it(0), it(1))")
        op(le).effect("LowerEqual(it(0), it(1))")
        op(gt).effect("Greater(it(0), it(1))")
        op(ge).effect("GreaterEqual(it(0), it(1))")
        postfix(instanceof .. type).effect("Instanceof(it(0), it(1))")
    }

    val eq_expr = assoc_left {
        operands = order_expr
        op(`==`).effect("Equal(it(0), it(1))")
        op(`!=`).effect("NotEqual(it(0), it(1))")
    }

    val binary_and_expr = assoc_left {
        operands = eq_expr
        op(`&`).effect("BinaryAnd(it(0), it(1))") }

    val xor_expr = assoc_left {
        operands = binary_and_expr
        op(`^`).effect("Xor(it(0), it(1))") }

    val binary_or_expr = assoc_left {
        operands = xor_expr
        op(`|`).effect("BinaryOr(it(0), it(1))") }

    val and_expr = assoc_left {
        operands = binary_or_expr
        op(`&&`).effect("And(it(0), it(1))") }

    val or_expr = assoc_left {
        operands = and_expr
        op(`||`).effect("Or(it(0), it(1))") }

    val ternary_suffix
        = (`?` .. !"expr" .. colon .. !"expr")
        .build("Ternary(it(0), it(1), it(2))")

    val ternary
        = (or_expr .. ternary_suffix.opt).end

    val assignment_suffix = (
          (`=`   .. !"expr").build("Assign(it(0), it(1), \"=\")")
        / (`+=`  .. !"expr").build("Assign(it(0), it(1), \"+=\")")
        / (`-=`  .. !"expr").build("Assign(it(0), it(1), \"-=\")")
        / (`*=`  .. !"expr").build("Assign(it(0), it(1), \"*=\")")
        / (dive  .. !"expr").build("Assign(it(0), it(1), \"/=\")")
        / (`%=`  .. !"expr").build("Assign(it(0), it(1), \"%=\")")
        / (sle   .. !"expr").build("Assign(it(0), it(1), \"<<=\")")
        / (sre   .. !"expr").build("Assign(it(0), it(1), \">>=\")")
        / (bsre  .. !"expr").build("Assign(it(0), it(1), \">>>=\")")
        / (`&=`  .. !"expr").build("Assign(it(0), it(1), \"&=\")")
        / (`^=`  .. !"expr").build("Assign(it(0), it(1), \"^=\")")
        / (`|=`  .. !"expr").build("Assign(it(0), it(1), \"|=\")")
    ).end

    val assignment
        = (ternary .. assignment_suffix.opt).end
    
    val expr
        = (lambda / assignment)
        .with(TypeHint)

    /// STATEMENTS =================================================================================
    val STATEMENTS = section(1)

    val if_stmt
        = (`if` .. par_expr .. !"stmt" .. (`else` .. !"stmt").maybe)
        .build ("If(it(0), it(1), it(2))")

    val expr_stmt_list
        = expr.comma_list0
        .build ("it.list<Stmt>()")

    val for_init_decl
        = (modifiers .. var_decl_no_semi)
        .build ("it.list<Stmt>()")

    val for_init
        = (for_init_decl / expr_stmt_list).end

    val basic_for_paren_part
        = (for_init .. semi .. expr.maybe .. semi .. expr_stmt_list.opt).end

    val basic_for_stmt
        = ( `for` .. basic_for_paren_part.parens .. !"stmt")
        .build ("BasicFor(it(0), it(1), it(2), it(3))")

    val for_val_decl
        = (modifiers .. type .. var_declarator_id .. colon .. expr).end

    val enhanced_for_stmt
        = ( `for` .. for_val_decl.parens .. !"stmt")
        .build ("EnhancedFor(it(0), it(1), it(2), it(3), it(4))")

    val while_stmt
        = ( `while` .. par_expr .. !"stmt")
        .build ("WhileStmt(it(0), it(1))")

    val do_while_stmt
        = ( `do` .. !"stmt" .. `while` .. par_expr .. semi)
        .build ("DoWhileStmt(it(0), it(1))")

    val catch_parameter_types
        = (type around0 `|`)
        .build ("it.list<Type>()")

    val catch_parameter
        = (modifiers .. catch_parameter_types .. var_declarator_id).end

    val catch_clause
        = (`catch` .. catch_parameter.parens .. !"block")
        .build ("CatchClause(it(0), it(1), it(2), it(3))")

    val catch_clauses
        = catch_clause.repeat0
        .build ("it.list<CatchClause>()")

    val finally_clause
        = (`finally` .. !"block").end

    val resource
        = (modifiers .. type .. var_declarator_id .. `=` .. expr)
        .build ("TryResource(it(0), it(1), it(2), it(3))")

    val resources
        = (resource around1 semi).parens.opt
        .build ("it.list<TryResource>()")

    val try_stmt
        = (`try` .. resources .. !"block" .. catch_clauses .. finally_clause.maybe)
        .build ("TryStmt(it(0), it(1), it(2), it(3))")

    val default_label
        = (default .. colon)
        .build ("DefaultLabel")

    val case_label
        = (`case` .. expr .. colon)
        .build ("CaseLabel(it(0))")

    val switch_label
        = (case_label / default_label).end

    val switch_clause
        = (switch_label .. !"stmts")
        .build ("SwitchClause(it(0), it(1))")

    val switch_stmt
        = (`switch` .. par_expr .. switch_clause.repeat0.curlies)
        .build ("SwitchStmt(it(0), it.list(1))")

    val synchronized_stmt
        = (`synchronized` .. par_expr .. !"block")
        .build ("SynchronizedStmt(it(1), it(2))")

    val return_stmt
        = (`return` .. expr.maybe .. semi)
        .build ("ReturnStmt(it(0))")

    val throw_stmt
        = (`throw` .. expr .. semi)
        .build ("ThrowStmt(it(0))")

    val break_stmt
        = (`break` .. iden.maybe .. semi)
        .build ("BreakStmt(it(0))")

    val continue_stmt
        = (`continue` .. iden.maybe .. semi)
        .build ("ContinueStmt(it(0))")

    val assert_stmt
        = (`assert` .. expr .. (colon .. expr).maybe .. semi)
        .build ("AssertStmt(it(0), it(1))")

    val semi_stmt
        = semi
        .build ("SemiStmt")

    val expr_stmt
        = (expr .. semi).end

    val labelled_stmt
        = (iden .. colon .. !"stmt")
        .build ("LabelledStmt(it(0), it(1))")

    val stmt = (
            !"block" /
            if_stmt /
            basic_for_stmt /
            enhanced_for_stmt /
            while_stmt /
            do_while_stmt /
            try_stmt /
            switch_stmt /
            synchronized_stmt /
            return_stmt /
            throw_stmt /
            break_stmt /
            continue_stmt /
            assert_stmt /
            semi_stmt /
            expr_stmt /
            labelled_stmt /
            var_decl /
            type_decl
    ).with(TypeHint)

    val block
        = stmt.repeat0.curlies
        .build ("Block(it.list())")

    val stmts
        = stmt.repeat0
        .build ("it.list<Stmt>()")

    ///  TOP-LEVEL =================================================================================
    val `TOP-LEVEL` = section(1)

    val package_decl
        = (annotations .. `package` .. qualified_iden .. semi)
        .build ("Package(it(0), it(1))")

    val import_decl
        = (`import` .. `static`.as_bool .. qualified_iden .. (dot .. `*`).as_bool .. semi)
        .build ("Import(it(0), it(1), it(2))")

    val import_decls
        = import_decl.repeat0
        .build ("it.list<Import>()")

    val root
        = (!"whitespace" .. package_decl.maybe .. import_decls .. type_decls)
        .build ("File(it(0), it(1), it(2))")
}