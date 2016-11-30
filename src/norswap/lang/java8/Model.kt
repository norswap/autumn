package norswap.lang.java8
import norswap.autumn.model.*
import norswap.lang.java8.ast.*
import norswap.lang.java8.ast.TypeDeclKind.*

class Java8Model
{
    val line_comment
        =  "//".str .. (!"char_any" until0 "\n".str)

    val multi_comment
        =  "/*".str .. (!"char_any()" until0 "*/".str)

    val whitespace
        = (!"space_char" / line_comment / multi_comment).repeat0

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

    // Numerals: bits and pieces

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

    val hexa_float_lit
        =  hex_significand .. binary_exponent .. float_suffix_opt

    val decimal_float_lit =
        (digits1 .. dlit .. digits0.. exponent.opt .. float_suffix_opt) /
        (dlit .. digits1.. exponent.opt .. float_suffix_opt) /
        (digits1 .. exponent .. float_suffix_opt) /
        (digits1 .. exponent.opt ..float_suffix)

    val float_lit
        = (hexa_float_lit / decimal_float_lit)
        .token("parse_float(it)")

    // Numerals: integral

    val bit
        = "01".set

    val binary_prefix
        = "0b".str / "0B".str

    val binary_num
        =  binary_prefix .. (bit.repeat1 around1 underscores)

    val octal_num
        =  "0".str.. (`_`.repeat0.. !"octal_digit").repeat1

    val decimal_num
        = "0".str / digits1

    val integer_num
        = hex_num / binary_num / octal_num / decimal_num

    val integer_lit
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

    // todo needs escaping
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

    val literal
        = (float_lit / integer_lit / `true` / `false` / char_literal / string_literal / `null`)
        .build ("Literal(get())")

    /// ANNOTATIONS ================================================================================

    val annotationElement
        = !"ternary" / !"annotationElementList" / !"annotation"

    val annotationElementList
        = (`{` .. (annotationElement around0 `,`) .. `,`.opt .. `}`)
        .build ("AnnotationElementList(rest())")

    val annotationElementPair
        = (iden .. `=` .. annotationElement)
        .build ("Pair<String, AnnotationElement>(get(), get())")

    val normalAnnotationSuffix
        = (`(` .. (annotationElementPair around1 `,`) .. `)`)
        .build (1, "NormalAnnotation(get(), rest<Pair<String, AnnotationElement>>())")

    val singleElementAnnotationSuffix
        = (`(` .. annotationElement .. `)`)
        .build (1, "SingleElementAnnotation(get(), get())")

    val markerAnnotationSuffix
        = (`(` .. `)`).opt
        .build (1, "MarkerAnnotation(get())")

    val annotationSuffix =
        normalAnnotationSuffix /
        singleElementAnnotationSuffix /
        markerAnnotationSuffix

    val qualifiedIden
        = (iden around1 dot)
        .build ("it.list<String>()")

    val annotation
        = `@` .. qualifiedIden .. annotationSuffix

    val annotations
        = annotation.repeat0
        .build ("it.list<Annotation>()")

    /// TYPES ======================================================================================

    val basicType
        = `byte` / `short` / `int` / `long` / `char` / `float` / `double` / `boolean` / `void`

    val primitiveType
        = (annotations .. basicType)
        .build ("PrimitiveType(get(), get())")

    val extendsBound
        = (`extends` .. !"type")
        .build ("ExtendsBound(get())")

    val superBound
        = (`super` .. !"type")
        .build ("SuperBound(get())")

    val wildcard
        = (annotations .. `?` .. (extendsBound / superBound).maybe)
        .build ("Wildcard(get(), maybe())")

    val typeArgs
        = (lt .. ((!"type" / wildcard) around0 `,`) .. gt).opt
        .build ("it.list<Type>()")

    val classTypePart
        = (annotations .. iden .. typeArgs)
        .build ("ClassTypePart(get(), get(), get())")

    val classType
        = (classTypePart around1 dot)
        .build ("ClassType(rest())")

    val stemType
        = primitiveType / classType

    val dim
        = (annotations .. lsbra .. rsbra)
        .build ("Dimension(get())")

    val dims
        = dim.repeat0
        .build ("it.list<Dimension>()")

    val dims1
        = dim.repeat1
        .build ("it.list<Dimension>()")

    val typeDimSuffix
        = dims1
        .build (1, "ArrayType(get(), get())")

    val type
        = stemType .. typeDimSuffix.opt

    val typeUnionSyntax
        = !"type" around1 `&`

    val typeUnion
        = typeUnionSyntax
        .build ("it.list<Type>()")

    val typeBounds
        = (`extends` .. typeUnionSyntax).opt
        .build ("it.list<Type>()")

    val typeParam
        = (annotations .. iden .. typeBounds)
        .build ("TypeParam(get(), get(), get())")

    val typeParams
        = (lt .. (typeParam around0 `,`) .. gt).opt
        .build ("it.list<TypeParam>()")

    /// MODIFIERS ==================================================================================

    val keywordModifier = (
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
        .build ("Keyword.valueOf(get())")

    val modifier
        = annotation / keywordModifier

    val modifiers
        = modifier.repeat0
        .build ("it.list<Modifier>()")

    /// PARAMETERS =================================================================================

    val args
        = (`(` .. (!"expr" around0 `,`) .. `)`)
        .build ("it.list<Expr>()")

    val thisParameterQualifier
        = (iden .. dot).repeat0
        .build ("it.list<String>()")

    val thisParamSuffix
        = (thisParameterQualifier .. `this`)
        .build (2, "ThisParameter(get(), get(), get())")

    val idenParamSuffix
        = (iden .. dims)
        .build (2, "IdenParameter(get(), get(), get(), get())")

    val variadicParamSuffix
        = (annotations .. ellipsis .. iden)
        .build (2, "VariadicParameter(get(), get(), get(), get())")

    val formalParamSuffix =
        idenParamSuffix /
        thisParamSuffix /
        variadicParamSuffix

    val formalParam
        =  modifiers .. type .. formalParamSuffix

    val formalParams
        = (`(` .. (formalParam around0 `,`) .. `)`)
        .build ("FormalParameters(rest())")

    val untypedParams
        = (`(` .. (iden around1 `,`) .. `)`)
        .build ("UntypedParameters(rest())")

    val singleParam
        = iden
        .build ("UntypedParameters(listOf(get<String>()))")

    val lambdaParams
        = formalParams / untypedParams / singleParam

    /// NON-TYPE DECLARATIONS ======================================================================

    val varInit
        = !"expr" / !"arrayInit"

    val arrayInit
        = (`{` .. (varInit around0 `,`) .. `,`.opt .. `}`)
        .build ("ArrayInit(rest())")

    val varDeclaratorID
        = (iden .. dims)
        .build ("VarDeclaratorID(get(), get())")

    val varDeclarator
        = (varDeclaratorID .. (`=` .. varInit).maybe)
        .build ("VarDeclarator(get(), maybe())")

    val varDeclNoSemi
        = (type .. (varDeclarator around1 `,`))
        .build (1, "VarDecl(get(), get(), rest())")

    val varDeclSuffix
        = varDeclNoSemi .. semi

    val varDecl
        =  modifiers .. varDeclSuffix

    val throwsClause
        = (throws .. (type around0 `,`)).opt
        .build ("it.list<Type>()")

    val methodDeclSuffix
        = (typeParams .. type .. iden .. formalParams .. dims .. throwsClause .. (!"block" / semi).maybe)
        .build (1, "MethodDecl(get(), get(), get(), get(), get(), get(), get(), maybe())")

    val constructorDeclSuffix
        = (typeParams .. iden .. formalParams .. throwsClause .. !"block")
        .build (1, "ConstructorDecl(get(), get(), get(), get(), get(), get())")

    val initBlock
        = (`static`.as_bool .. !"block")
        .build ("InitBlock(get(), get())")

    /// TYPE DECLARATIONS ==========================================================================

    // Common -----------------------------------------------------------------

    val extendsClause
        = (`extends` .. (type around0 `,`)).opt
        .build ("it.list<Type>()")

    val implementsClause
        = (`implements` .. (type around0 `,`)).opt
        .build ("it.list<Type>()")

    val typeSig
        =  iden .. typeParams .. extendsClause .. implementsClause

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
        .build ("EnumConstant(get(), get(), maybe(), maybe())")

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
        .build (1, "EnumDecl(TypeDecl(ENUM, get(), get(), get(), get(), get(), get()), get())")

    // Annotations ------------------------------------------------------------

    val annotDefaultClause
        = (`default` .. annotationElement)
        .build ("get(1)")

    val annotElemDecl
        = (modifiers .. type .. iden .. `(` .. `)` .. dims .. annotDefaultClause.maybe .. semi)
        .build ("AnnotationElemDecl(get(), get(), get(), get(), maybe())")

    val annotBodyDecls
        = (annotElemDecl / classBodyDecl).repeat0
        .build ("it.list<Decl>()")

    val annotationDecl
        = (`@` .. `interface` .. typeSig .. `{` .. annotBodyDecls .. `}`)
        .build (1, "TypeDecl(ANNOTATION, get(), get(), get(), get(), get(), get())")

    // ------------------------------------------------------------------------

    val classDecl
        = (`class` .. typeSig .. typeBody)
        .build (1, "TypeDecl(CLASS, get(), get(), get(), get(), get(), get())")

    val interfaceDeclaration
        = (`interface` .. typeSig .. typeBody)
        .build (1, "TypeDecl(INTERFACE, get(), get(), get(), get(), get(), get())")

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

    // Array Constructor ------------------------------------------------------

    val initArrayCreator
        = (stemType .. dims1 .. arrayInit)
        .build ("ArrayCtorCall(get(), listOf(), get(), get())")

    val dimExpr
        = (annotations .. lsbra .. !"expr" .. rsbra)
        .build ("DimExpr(get(), get())")

    val dimExprs
        = dimExpr.repeat1
        .build ("it.list<DimExpr>()")

    val dimExprArrayCreator
        = (stemType .. dimExprs .. dims)
        .build ("ArrayCtorCall(get(), get(), get(), null)")

    val arrayCtorCall
        =  `new` .. (dimExprArrayCreator / initArrayCreator)

    // Lambda Expression ------------------------------------------------------

    val lambda
        = (lambdaParams .. arrow .. (!"block" / !"expr"))
        .build ("Lambda(get(), get())")

    // Expression: Primary ----------------------------------------------------

    val ctorCall
        = (`new` .. typeArgs .. stemType .. args.. typeBody.maybe)
        .build ("CtorCall(get(), get(), get(), maybe())")

    val superExpr
        = (`super` .. args.maybe)
        .build (
            "maybe<List<Expr>>()" +
                "?. let { SuperCall(it) }" +
                "?: Super"
        )

    val thisExpr
        = (`this` .. args.maybe)
        .build (
            "maybe<List<Expr>>()" +
                "?. let { ThisCall(it) }" +
                "?: This"
        )

    val idenOrMethodExpr
        = (iden .. args.maybe)
        .build (
            "maybe<List<Expr>>(1)" +
                "?. let { MethodCall(null, listOf(), get(), it) }" +
                "?: Identifier(get())"
        )

    val classExpr
        = (type .. dot .. `class`)
        .build ("ClassExpr(get())")

    val parExpr
        =  `(` .. !"expr" .. `)`

    val methodRef
        = (type .. dcolon .. typeArgs .. iden)
        .build ("MaybeBoundMethodReference(get(), get(), get())")

    val newRef
        = (type .. dcolon .. typeArgs .. `new`)
        .build ("NewReference(get(), get())")

    val newRefSuffix
        = `new`
        .build (2, "NewReference(get(), get())")

    val methodRefSuffix
        = iden
        .build (2, "MaybeBoundMethodReference(get(), get(), get())")

    val refSuffix
        = dcolon .. typeArgs .. (newRefSuffix / methodRefSuffix)

    val classExprSuffix
        = (dot .. `class`)
        .build (1, "ClassExpr(get())")

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
        .build (1, "DotThis(get())")

    val dotSuper
        = `super`
        .build (1, "DotSuper(get())")

    val dotIden
        = iden
        .build (1, "DotIden(get(), get())")

    val dotNew
        = ctorCall
        .build (1, "DotNew(get(), get())")

    val dotMethod
        = (typeArgs .. iden .. args)
        .build (1, "MethodCall(get(), get(), get(), get())")

    val dotPostfix =
        dotMethod /
        dotIden /
        dotThis /
        dotSuper /
        dotNew

    val refPostfix
        = (dcolon .. typeArgs .. iden)
        .build (1, "BoundMethodReference(get(), get(), get())")

    val arrayPostfix
        = (lsbra .. !"expr" .. rsbra)
        .build (1, "ArrayAccess(get(), get())")

    val incSuffix
        = `++`
        .build (1, "PostIncrement(get())")

    val decSuffix
        = `--`
        .build (1, "PostDecrement(get())")

    val postfix =
      dot .. dotPostfix /
      arrayPostfix /
      incSuffix /
      decSuffix /
      refPostfix

    val postfixExpr
        =  primaryExpr .. postfix.repeat0

    val incPrefix
        = (`++` .. !"prefixExpr")
        .build ("PreIncrement(get())")

    val decPrefix
        = (`--` .. !"prefixExpr")
        .build ("PreDecrement(get())")

    val unaryPlus
        = (`+`  .. !"prefixExpr")
        .build ("UnaryPlus(get())")

    val unaryMinus
        = (`-`  .. !"prefixExpr")
        .build ("UnaryMinus(get())")

    val complement
        = (`~`  .. !"prefixExpr")
        .build ("Complement(get())")

    val not
        = (`!`  .. !"prefixExpr")
        .build ("Not(get())")

    val cast
        = (`(` .. typeUnion .. `)` .. (lambda / !"prefixExpr"))
        .build ("Cast(get(), get())")

    val prefixExpr =
        incPrefix /
        decPrefix /
        unaryPlus /
        unaryMinus /
        complement /
        not /
        cast /
        postfixExpr

    val expr = "".str

    /// STATEMENTS =================================================================================

    val ifStmt
        = (`if` .. parExpr .. !"stmt" .. (`else` .. !"stmt").maybe)
        .build ("If(get(), get(), maybe())")

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
        .build ("BasicFor(get(), maybe(), get(), get())")

    val forVarDecl
        =  modifiers .. type ..varDeclaratorID.. colon .. expr

    val enhancedForStmt
        = ( `for` .. `(` .. forVarDecl .. `)` .. !"stmt")
        .build ("EnhancedFor(get(), get(), get(), get(), get())")

    val whileStmt
        = ( `while` .. parExpr .. !"stmt")
        .build ("WhileStmt(get(), get())")

    val doWhileStmt
        = ( `do` .. !"stmt" .. `while` .. parExpr .. semi)
        .build ("DoWhileStmt(get(), get())")

    val catchParameterTypes
        = (type around0 `|`)
        .build ("it.list<Type>()")

    val catchParameter
        =  modifiers .. catchParameterTypes .. varDeclaratorID

    val catchClause
        = (`catch` .. `(` .. catchParameter .. `)` .. !"block")
        .build ("CatchClause(get(), get(), get(), get())")

    val catchClauses
        = catchClause.repeat0
        .build ("it.list<CatchClause>()")

    val finallyClause
        =  `finally` .. !"block"

    val resource
        = (modifiers .. type .. varDeclaratorID .. `=` .. expr)
        .build ("TryResource(get(), get(), get(), get())")

    val resources
        = (`(` .. (resource around1 semi) .. `)`).opt
        .build ("it.list<TryResource>()")

    val tryStmt
        = (`try` .. resources .. !"block" .. catchClauses .. finallyClause.maybe)
        .build ("TryStmt(get(), get(), get(), maybe())")

    val defaultLabel
        = (default .. colon)
        .build ("DefaultLabel")

    val caseLabel
        = (`case` .. expr .. colon)
        .build ("CaseLabel(get())")

    val switchLabel
        = caseLabel / defaultLabel

    val switchClause
        = (switchLabel .. !"stmts")
        .build ("SwitchClause(get(), get())")

    val switchStmt
        = (`switch` .. parExpr .. `{` .. switchClause.repeat0.. `}`)
        .build ("SwitchStmt(get(), rest())")

    val synchronizedStmt
        = (`synchronized` .. parExpr .. !"block")
        .build ("SynchronizedStmt(get(1), get(2))")

    val returnStmt
        = (`return` .. expr.maybe .. semi)
        .build ("ReturnStmt(maybe())")

    val throwStmt
        = (`throw` .. expr .. semi)
        .build ("ThrowStmt(get())")

    val breakStmt
        = (`break` .. iden.maybe .. semi)
        .build ("BreakStmt(maybe())")

    val continueStmt
        = (`continue` .. iden.maybe .. semi)
        .build ("ContinueStmt(maybe())")

    val assertStmt
        = (`assert` .. expr .. (colon .. expr).maybe .. semi)
        .build ("AssertStmt(get(), maybe())")

    val semiStmt
        = semi
        .build ("SemiStmt")

    val exprStmt
        =  expr .. semi

    val labelledStmt
        = (iden .. colon .. !"stmt")
        .build ("LabelledStmt(get(), get())")

    val stmt =
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

    val block
        = (`{` .. stmt.repeat0.. `}`)
        .build ("Block(rest())")

    val stmts
        = stmt.repeat0
        .build ("it.list<Stmt>()")

    /// TOP-LEVEL ==================================================================================

    val packageDecl
        = (annotations .. `package` .. qualifiedIden .. semi)
        .build ("Package(get(), get())")

    val importDecl
        = (`import` .. `static`.as_bool .. qualifiedIden .. (dot .. `*`).as_bool .. semi)
        .build ("Import(get(), get(), get())")

    val importDecls
        = importDecl.repeat0
        .build ("it.list<Import>()")

    val root
        = (!"whitespace" .. packageDecl.maybe .. importDecls .. typeDecls)
        .build ("File(maybe(), get(), get())")
}