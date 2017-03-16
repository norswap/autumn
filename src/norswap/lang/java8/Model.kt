package norswap.lang.java8
import norswap.autumn.model.*

class Java8Model
{
    /// LEXICAL ====================================================================================
    val LEXICAL = section(1)

    // Whitespace

    val line_comment
        =  "//".str .. (!"char_any" until0 "\n".str)

    val multi_comment
        =  "/*".str .. (!"char_any" until0 "*/".str)

    val whitespace
        = (!"space_char" / line_comment / multi_comment).repeat0

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

    // Identifiers (must come after keywords)

    val iden = (!"java_iden").token

    val `_`
        = "_".str

    val dlit
        = ".".str

    val hex_prefix
        =  "0x".str / "0x".str

    val underscores
        = `_`.repeat0

    val digits1
        = !"digit" around0 underscores

    val digits0
        = !"digit" around0 underscores

    val hex_digits
        = !"hex_digit" around1 underscores

    val hex_num
        = hex_prefix .. hex_digits

    // Numerals: floating point

    val hex_significand =
        (hex_prefix .. hex_digits.opt .. dlit .. hex_digits) / (hex_num .. dlit.opt)

    val exp_sign_opt
        = "+-".set.opt

    val exponent
        =  "eE".set ..exp_sign_opt .. digits1

    val binary_exponent
        =  "pP".set .. exp_sign_opt .. digits1

    val float_suffix
        = "fFdD".set

    val float_suffix_opt
        = float_suffix.opt

    val hex_float_lit
        =  hex_significand .. binary_exponent .. float_suffix_opt

    val decimal_float_lit =
        (digits1 .. dlit .. digits0.. exponent.opt .. float_suffix_opt) /
        (dlit .. digits1.. exponent.opt .. float_suffix_opt) /
        (digits1 .. exponent .. float_suffix_opt) /
        (digits1 .. exponent.opt ..float_suffix)

    val float_literal
        = (hex_float_lit / decimal_float_lit)
        .token("parse_float(it)")

    // Numerals: integral

    val bit
        = "01".set

    val binary_prefix
        = "0b".str / "0B".str

    val binary_num
        =  binary_prefix .. (bit.repeat1 around1 underscores)

    val octal_num
        =  "0".str.. (underscores .. !"octal_digit").repeat1

    val decimal_num
        = "0".str / digits1

    val integer_num
        = hex_num / binary_num / octal_num / decimal_num

    val integer_literal
        = (integer_num .. "lL".set.opt)
        .token("parse_int(it)")

    // Characters and Strings

    val octal_escape =
        (('0' upto '3') .. !"octal_digit" .. !"octal_digit") /
        (!"octal_digit" .. (!"octal_digit").opt)

    val unicode_escape
        =  "u".str.repeat1 .. (!"hex_digit").repeat(4)

    val escape
        =  "\\".str .. ("btnfr\"'\\".set / octal_escape / unicode_escape)

        val naked_char
        = escape / ("'\\\n\r".set.not .. !"char_any")

    val char_literal
        = ("'".str .. naked_char .. "'".str)
        .token("parse_char(it)")

    val naked_string_char
        = escape / ("\"\\\n\r".set.not .. !"char_any")

    val string_literal
        = ("\"".str .. naked_string_char.repeat0 .. "\"".str)
        .token("parse_string(it)")

    // Literal

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
        .build (1, "NormalAnnotation(it(0), it.list<Pair<String, AnnotationElement>>(1)))")

    val single_element_annotation_suffix
        = annotation_element.parens
        .build (1, "SingleElementAnnotation(it(0), it(1))")

    val marker_annotation_suffix
        = parens.opt
        .build (1, "MarkerAnnotation(it(0))")

    val annotation_suffix =
        normal_annotation_suffix /
        single_element_annotation_suffix /
        marker_annotation_suffix

    val qualified_iden
        = (iden around1 dot)
        .build ("it.list<String>()")

    val annotation
        = `@` .. qualified_iden .. annotation_suffix

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
        .build ("Wildcard(it(0), it(1)")

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
        = primitive_type / class_type

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
        = (stem_type .. type_dim_suffix.opt)
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
        = annotation / keyword_modifier

    val modifiers
        = modifier.repeat0
        .build ("it.list<Modifier>()")

    /// PARAMETERS =================================================================================
    val PARAMETERS = section(1)

    val args
        = (`(` .. (!"expr" around0 `,`) .. `)`)
        .build ("it.list<Expr>()")

    val thisParameterQualifier
        = (iden .. dot).repeat0
        .build ("it.list<String>()")

    val thisParamSuffix
        = (thisParameterQualifier .. `this`)
        .build (2, "ThisParameter(it(0), it(1), it(2))")

    val idenParamSuffix
        = (iden .. dims)
        .build (2, "IdenParameter(it(0), it(1), it(2), it(3))")

    val variadicParamSuffix
        = (annotations .. ellipsis .. iden)
        .build (2, "VariadicParameter(it(0), it(1), it(2), it(3))")

    val formalParamSuffix =
        idenParamSuffix /
        thisParamSuffix /
        variadicParamSuffix

    val formalParam
        =  modifiers .. type .. formalParamSuffix

    val formalParams
        = (`(` .. (formalParam around0 `,`) .. `)`)
        .build ("FormalParameters(it.list())")

    val untypedParams
        = (`(` .. (iden around1 `,`) .. `)`)
        .build ("UntypedParameters(it.list())")

    val singleParam
        = iden
        .build ("UntypedParameters(listOf(get<String>()))")

    val lambdaParams
        = formalParams / untypedParams / singleParam

    ///  NON-TYPE DECLARATIONS =====================================================================
    val `NON-TYPE DECLARATIONS` = section(1)

    val varInit
        = (!"expr" / !"arrayInit")
        .with(TypeHint)

    val arrayInit
        = (`{` .. (varInit around0 `,`) .. `,`.opt .. `}`)
        .build ("ArrayInit(it.list())")

    val varDeclaratorID
        = (iden .. dims)
        .build ("VarDeclaratorID(it(0), it(1))")

    val varDeclarator
        = (varDeclaratorID .. (`=` .. varInit).maybe)
        .build ("VarDeclarator(it(0), it(1)")

    val varDeclNoSemi
        = (type .. (varDeclarator around1 `,`))
        .build (1, "VarDecl(it(0), it(1), it.list(2))")

    val varDeclSuffix
        = varDeclNoSemi .. semi

    val varDecl
        =  modifiers .. varDeclSuffix

    val throwsClause
        = (throws .. (type around0 `,`)).opt
        .build ("it.list<Type>()")

    val methodDeclSuffix
        = (type_params.. type .. iden .. formalParams .. dims .. throwsClause .. (!"block" / semi).maybe)
        .build (1, "MethodDecl(it(0), it(1), it(2), it(3), it(4), it(5), it(6), it(7))")

    val constructorDeclSuffix
        = (type_params.. iden .. formalParams .. throwsClause .. !"block")
        .build (1, "ConstructorDecl(it(0), it(1), it(2), it(3), it(4), it(5))")

    val initBlock
        = (`static`.as_bool .. !"block")
        .build ("InitBlock(it(0), it(1))")

    ///  TYPE DECLARATIONS =========================================================================
    val `TYPE DECLARATIONS` = section(1)

    // Common -----------------------------------------------------------------

    val extendsClause
        = (`extends` .. (type around0 `,`)).opt
        .build ("it.list<Type>()")

    val implementsClause
        = (`implements` .. (type around0 `,`)).opt
        .build ("it.list<Type>()")

    val typeSig
        =  iden ..type_params.. extendsClause .. implementsClause

    val classModifiedDecl
        = modifiers .. (varDeclSuffix / methodDeclSuffix / constructorDeclSuffix / !"typeDeclSuffix")

    val classBodyDecl =
         classModifiedDecl /
         initBlock /
         semi

    val classBodyDecls
        = classBodyDecl.repeat0
        .build ("it.list<Decl>()")

    val typeBody
        =  `{` .. classBodyDecls .. `}`

    // Enum -------------------------------------------------------------------

    val enumConstant
        = (annotations .. iden .. args.maybe .. typeBody.maybe)
        .build ("EnumConstant(it(0), it(1), it(2), it(3))")

    val enumClassDecls
        = (semi .. classBodyDecl.repeat0).opt
        .build ("it.list<Decl>()")

    val enumConstants
        = ((enumConstant around1 `,`) .. `,`.opt).opt
        .build ("it.list<EnumConstant>()")

    val enumBody
        = (`{` .. enumConstants .. enumClassDecls .. `}`)
        .affect { stack.push(it(1)) ; stack.push(it(0)) /* swap */ }

    val enumDecl
        = (`enum` .. typeSig .. enumBody)
        .build (1,
            "val td = TypeDecl(ENUM, it(0), it(1), it(2), it(3), it(4), it(5))\n" +
            "EnumDecl(td, it(6))")

    // Annotations ------------------------------------------------------------

    val annotDefaultClause
        = (`default` ..annotation_element)
        .build ("get(1)")

    val annotElemDecl
        = (modifiers .. type .. iden .. `(` .. `)` .. dims .. annotDefaultClause.maybe .. semi)
        .build ("AnnotationElemDecl(it(0), it(1), it(2), it(3), it(4))")

    val annotBodyDecls
        = (annotElemDecl / classBodyDecl).repeat0
        .build ("it.list<Decl>()")

    val annotationDecl
        = (`@` .. `interface` .. typeSig .. `{` .. annotBodyDecls .. `}`)
        .build (1, "TypeDecl(ANNOTATION, it(0), it(1), it(2), it(3), it(4), it(5))")

    // ------------------------------------------------------------------------

    val classDecl
        = (`class` .. typeSig .. typeBody)
        .build (1, "TypeDecl(CLASS, it(0), it(1), it(2), it(3), it(4), it(5))")

    val interfaceDeclaration
        = (`interface` .. typeSig .. typeBody)
        .build (1, "TypeDecl(INTERFACE, it(0), it(1), it(2), it(3), it(4), it(5))")

    val typeDeclSuffix =
         classDecl /
         interfaceDeclaration /
         enumDecl /
         annotationDecl

    val typeDecl
        = modifiers .. typeDeclSuffix

    val typeDecls
        = (typeDecl / semi).repeat0
        .build ("it.list<Decl>()")

    /// EXPRESSIONS ================================================================================
    val EXPRESSIONS = section(1)

    // Array Constructor ------------------------------------------------------

    val initArrayCreator
        = (stem_type.. dims1 .. arrayInit)
        .build ("ArrayCtorCall(it(0), emptyList(), it(1), it(2))")

    val dimExpr
        = (annotations .. lsbra .. !"expr" .. rsbra)
        .build ("DimExpr(it(0), it(1))")

    val dimExprs
        = dimExpr.repeat1
        .build ("it.list<DimExpr>()")

    val dimExprArrayCreator
        = (stem_type.. dimExprs .. dims)
        .build ("ArrayCtorCall(it(0), it(1), it(2), null)")

    val arrayCtorCall
        =  `new` .. (dimExprArrayCreator / initArrayCreator)

    // Lambda Expression ------------------------------------------------------

    val lambda
        = (lambdaParams .. arrow .. (!"block" / !"expr"))
        .build ("Lambda(it(0), it(1))")

    // Expression: Primary ----------------------------------------------------

    val ctorCall
        = (`new` ..type_args..stem_type.. args.. typeBody.maybe)
        .build ("CtorCall(it(0), it(1), it(2), it(3))")

    val superExpr
        = (`super` .. args.maybe)
        .build ("it[0] ?. let { SuperCall(it(0)) } ?: Super")

    val thisExpr
        = (`this` .. args.maybe)
        .build ("it[0] ?. let { ThisCall(it(0)) } ?: This")

    val idenOrMethodExpr
        = (iden .. args.maybe)
        .build ("it[1] ?. let { MethodCall(null, listOf(), it(0), it(1)) } ?: Identifier(it(0))")

    val classExpr
        = (type .. dot .. `class`)
        .build ("ClassExpr(it(0))")

    val parExpr
        =  `(` .. !"expr" .. `)`

    val methodRef
        = (type .. dcolon ..type_args.. iden)
        .build ("MaybeBoundMethodReference(it(0), it(1), it(2))")

    val newRef
        = (type .. dcolon ..type_args.. `new`)
        .build ("NewReference(it(0), it(1))")

    val newRefSuffix
        = `new`
        .build (2, "NewReference(it(0), it(1))")

    val methodRefSuffix
        = iden
        .build (2, "MaybeBoundMethodReference(it(0), it(1), it(2))")

    val refSuffix
        = dcolon ..type_args.. (newRefSuffix / methodRefSuffix)

    val classExprSuffix
        = (dot .. `class`)
        .build (1, "ClassExpr(it(0))")

    val typeSuffixExpr
        = type .. (refSuffix / classExprSuffix)

    val primaryExpr =
        parExpr /
        arrayCtorCall /
        ctorCall /
        typeSuffixExpr /
        idenOrMethodExpr /
        thisExpr /
        superExpr /
        literal

    // Expression: Postfix ----------------------------------------------------

    val dotThis
        = `this`
        .build (1, "DotThis(it(0))")

    val dotSuper
        = `super`
        .build (1, "DotSuper(it(0))")

    val dotIden
        = iden
        .build (1, "DotIden(it(0), it(1))")

    val dotNew
        = ctorCall
        .build (1, "DotNew(it(0), it(1))")

    val dotMethod
        = (type_args.. iden .. args)
        .build (1, "MethodCall(it(0), it(1), it(2), it(3))")

    val dotPostfix =
        dotMethod /
        dotIden /
        dotThis /
        dotSuper /
        dotNew

    val refPostfix
        = (dcolon ..type_args.. iden)
        .build (1, "BoundMethodReference(it(0), it(1), it(2))")

    val arrayPostfix
        = (lsbra .. !"expr" .. rsbra)
        .build (1, "ArrayAccess(it(0), it(1))")

    val incSuffix
        = `++`
        .build (1, "PostIncrement(it(0))")

    val decSuffix
        = `--`
        .build (1, "PostDecrement(it(0))")

    val postfix =
      dot .. dotPostfix /
      arrayPostfix /
      incSuffix /
      decSuffix /
      refPostfix

    val postfixExpr
        =  primaryExpr .. postfix.repeat0

    val incPrefix
        = (`++` .. !"prefix_expr")
        .build ("PreIncrement(it(0))")

    val decPrefix
        = (`--` .. !"prefix_expr")
        .build ("PreDecrement(it(0))")

    val unaryPlus
        = (`+`  .. !"prefix_expr")
        .build ("UnaryPlus(it(0))")

    val unaryMinus
        = (`-`  .. !"prefix_expr")
        .build ("UnaryMinus(it(0))")

    val complement
        = (`~`  .. !"prefix_expr")
        .build ("Complement(it(0))")

    val not
        = (`!`  .. !"prefix_expr")
        .build ("Not(it(0))")

    val cast
        = (`(` ..type_union.. `)` .. (lambda / !"prefix_expr"))
        .build ("Cast(it(0), it(1))")

    val prefix_expr = (
        incPrefix /
        decPrefix /
        unaryPlus /
        unaryMinus /
        complement /
        not /
        cast /
        postfixExpr
    ).with(TypeHint)

    val expr = "".str
    // .with(TypeHint)

    /// STATEMENTS =================================================================================
    val STATEMENTS = section(1)

    val ifStmt
        = (`if` .. parExpr .. !"stmt" .. (`else` .. !"stmt").maybe)
        .build ("If(it(0), it(1), it(2))")

    val exprStmtList
        = (expr around0 `,`)
        .build ("it.list<Stmt>()")

    val forInit
        = (modifiers .. varDeclNoSemi).build ("it.list<Stmt>()") / exprStmtList

    val basicForStmt
        = ( `for` .. `(`
        ..  forInit .. semi
        ..  expr.maybe .. semi
        ..  exprStmtList.opt
        ..  `)` .. !"stmt")
        .build ("BasicFor(it(0), it(1), it(2), it(3))")

    val forVarDecl
        =  modifiers .. type ..varDeclaratorID.. colon .. expr

    val enhancedForStmt
        = ( `for` .. `(` .. forVarDecl .. `)` .. !"stmt")
        .build ("EnhancedFor(it(0), it(1), it(2), it(3), it(4))")

    val whileStmt
        = ( `while` .. parExpr .. !"stmt")
        .build ("WhileStmt(it(0), it(1))")

    val doWhileStmt
        = ( `do` .. !"stmt" .. `while` .. parExpr .. semi)
        .build ("DoWhileStmt(it(0), it(1))")

    val catchParameterTypes
        = (type around0 `|`)
        .build ("it.list<Type>()")

    val catchParameter
        =  modifiers .. catchParameterTypes .. varDeclaratorID

    val catchClause
        = (`catch` .. `(` .. catchParameter .. `)` .. !"block")
        .build ("CatchClause(it(0), it(1), it(2), it(3))")

    val catchClauses
        = catchClause.repeat0
        .build ("it.list<CatchClause>()")

    val finallyClause
        =  `finally` .. !"block"

    val resource
        = (modifiers .. type .. varDeclaratorID .. `=` .. expr)
        .build ("TryResource(it(0), it(1), it(2), it(3))")

    val resources
        = (`(` .. (resource around1 semi) .. `)`).opt
        .build ("it.list<TryResource>()")

    val tryStmt
        = (`try` .. resources .. !"block" .. catchClauses .. finallyClause.maybe)
        .build ("TryStmt(it(0), it(1), it(2), it(3))")

    val defaultLabel
        = (default .. colon)
        .build ("DefaultLabel")

    val caseLabel
        = (`case` .. expr .. colon)
        .build ("CaseLabel(it(0))")

    val switchLabel
        = caseLabel / defaultLabel

    val switchClause
        = (switchLabel .. !"stmts")
        .build ("SwitchClause(it(0), it(1))")

    val switchStmt
        = (`switch` .. parExpr .. `{` .. switchClause.repeat0.. `}`)
        .build ("SwitchStmt(it(0), it.list(1))")

    val synchronizedStmt
        = (`synchronized` .. parExpr .. !"block")
        .build ("SynchronizedStmt(it(1), it(2))")

    val returnStmt
        = (`return` .. expr.maybe .. semi)
        .build ("ReturnStmt(it(0))")

    val throwStmt
        = (`throw` .. expr .. semi)
        .build ("ThrowStmt(it(0))")

    val breakStmt
        = (`break` .. iden.maybe .. semi)
        .build ("BreakStmt(it(0)")

    val continueStmt
        = (`continue` .. iden.maybe .. semi)
        .build ("ContinueStmt(it(0))")

    val assertStmt
        = (`assert` .. expr .. (colon .. expr).maybe .. semi)
        .build ("AssertStmt(it(0), it(1))")

    val semiStmt
        = semi
        .build ("SemiStmt")

    val exprStmt
        =  expr .. semi

    val labelledStmt
        = (iden .. colon .. !"stmt")
        .build ("LabelledStmt(it(0), it(1))")

    val stmt = (
        !"block" /
        ifStmt /
        basicForStmt /
        enhancedForStmt /
        whileStmt /
        doWhileStmt /
        tryStmt /
        switchStmt /
        synchronizedStmt /
        returnStmt /
        throwStmt /
        breakStmt /
        continueStmt /
        assertStmt /
        semiStmt /
        exprStmt /
        labelledStmt /
        varDecl /
        typeDecl
    ).with(TypeHint)

    val block
        = (`{` .. stmt.repeat0.. `}`)
        .build ("Block(it.list())")

    val stmts
        = stmt.repeat0
        .build ("it.list<Stmt>()")

    ///  TOP-LEVEL =================================================================================
    val `TOP-LEVEL` = section(1)

    val packageDecl
        = (annotations .. `package` ..qualified_iden.. semi)
        .build ("Package(it(0), it(1))")

    val importDecl
        = (`import` .. `static`.as_bool ..qualified_iden.. (dot .. `*`).as_bool .. semi)
        .build ("Import(it(0), it(1), it(2))")

    val importDecls
        = importDecl.repeat0
        .build ("it.list<Import>()")

    val root
        = (!"whitespace" .. packageDecl.maybe .. importDecls .. typeDecls)
        .build ("File(it(0), it(1), it(2))")
}