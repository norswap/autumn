//package norswap.lang.java8
//import norswap.autumn.Parser
//import norswap.autumn.TokenGrammar
//import norswap.autumn.parsers.*
//import norswap.lang.java_base.*
//import norswap.lang.java8.ast.*
//import norswap.lang.java8.ast.TypeDeclKind.*
//
//class Scratch: TokenGrammar()
//{
//
//    /// LEXICAL ====================================================================================
//
//    fun line_comment()
//        = seq { "//".str && until0 ( {char_any()} , {"\n".str} ) }
//
//    fun multi_comment()
//        = seq { "/*".str && until0 ( {char_any()} , {"*/".str} ) }
//
//    override fun whitespace()
//        = repeat0 { choice { space_char() || line_comment() || multi_comment() } }
//
//    val boolean
//        = "boolean".token
//
//    val byte
//        = "byte".token
//
//    val char
//        = "char".token
//
//    val double
//        = "double".token
//
//    val float
//        = "float".token
//
//    val int
//        = "int".token
//
//    val long
//        = "long".token
//
//    val short
//        = "short".token
//
//    val void
//        = "void".token
//
//    val abstract
//        = "abstract".token
//
//    val default
//        = "default".token
//
//    val final
//        = "final".token
//
//    val native
//        = "native".token
//
//    val private
//        = "private".token
//
//    val protected
//        = "protected".token
//
//    val public
//        = "public".token
//
//    val static
//        = "static".token
//
//    val strictfp
//        = "strictfp".token
//
//    val synchronized
//        = "synchronized".token
//
//    val transient
//        = "transient".token
//
//    val volatile
//        = "volatile".token
//
//    val `false`
//        = token ({ false }) { "false".str }
//
//    val `true`
//        = token ({ true }) { "true".str }
//
//    val `null`
//        = token ({ Null }) { "null".str }
//
//    val assert
//        = "assert".keyword
//
//    val `break`
//        = "break".keyword
//
//    val case
//        = "case".keyword
//
//    val catch
//        = "catch".keyword
//
//    val `class`
//        = "class".keyword
//
//    val const
//        = "const".keyword
//
//    val `continue`
//        = "continue".keyword
//
//    val `do`
//        = "do".keyword
//
//    val `else`
//        = "else".keyword
//
//    val enum
//        = "enum".keyword
//
//    val extends
//        = "extends".keyword
//
//    val finally
//        = "finally".keyword
//
//    val `for`
//        = "for".keyword
//
//    val goto
//        = "goto".keyword
//
//    val `if`
//        = "if".keyword
//
//    val implements
//        = "implements".keyword
//
//    val import
//        = "import".keyword
//
//    val `interface`
//        = "interface".keyword
//
//    val instanceof
//        = "instanceof".keyword
//
//    val new
//        = "new".keyword
//
//    val `package`
//        = "package".keyword
//
//    val `return`
//        = "return".keyword
//
//    val `super`
//        = "super".keyword
//
//    val switch
//        = "switch".keyword
//
//    val `this`
//        = "this".keyword
//
//    val throws
//        = "throws".keyword
//
//    val `throw`
//        = "throw".keyword
//
//    val `try`
//        = "try".keyword
//
//    val `while`
//        = "while".keyword
//
//    val `!`
//        = "!".keyword
//
//    val `%`
//        = "%".keyword
//
//    val `%=`
//        = "%=".keyword
//
//    val `&`
//        = "&".keyword
//
//    val `&&`
//        = "&&".keyword
//
//    val `&=`
//        = "&=".keyword
//
//    val `(`
//        = "(".keyword
//
//    val `)`
//        = ")".keyword
//
//    val `*`
//        = "*".keyword
//
//    val `*=`
//        = "*=".keyword
//
//    val `+`
//        = "+".keyword
//
//    val `++`
//        = "++".keyword
//
//    val `+=`
//        = "+=".keyword
//
//    val `,`
//        = ",".keyword
//
//    val `-`
//        = "-".keyword
//
//    val `--`
//        = "--".keyword
//
//    val `-=`
//        = "-=".keyword
//
//    val `=`
//        = "=".keyword
//
//    val `==`
//        = "==".keyword
//
//    val `?`
//        = "?".keyword
//
//    val `^`
//        = "^".keyword
//
//    val `^=`
//        = "^=".keyword
//
//    val `{`
//        = "{".keyword
//
//    val `|`
//        = "|".keyword
//
//    val `|=`
//        = "|=".keyword
//
//    val `!=`
//        = "!=".keyword
//
//    val `||`
//        = "||".keyword
//
//    val `}`
//        = "}".keyword
//
//    val `~`
//        = "~".keyword
//
//    val `@`
//        = "@".keyword
//
//    val div
//        = "/".keyword
//
//    val dive
//        = "/=".keyword
//
//    val gt
//        = ">".keyword
//
//    val lt
//        = "<".keyword
//
//    val ge
//        = ">=".keyword
//
//    val le
//        = "<=".keyword
//
//    val sl
//        = "<<".keyword
//
//    val sle
//        = "<<=".keyword
//
//    fun sr()
//        = ">>".word
//
//    val sre
//        = ">>=".keyword
//
//    fun bsr()
//        = ">>>".word
//
//    val bsre
//        = ">>>=".keyword
//
//    val lsbra
//        = "[".keyword
//
//    val rsbra
//        = "]".keyword
//
//    val arrow
//        = "->".keyword
//
//    val colon
//        = ":".keyword
//
//    val semi
//        = ";".keyword
//
//    val dot
//        = ".".keyword
//
//    val ellipsis
//        = "...".keyword
//
//    val dcolon
//        = "::".keyword
//
//    val iden
//        = token ({ TODO() }) { java_iden() }
//
//    fun `_`()
//        = "_".str
//
//    fun dlit()
//        = ".".str
//
//    fun hex_prefix()
//        = choice { "0x".str || "0x".str }
//
//    fun underscores()
//        = repeat0 { `_`() }
//
//    fun digits1()
//        = around0 ( {digit()} , {underscores()} )
//
//    fun digits0()
//        = around0 ( {digit()} , {underscores()} )
//
//    fun hex_digits()
//        = around1 ( {hex_digit()} , {underscores()} )
//
//    fun hex_num()
//        = seq { hex_prefix() && hex_digits() }
//
//    fun hex_significand()
//        = choice { seq { hex_prefix() && opt { hex_digits() } && dlit() && hex_digits() } || seq { hex_prefix() && hex_digits() && opt { dlit() } } }
//
//    fun exp_sign_opt()
//        = opt { "+-".set }
//
//    fun exponent()
//        = seq { "eE".set && exp_sign_opt() && digits1() }
//
//    fun binary_exponent()
//        = seq { "pP".set && exp_sign_opt() && digits1() }
//
//    fun float_suffix()
//        = "fFdD".set
//
//    fun float_suffix_opt()
//        = opt { float_suffix() }
//
//    fun hex_float_lit()
//        = seq { hex_significand() && binary_exponent() && float_suffix_opt() }
//
//    fun decimal_float_lit()
//        = choice { seq { digits1() && dlit() && digits0() && opt { exponent() } && float_suffix_opt() } || seq { dlit() && digits1() && opt { exponent() } && float_suffix_opt() } || seq { digits1() && exponent() && float_suffix_opt() } || seq { digits1() && opt { exponent() } && float_suffix() } }
//
//    val float_literal
//        = token ({ parse_float(it) }) { choice { hex_float_lit() || decimal_float_lit() } }
//
//    fun bit()
//        = "01".set
//
//    fun binary_prefix()
//        = choice { "0b".str || "0B".str }
//
//    fun binary_num()
//        = seq { binary_prefix() && around1 ( {repeat1 { bit() }} , {underscores()} ) }
//
//    fun octal_num()
//        = seq { "0".str && repeat1 { seq { underscores() && octal_digit() } } }
//
//    fun decimal_num()
//        = choice { "0".str || digits1() }
//
//    fun integer_num()
//        = choice { hex_num() || binary_num() || octal_num() || decimal_num() }
//
//    val integer_literal
//        = token ({ parse_int(it) }) { seq { integer_num() && opt { "lL".set } } }
//
//    fun octal_escape()
//        = choice { seq { char_range('0', '3') && octal_digit() && octal_digit() } || seq { octal_digit() && opt { octal_digit() } } }
//
//    fun unicode_escape()
//        = seq { repeat1 { "u".str } && repeat(4) { hex_digit() } }
//
//    fun escape()
//        = seq { "\\".str && choice { "btnfr\"'\\".set || octal_escape() || unicode_escape() } }
//
//    fun naked_char()
//        = choice { escape() || seq { not { "'\\\n\r".set } && char_any() } }
//
//    val char_literal
//        = token ({ parse_char(it) }) { seq { "'".str && naked_char() && "'".str } }
//
//    fun naked_string_char()
//        = choice { escape() || seq { not { "\"\\\n\r".set } && char_any() } }
//
//    val string_literal
//        = token ({ parse_string(it) }) { seq { "\"".str && repeat0 { naked_string_char() } && "\"".str } }
//
//    fun literal() = build(
//        syntax = { choice { integer_literal() || string_literal() || `null`() || float_literal() || `true`() || `false`() || char_literal() } },
//        effect = { Literal(it(0)) })
//
//    /// ANNOTATIONS ================================================================================
//
//    fun annotation_element()
//        = choice { ternary() || annotation_element_list() || annotation() }
//
//    fun annotation_inner_list()
//        = comma_list_term0(
//        { annotation_element() })
//
//    fun annotation_element_list() = build(
//        syntax = { seq { `{`() && around0 ( {annotation_element()} , {`,`()} ) && opt { `,`() } && `}`() } },
//        effect = { AnnotationElementList(it.list()) })
//
//    fun annotationElementPair() = build(
//        syntax = { seq { iden() && `=`() && annotation_element() } },
//        effect = { Pair<String, AnnotationElement>(it(0), it(1)) })
//
//    fun normalAnnotationSuffix() = build(1,
//        syntax = { seq { `(`() && around1 ( {annotationElementPair()} , {`,`()} ) && `)`() } },
//        effect = { NormalAnnotation(it(0), it.list<Pair<String, AnnotationElement>>(1))) })
//
//    fun singleElementAnnotationSuffix() = build(1,
//        syntax = { seq { `(`() && annotation_element() && `)`() } },
//        effect = { SingleElementAnnotation(it(0), it(1)) })
//
//    fun markerAnnotationSuffix() = build(1,
//        syntax = { opt { seq { `(`() && `)`() } } },
//        effect = { MarkerAnnotation(it(0)) })
//
//    fun annotationSuffix()
//        = choice { normalAnnotationSuffix() || singleElementAnnotationSuffix() || markerAnnotationSuffix() }
//
//    fun qualifiedIden() = build(
//        syntax = { around1 ( {iden()} , {dot()} ) },
//        effect = { it.list<String>() })
//
//    fun annotation()
//        = seq { `@`() && qualifiedIden() && annotationSuffix() }
//
//    fun annotations() = build(
//        syntax = { repeat0 { annotation() } },
//        effect = { it.list<Annotation>() })
//
//    /// TYPES ======================================================================================
//
//    fun basicType()
//        = choice { byte() || short() || int() || long() || char() || float() || double() || boolean() || void() }
//
//    fun primitiveType() = build(
//        syntax = { seq { annotations() && basicType() } },
//        effect = { PrimitiveType(it(0), it(1)) })
//
//    fun extendsBound() = build(
//        syntax = { seq { extends() && type() } },
//        effect = { ExtendsBound(it(0)) })
//
//    fun superBound() = build(
//        syntax = { seq { `super`() && type() } },
//        effect = { SuperBound(it(0)) })
//
//    fun wildcard() = build(
//        syntax = { seq { annotations() && `?`() && maybe { choice { extendsBound() || superBound() } } } },
//        effect = { Wildcard(it(0), it(1) })
//
//    fun typeArgs() = build(
//        syntax = { opt { seq { lt() && around0 ( {choice { type() || wildcard() }} , {`,`()} ) && gt() } } },
//        effect = { it.list<Type>() })
//
//    fun classTypePart() = build(
//        syntax = { seq { annotations() && iden() && typeArgs() } },
//        effect = { ClassTypePart(it(0), it(1), it(2)) })
//
//    fun classType() = build(
//        syntax = { around1 ( {classTypePart()} , {dot()} ) },
//        effect = { ClassType(it.list()) })
//
//    fun stemType()
//        = choice { primitiveType() || classType() }
//
//    fun dim() = build(
//        syntax = { seq { annotations() && lsbra() && rsbra() } },
//        effect = { Dimension(it(0)) })
//
//    fun dims() = build(
//        syntax = { repeat0 { dim() } },
//        effect = { it.list<Dimension>() })
//
//    fun dims1() = build(
//        syntax = { repeat1 { dim() } },
//        effect = { it.list<Dimension>() })
//
//    fun typeDimSuffix() = build(1,
//        syntax = { dims1() },
//        effect = { ArrayType(it(0), it(1)) })
//
//    fun type()
//        = seq { stemType() && opt { typeDimSuffix() } }
//
//    fun typeUnionSyntax()
//        = around1 ( {type()} , {`&`()} )
//
//    fun typeUnion() = build(
//        syntax = { typeUnionSyntax() },
//        effect = { it.list<Type>() })
//
//    fun typeBounds() = build(
//        syntax = { opt { seq { extends() && typeUnionSyntax() } } },
//        effect = { it.list<Type>() })
//
//    fun typeParam() = build(
//        syntax = { seq { annotations() && iden() && typeBounds() } },
//        effect = { TypeParam(it(0), it(1), it(2)) })
//
//    fun typeParams() = build(
//        syntax = { opt { seq { lt() && around0 ( {typeParam()} , {`,`()} ) && gt() } } },
//        effect = { it.list<TypeParam>() })
//
//    /// MODIFIERS ==================================================================================
//
//    fun keywordModifier() = build(
//        syntax = { choice { public() || protected() || private() || abstract() || static() || final() || synchronized() || native() || strictfp() || default() || transient() || volatile() } },
//        effect = { Keyword.valueOf(it(0)) })
//
//    fun modifier()
//        = choice { annotation() || keywordModifier() }
//
//    fun modifiers() = build(
//        syntax = { repeat0 { modifier() } },
//        effect = { it.list<Modifier>() })
//
//    /// PARAMETERS =================================================================================
//
//    fun args() = build(
//        syntax = { seq { `(`() && around0 ( {expr()} , {`,`()} ) && `)`() } },
//        effect = { it.list<Expr>() })
//
//    fun thisParameterQualifier() = build(
//        syntax = { repeat0 { seq { iden() && dot() } } },
//        effect = { it.list<String>() })
//
//    fun thisParamSuffix() = build(2,
//        syntax = { seq { thisParameterQualifier() && `this`() } },
//        effect = { ThisParameter(it(0), it(1), it(2)) })
//
//    fun idenParamSuffix() = build(2,
//        syntax = { seq { iden() && dims() } },
//        effect = { IdenParameter(it(0), it(1), it(2), it(3)) })
//
//    fun variadicParamSuffix() = build(2,
//        syntax = { seq { annotations() && ellipsis() && iden() } },
//        effect = { VariadicParameter(it(0), it(1), it(2), it(3)) })
//
//    fun formalParamSuffix()
//        = choice { idenParamSuffix() || thisParamSuffix() || variadicParamSuffix() }
//
//    fun formalParam()
//        = seq { modifiers() && type() && formalParamSuffix() }
//
//    fun formalParams() = build(
//        syntax = { seq { `(`() && around0 ( {formalParam()} , {`,`()} ) && `)`() } },
//        effect = { FormalParameters(it.list()) })
//
//    fun untypedParams() = build(
//        syntax = { seq { `(`() && around1 ( {iden()} , {`,`()} ) && `)`() } },
//        effect = { UntypedParameters(it.list()) })
//
//    fun singleParam() = build(
//        syntax = { iden() },
//        effect = { UntypedParameters(listOf(get<String>())) })
//
//    fun lambdaParams()
//        = choice { formalParams() || untypedParams() || singleParam() }
//
//    /// NON-TYPE DECLARATIONS ======================================================================
//
//    fun varInit()
//        = choice { expr() || array_init() }
//
//    fun arrayInit() = build(
//        syntax = { seq { `{`() && around0 ( {varInit()} , {`,`()} ) && opt { `,`() } && `}`() } },
//        effect = { ArrayInit(it.list()) })
//
//    fun varDeclaratorID() = build(
//        syntax = { seq { iden() && dims() } },
//        effect = { VarDeclaratorID(it(0), it(1)) })
//
//    fun varDeclarator() = build(
//        syntax = { seq { varDeclaratorID() && maybe { seq { `=`() && varInit() } } } },
//        effect = { VarDeclarator(it(0), it(1) })
//
//    fun varDeclNoSemi() = build(1,
//        syntax = { seq { stemType() && opt { typeDimSuffix() } && around1 ( {varDeclarator()} , {`,`()} ) } },
//        effect = { VarDecl(it(0), it(1), it.list(2)) })
//
//    fun varDeclSuffix()
//        = seq { varDeclNoSemi() && semi() }
//
//    fun varDecl()
//        = seq { modifiers() && varDeclSuffix() }
//
//    fun throwsClause() = build(
//        syntax = { opt { seq { throws() && around0 ( {type()} , {`,`()} ) } } },
//        effect = { it.list<Type>() })
//
//    fun methodDeclSuffix() = build(1,
//        syntax = { seq { typeParams() && type() && iden() && formalParams() && dims() && throwsClause() && maybe { choice { block() || semi() } } } },
//        effect = { MethodDecl(it(0), it(1), it(2), it(3), it(4), it(5), it(6), it(7)) })
//
//    fun constructorDeclSuffix() = build(1,
//        syntax = { seq { typeParams() && iden() && formalParams() && throwsClause() && block() } },
//        effect = { ConstructorDecl(it(0), it(1), it(2), it(3), it(4), it(5)) })
//
//    fun initBlock() = build(
//        syntax = { seq { as_bool { static() } && block() } },
//        effect = { InitBlock(it(0), it(1)) })
//
//    /// TYPE DECLARATIONS ==========================================================================
//
//    fun extendsClause() = build(
//        syntax = { opt { seq { extends() && around0 ( {type()} , {`,`()} ) } } },
//        effect = { it.list<Type>() })
//
//    fun implementsClause() = build(
//        syntax = { opt { seq { implements() && around0 ( {type()} , {`,`()} ) } } },
//        effect = { it.list<Type>() })
//
//    fun typeSig()
//        = seq { iden() && typeParams() && extendsClause() && implementsClause() }
//
//    fun classModifiedDecl()
//        = seq { modifiers() && choice { varDeclSuffix() || methodDeclSuffix() || constructorDeclSuffix() || type_decl_suffix() } }
//
//    fun classBodyDecl()
//        = choice { classModifiedDecl() || initBlock() || semi() }
//
//    fun classBodyDecls() = build(
//        syntax = { repeat0 { classBodyDecl() } },
//        effect = { it.list<Decl>() })
//
//    fun typeBody()
//        = seq { `{`() && classBodyDecls() && `}`() }
//
//    fun enumConstant() = build(
//        syntax = { seq { annotations() && iden() && maybe { args() } && maybe { typeBody() } } },
//        effect = { EnumConstant(it(0), it(1), it(2), it(3)) })
//
//    fun enumClassDecls() = build(
//        syntax = { opt { seq { semi() && repeat0 { classBodyDecl() } } } },
//        effect = { it.list<Decl>() })
//
//    fun enumConstants() = build(
//        syntax = { opt { seq { around1 ( {enumConstant()} , {`,`()} ) && opt { `,`() } } } },
//        effect = { it.list<EnumConstant>() })
//
//    fun enumBody() = affect(
//        syntax = { seq { `{`() && enumConstants() && enumClassDecls() && `}`() } },
//        effect = { TODO() })
//
//    fun enumDecl() = build(1,
//        syntax = { seq { enum() && typeSig() && enumBody() } },
//        effect = { val td = TypeDecl(ENUM, it(0), it(1), it(2), it(3), it(4), it(5))
//            EnumDecl(td, it(6)) })
//
//    fun annotDefaultClause() = build(
//        syntax = { seq { default() && annotation_element() } },
//        effect = { get(1) })
//
//    fun annotElemDecl() = build(
//        syntax = { seq { modifiers() && type() && iden() && `(`() && `)`() && dims() && maybe { annotDefaultClause() } && semi() } },
//        effect = { AnnotationElemDecl(it(0), it(1), it(2), it(3), it(4)) })
//
//    fun annotBodyDecls() = build(
//        syntax = { repeat0 { choice { annotElemDecl() || classBodyDecl() } } },
//        effect = { it.list<Decl>() })
//
//    fun annotationDecl() = build(1,
//        syntax = { seq { `@`() && `interface`() && typeSig() && `{`() && annotBodyDecls() && `}`() } },
//        effect = { TypeDecl(ANNOTATION, it(0), it(1), it(2), it(3), it(4), it(5)) })
//
//    fun classDecl() = build(1,
//        syntax = { seq { `class`() && typeSig() && typeBody() } },
//        effect = { TypeDecl(CLASS, it(0), it(1), it(2), it(3), it(4), it(5)) })
//
//    fun interfaceDeclaration() = build(1,
//        syntax = { seq { `interface`() && typeSig() && typeBody() } },
//        effect = { TypeDecl(INTERFACE, it(0), it(1), it(2), it(3), it(4), it(5)) })
//
//    fun typeDeclSuffix()
//        = choice { classDecl() || interfaceDeclaration() || enumDecl() || annotationDecl() }
//
//    fun typeDecl()
//        = seq { modifiers() && typeDeclSuffix() }
//
//    fun typeDecls() = build(
//        syntax = { repeat0 { choice { typeDecl() || semi() } } },
//        effect = { it.list<Decl>() })
//
//    /// EXPRESSIONS ================================================================================
//
//    fun initArrayCreator() = build(
//        syntax = { seq { stemType() && dims1() && arrayInit() } },
//        effect = { ArrayCtorCall(it(0), emptyList(), it(1), it(2)) })
//
//    fun dimExpr() = build(
//        syntax = { seq { annotations() && lsbra() && expr() && rsbra() } },
//        effect = { DimExpr(it(0), it(1)) })
//
//    fun dimExprs() = build(
//        syntax = { repeat1 { dimExpr() } },
//        effect = { it.list<DimExpr>() })
//
//    fun dimExprArrayCreator() = build(
//        syntax = { seq { stemType() && dimExprs() && dims() } },
//        effect = { ArrayCtorCall(it(0), it(1), it(2), null) })
//
//    fun arrayCtorCall()
//        = seq { new() && choice { dimExprArrayCreator() || initArrayCreator() } }
//
//    fun lambda() = build(
//        syntax = { seq { lambdaParams() && arrow() && choice { block() || expr() } } },
//        effect = { Lambda(it(0), it(1)) })
//
//    fun ctorCall() = build(
//        syntax = { seq { new() && typeArgs() && stemType() && args() && maybe { typeBody() } } },
//        effect = { CtorCall(it(0), it(1), it(2), it(3)) })
//
//    fun superExpr() = build(
//        syntax = { seq { `super`() && maybe { args() } } },
//        effect = { it[0] ?. let { SuperCall(it(0)) } ?: Super })
//
//    fun thisExpr() = build(
//        syntax = { seq { `this`() && maybe { args() } } },
//        effect = { it[0] ?. let { ThisCall(it(0)) } ?: This })
//
//    fun idenOrMethodExpr() = build(
//        syntax = { seq { iden() && maybe { args() } } },
//        effect = { it[1] ?. let { MethodCall(null, listOf(), it(0), it(1)) } ?: Identifier(it(0)) })
//
//    fun classExpr() = build(
//        syntax = { seq { stemType() && opt { typeDimSuffix() } && dot() && `class`() } },
//        effect = { ClassExpr(it(0)) })
//
//    fun parExpr()
//        = seq { `(`() && expr() && `)`() }
//
//    fun methodRef() = build(
//        syntax = { seq { stemType() && opt { typeDimSuffix() } && dcolon() && typeArgs() && iden() } },
//        effect = { MaybeBoundMethodReference(it(0), it(1), it(2)) })
//
//    fun newRef() = build(
//        syntax = { seq { stemType() && opt { typeDimSuffix() } && dcolon() && typeArgs() && new() } },
//        effect = { NewReference(it(0), it(1)) })
//
//    fun newRefSuffix() = build(2,
//        syntax = { new() },
//        effect = { NewReference(it(0), it(1)) })
//
//    fun methodRefSuffix() = build(2,
//        syntax = { iden() },
//        effect = { MaybeBoundMethodReference(it(0), it(1), it(2)) })
//
//    fun refSuffix()
//        = seq { dcolon() && typeArgs() && choice { newRefSuffix() || methodRefSuffix() } }
//
//    fun classExprSuffix() = build(1,
//        syntax = { seq { dot() && `class`() } },
//        effect = { ClassExpr(it(0)) })
//
//    fun typeSuffixExpr()
//        = seq { stemType() && opt { typeDimSuffix() } && choice { refSuffix() || classExprSuffix() } }
//
//    fun primaryExpr()
//        = choice { parExpr() || arrayCtorCall() || ctorCall() || typeSuffixExpr() || idenOrMethodExpr() || thisExpr() || superExpr() || literal() }
//
//    fun dotThis() = build(1,
//        syntax = { `this`() },
//        effect = { DotThis(it(0)) })
//
//    fun dotSuper() = build(1,
//        syntax = { `super`() },
//        effect = { DotSuper(it(0)) })
//
//    fun dotIden() = build(1,
//        syntax = { iden() },
//        effect = { DotIden(it(0), it(1)) })
//
//    fun dotNew() = build(1,
//        syntax = { ctorCall() },
//        effect = { DotNew(it(0), it(1)) })
//
//    fun dotMethod() = build(1,
//        syntax = { seq { typeArgs() && iden() && args() } },
//        effect = { MethodCall(it(0), it(1), it(2), it(3)) })
//
//    fun dotPostfix()
//        = choice { dotMethod() || dotIden() || dotThis() || dotSuper() || dotNew() }
//
//    fun refPostfix() = build(1,
//        syntax = { seq { dcolon() && typeArgs() && iden() } },
//        effect = { BoundMethodReference(it(0), it(1), it(2)) })
//
//    fun arrayPostfix() = build(1,
//        syntax = { seq { lsbra() && expr() && rsbra() } },
//        effect = { ArrayAccess(it(0), it(1)) })
//
//    fun incSuffix() = build(1,
//        syntax = { `++`() },
//        effect = { PostIncrement(it(0)) })
//
//    fun decSuffix() = build(1,
//        syntax = { `--`() },
//        effect = { PostDecrement(it(0)) })
//
//    fun postfix()
//        = seq { dot() && choice { dotMethod() || dotIden() || dotThis() || dotSuper() || dotNew() || arrayPostfix() || incSuffix() || decSuffix() || refPostfix() } }
//
//    fun postfixExpr()
//        = seq { primaryExpr() && repeat0 { postfix() } }
//
//    fun incPrefix() = build(
//        syntax = { seq { `++`() && prefix_expr() } },
//        effect = { PreIncrement(it(0)) })
//
//    fun decPrefix() = build(
//        syntax = { seq { `--`() && prefix_expr() } },
//        effect = { PreDecrement(it(0)) })
//
//    fun unaryPlus() = build(
//        syntax = { seq { `+`() && prefix_expr() } },
//        effect = { UnaryPlus(it(0)) })
//
//    fun unaryMinus() = build(
//        syntax = { seq { `-`() && prefix_expr() } },
//        effect = { UnaryMinus(it(0)) })
//
//    fun complement() = build(
//        syntax = { seq { `~`() && prefix_expr() } },
//        effect = { Complement(it(0)) })
//
//    fun not() = build(
//        syntax = { seq { `!`() && prefix_expr() } },
//        effect = { Not(it(0)) })
//
//    fun cast() = build(
//        syntax = { seq { `(`() && typeUnion() && `)`() && choice { lambda() || prefix_expr() } } },
//        effect = { Cast(it(0), it(1)) })
//
//    fun prefixExpr()
//        = choice { incPrefix() || decPrefix() || unaryPlus() || unaryMinus() || complement() || not() || cast() || postfixExpr() }
//
//    fun expr()
//        = "".str
//
//    /// STATEMENTS =================================================================================
//
//    fun ifStmt() = build(
//        syntax = { seq { `if`() && parExpr() && stmt() && maybe { seq { `else`() && stmt() } } } },
//        effect = { If(it(0), it(1), it(2)) })
//
//    fun exprStmtList() = build(
//        syntax = { around0 ( {expr()} , {`,`()} ) },
//        effect = { it.list<Stmt>() })
//
//    fun forInit()
//        = choice { build(
//        syntax = { seq { modifiers() && varDeclNoSemi() } },
//        effect = { it.list<Stmt>() }) || exprStmtList() }
//
//    fun basicForStmt() = build(
//        syntax = { seq { `for`() && `(`() && forInit() && semi() && maybe { expr() } && semi() && opt { exprStmtList() } && `)`() && stmt() } },
//        effect = { BasicFor(it(0), it(1), it(2), it(3)) })
//
//    fun forVarDecl()
//        = seq { modifiers() && type() && varDeclaratorID() && colon() && expr() }
//
//    fun enhancedForStmt() = build(
//        syntax = { seq { `for`() && `(`() && forVarDecl() && `)`() && stmt() } },
//        effect = { EnhancedFor(it(0), it(1), it(2), it(3), it(4)) })
//
//    fun whileStmt() = build(
//        syntax = { seq { `while`() && parExpr() && stmt() } },
//        effect = { WhileStmt(it(0), it(1)) })
//
//    fun doWhileStmt() = build(
//        syntax = { seq { `do`() && stmt() && `while`() && parExpr() && semi() } },
//        effect = { DoWhileStmt(it(0), it(1)) })
//
//    fun catchParameterTypes() = build(
//        syntax = { around0 ( {type()} , {`|`()} ) },
//        effect = { it.list<Type>() })
//
//    fun catchParameter()
//        = seq { modifiers() && catchParameterTypes() && varDeclaratorID() }
//
//    fun catchClause() = build(
//        syntax = { seq { catch() && `(`() && catchParameter() && `)`() && block() } },
//        effect = { CatchClause(it(0), it(1), it(2), it(3)) })
//
//    fun catchClauses() = build(
//        syntax = { repeat0 { catchClause() } },
//        effect = { it.list<CatchClause>() })
//
//    fun finallyClause()
//        = seq { finally() && block() }
//
//    fun resource() = build(
//        syntax = { seq { modifiers() && type() && varDeclaratorID() && `=`() && expr() } },
//        effect = { TryResource(it(0), it(1), it(2), it(3)) })
//
//    fun resources() = build(
//        syntax = { opt { seq { `(`() && around1 ( {resource()} , {semi()} ) && `)`() } } },
//        effect = { it.list<TryResource>() })
//
//    fun tryStmt() = build(
//        syntax = { seq { `try`() && resources() && block() && catchClauses() && maybe { finallyClause() } } },
//        effect = { TryStmt(it(0), it(1), it(2), it(3)) })
//
//    fun defaultLabel() = build(
//        syntax = { seq { default() && colon() } },
//        effect = { DefaultLabel })
//
//    fun caseLabel() = build(
//        syntax = { seq { case() && expr() && colon() } },
//        effect = { CaseLabel(it(0)) })
//
//    fun switchLabel()
//        = choice { caseLabel() || defaultLabel() }
//
//    fun switchClause() = build(
//        syntax = { seq { switchLabel() && stmts() } },
//        effect = { SwitchClause(it(0), it(1)) })
//
//    fun switchStmt() = build(
//        syntax = { seq { switch() && parExpr() && `{`() && repeat0 { switchClause() } && `}`() } },
//        effect = { SwitchStmt(it(0), it.list(1)) })
//
//    fun synchronizedStmt() = build(
//        syntax = { seq { synchronized() && parExpr() && block() } },
//        effect = { SynchronizedStmt(it(1), it(2)) })
//
//    fun returnStmt() = build(
//        syntax = { seq { `return`() && maybe { expr() } && semi() } },
//        effect = { ReturnStmt(it(0)) })
//
//    fun throwStmt() = build(
//        syntax = { seq { `throw`() && expr() && semi() } },
//        effect = { ThrowStmt(it(0)) })
//
//    fun breakStmt() = build(
//        syntax = { seq { `break`() && maybe { iden() } && semi() } },
//        effect = { BreakStmt(it(0) })
//
//    fun continueStmt() = build(
//        syntax = { seq { `continue`() && maybe { iden() } && semi() } },
//        effect = { ContinueStmt(it(0)) })
//
//    fun assertStmt() = build(
//        syntax = { seq { assert() && expr() && maybe { seq { colon() && expr() } } && semi() } },
//        effect = { AssertStmt(it(0), it(1)) })
//
//    fun semiStmt() = build(
//        syntax = { semi() },
//        effect = { SemiStmt })
//
//    fun exprStmt()
//        = seq { expr() && semi() }
//
//    fun labelledStmt() = build(
//        syntax = { seq { iden() && colon() && stmt() } },
//        effect = { LabelledStmt(it(0), it(1)) })
//
//    fun stmt()
//        = choice { block() || ifStmt() || basicForStmt() || enhancedForStmt() || whileStmt() || doWhileStmt() || tryStmt() || switchStmt() || synchronizedStmt() || returnStmt() || throwStmt() || breakStmt() || continueStmt() || assertStmt() || semiStmt() || exprStmt() || labelledStmt() || varDecl() || typeDecl() }
//
//    fun block() = build(
//        syntax = { seq { `{`() && repeat0 { stmt() } && `}`() } },
//        effect = { Block(it.list()) })
//
//    fun stmts() = build(
//        syntax = { repeat0 { stmt() } },
//        effect = { it.list<Stmt>() })
//
//    /// TOP-LEVEL ==================================================================================
//
//    fun packageDecl() = build(
//        syntax = { seq { annotations() && `package`() && qualifiedIden() && semi() } },
//        effect = { Package(it(0), it(1)) })
//
//    fun importDecl() = build(
//        syntax = { seq { import() && as_bool { static() } && qualifiedIden() && as_bool { seq { dot() && `*`() } } && semi() } },
//        effect = { Import(it(0), it(1), it(2)) })
//
//    fun importDecls() = build(
//        syntax = { repeat0 { importDecl() } },
//        effect = { it.list<Import>() })
//
//    override fun root() = build(
//        syntax = { seq { whitespace() && maybe { packageDecl() } && importDecls() && typeDecls() } },
//        effect = { File(it(0), it(1), it(2)) })
//}