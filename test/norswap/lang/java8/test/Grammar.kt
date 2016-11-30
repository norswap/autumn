package norswap.lang.java8.test
import norswap.autumn.Grammar
import norswap.autumn.PartialMatch
import norswap.autumn.UnexpectedToken
import norswap.autumn.test.*
import norswap.lang.java8.Java8Grammar
import norswap.lang.java8.ast.*
import norswap.lang.java8.ast.Annotation
import org.testng.annotations.*
import kotlin.collections.listOf as l

class Grammar: GrammarFixture()
{
    // ---------------------------------------------------------------------------------------------

    override val g = Java8Grammar()

    // ---------------------------------------------------------------------------------------------

    val o = emptyList<Nothing>()
    val marker = MarkerAnnotation(l("Marker"))
    val T = ClassType(l(ClassTypePart(o, "T", o)))
    val dim = Dimension(o)

    fun prim   (name: String) = PrimitiveType(o, name)
    fun cpart  (name: String) = ClassTypePart(o, name, o)
    fun sclass (name: String) = ClassType(l(cpart(name)))

    fun sclass (ann: List<Annotation>,
                name: String,
                targs: List<Type>)
        = ClassType(l(ClassTypePart(ann, name, targs)))

    // ---------------------------------------------------------------------------------------------

    @Test fun literals() {
        top { g.literal() }
        success_expect("0", Literal(0))
        success_expect("0L", Literal(0L))
        success_expect("42", Literal(42))
        success_expect("4_2", Literal(42))
        success_expect("42l", Literal(42L))
        success_expect("4_2L", Literal(42L))
        success_expect("42.", Literal(42.0))
        success_expect(".42", Literal(0.42))
        success_expect("4_2.", Literal(42.0))
        success_expect(".4_2", Literal(0.42))
        success_expect("42f", Literal(42f))
        success_expect("42d", Literal(42.0))
        success_expect(".42f", Literal(0.42f))
        success_expect("42e42", Literal(42e42))
        success_expect("42e+4_2", Literal(42e42))
        success_expect(".42e42", Literal(0.42e42))
        success_expect(".42e38f", Literal(0.42e38f))
        success_expect("42.42e+24f", Literal(42.42e24f))
        success_expect("42.e-42F", Literal(42e-42f))
        success_expect("0x8", Literal(8))
        success_expect("0x1_8", Literal(24))
        success_expect("0x8p0", Literal(8.0))
        success_expect("0x8p8", Literal((8 * 2 shl 7).toDouble()))
        success_expect("0x8p0_8", Literal((8 * 2 shl 7).toDouble()))
        success_expect("0x8.8p0", Literal(8.5))
        success_expect("0x8.8p0d", Literal(8.5))
        success_expect("0x8p0f", Literal(8f))
        success_expect("0111", Literal(73))
        success_expect("0111L", Literal(73L))
        success_expect("01_1__1", Literal(73))
        success_expect("0B111", Literal(7))
        success_expect("0b1_0__1L", Literal(5L))
        success_expect("true", Literal(true))
        success_expect("false", Literal(false))
        success_expect("null", Literal(Null))
        success_expect("\"\\u07FF\"", Literal("\u07FF"))
        success_expect("'a'", Literal('a'))
        success_expect("\"\\177\"", Literal("\u007F"))
        success_expect("'\\177'", Literal('\u007F'))
        success_expect("'\\u07FF'", Literal('\u07FF'))
        failure_at("#", 0, UnexpectedToken)
        failure_at("identifier", 0, UnexpectedToken)
        failure_at("_42", 0, UnexpectedToken)
        failure_at("42_", 2, PartialMatch)

        success_expect("0f", Literal(0f))
        success_expect("0d", Literal(0.0))
        success_expect("0e999999999f", Literal(0f))
        success_expect("0e999999999", Literal(0.0))
        success_expect("0x0p0f", Literal(0f))
        success_expect("0x0p0", Literal(0.0))
        success_expect("0x0p999999999f", Literal(0f))
        success_expect("0x0p999999999", Literal(0.0))

        success_issue(".42e-48f", Literal(0.0f),
                "Float literal is too small: rounded to 0.")
        success_issue("42.42e+42f", Literal(Float.POSITIVE_INFINITY),
                "Float literal is too big: rounded to infinity.")
        success_issue("0.1e-999", Literal(0.0),
                "Double literal is too small: rounded to 0.")
        success_issue("42e999", Literal(Double.POSITIVE_INFINITY),
                "Double literal is too big: rounded to infinity.")

        success_issue("0x42p-999f", Literal(0.0f),
                "Float literal is too small: rounded to 0.")
        success_issue("0x42p999f", Literal(Float.POSITIVE_INFINITY),
                "Float literal is too big: rounded to infinity.")
        success_issue("0x42p-9999", Literal(0.0),
                "Double literal is too small: rounded to 0.")
        success_issue("0x42p9999", Literal(Double.POSITIVE_INFINITY),
                "Double literal is too big: rounded to infinity.")

        success_issue("9999999999", Literal(Int.MAX_VALUE),
                "Integer literal is too big: rounded to max value.")
        success_issue("9999999999999999999L", Literal(Long.MAX_VALUE),
                "Long literal is too big: rounded to max value.")
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun annotations()
    {
        top { g.annotation() }

        val hairy = "42"
        val hyval = Literal(42)
        // candidate: "true ? x.y : x.y()[1]"

        success_expect("@Marker",
            marker)
        success_expect("@Marker()",
            MarkerAnnotation(l("Marker")))
        success_expect("@java.util.Marker()",
            MarkerAnnotation(l("java", "util", "Marker")))
        success_expect("@Single($hairy)",
            SingleElementAnnotation(l("Single"), hyval))
        success_expect("@Single(@Marker)",
            SingleElementAnnotation(l("Single"), marker))
        success_expect("@java.util.Single(@java.util.Marker)",
            SingleElementAnnotation(l("java", "util", "Single"), MarkerAnnotation(l("java", "util", "Marker"))))
        success_expect("@Single({@Marker, $hairy})",
            SingleElementAnnotation(l("Single"), AnnotationElementList(l(marker, hyval))))
        success_expect("@Single({})",
            SingleElementAnnotation(l("Single"), AnnotationElementList(l())))
        success_expect("@Single({,})",
            SingleElementAnnotation(l("Single"), AnnotationElementList(l())))
        success_expect("@Single({x,})",
            SingleElementAnnotation(l("Single"), AnnotationElementList(l(Identifier("x")))))
        success_expect("@Single(x)",
            SingleElementAnnotation(l("Single"), Identifier("x")))
        success_expect("@Pairs(x = @Marker)",
            NormalAnnotation(l("Pairs"), l("x" to marker)))
        success_expect("@Pairs(x = @Marker, y = $hairy, z = {@Marker, $hairy}, u = x)",
            NormalAnnotation(l("Pairs"), l(
                "x" to marker,
                "y" to hyval,
                "z" to AnnotationElementList(l(marker, hyval)),
                "u" to Identifier("x"))))

        // TODO multipart annotation name
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun types()
    {
        top { g.type() }

        success_expect("char",      prim("char"))
        success_expect("int",       prim("int"))
        success_expect("double",    prim("double"))
        success_expect("void",      prim("void"))
        success_expect("String",    sclass("String"))

        success_expect("java.util.String",
            ClassType(l(cpart("java"), cpart("util"), cpart("String"))))
        success_expect("List<?>",
            sclass(o, "List", l(Wildcard(o, null))))
        success_expect("List<T>",
            sclass(o, "List", l(T)))
        success_expect("List<? super T>",
            sclass(o, "List", l(Wildcard(o, SuperBound(T)))))
        success_expect("List<? extends T>",
            sclass(o, "List", l(Wildcard(o, ExtendsBound(T)))))

        success("java.util.List<?>")
        success("java.util.List<T>")
        success("java.util.List<? super T>")

        success_expect("char[]",
            ArrayType(prim("char"), l(dim)))
        success_expect("int[][][]",
            ArrayType(prim("int"), l(dim, dim, dim)))
        success_expect("T[]",
            ArrayType(T, l(dim)))
        success_expect("List<T>[][]",
            ArrayType(sclass(o, "List", l(T)), l(dim, dim)))

        success("java.util.String[][]")
        success("List<?>[]")
        success("List<? super T>[]")
        success("List<? extends T>[][]")
        success("java.util.List<?>[]")
        success("java.util.List<T>[][]")
        success("java.util.List<? super T>[][]")

        success_expect("List<List<T>>",
            sclass(o, "List", l(sclass(o, "List", l(T)))))
        success_expect("List<? extends List<? super T>>",
            sclass(o, "List", l(Wildcard(o, ExtendsBound(sclass(o, "List", l(Wildcard(o, SuperBound(T)))))))))

        success_expect("@Marker int",
            PrimitiveType(l(marker), "int"))
        success_expect("@Marker java.@test.Mbrker util . @Mcrker String",
            ClassType(l(
                ClassTypePart(l(marker), "java", o),
                ClassTypePart(l(MarkerAnnotation(l("test", "Mbrker"))), "util", o),
                ClassTypePart(l(MarkerAnnotation(l("Mcrker"))), "String", o))))
        success_expect("List<@Marker ?>",
            sclass(o, "List", l(Wildcard(l(marker), null))))
        success_expect("List<? extends @Marker T>",
            sclass(o, "List", l(Wildcard(o, ExtendsBound(ClassType(l(ClassTypePart(l(marker), "T", o))))))))
        success("List<@Marker ? extends @Marker T>")
        success_expect("@Marker int @Mbrker []",
            ArrayType(PrimitiveType(l(marker), "int"), l(Dimension(l(MarkerAnnotation(l("Mbrker")))))))
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun primary_expression()
    {
        top { g.expr() }
        // top { javag.primary_expr() } // to diagnose

        success_expect("1", Literal(1))
        success_expect("iden", Identifier("iden"))
        success_expect("iden()", MethodCall(null, o, "iden", o))
        success_expect("iden(1, x)", MethodCall(null, o, "iden", l(Literal(1), Identifier("x"))))
        success_expect("(1)", ParenExpr(Literal(1)))
        success_expect("this", This)
        success_expect("super", Super)
        success_expect("this()", ThisCall(o))
        success_expect("super()", SuperCall(o))
        success_expect("this(1, x)", ThisCall(l(Literal(1), Identifier("x"))))
        success_expect("super(1, x)", SuperCall(l(Literal(1), Identifier("x"))))
        success_expect("new String()", CtorCall(o, sclass("String"), o, null))
        success_expect("new <T> Test()", CtorCall(l(T), sclass("Test"), o, null))
        success_expect("new Test<T>()", CtorCall(o, sclass(o, "Test", l(T)), o, null))
        success_expect("void.class", ClassExpr(prim("void")))
        success_expect("int.class", ClassExpr(prim("int")))
        success_expect("List.class", ClassExpr(sclass("List")))
        success_expect("java.util.List.class",
            ClassExpr(ClassType(l(cpart("java"), cpart("util"), cpart("List")))))
        success_expect("new int[42]",
            ArrayCtorCall(prim("int"), l(DimExpr(o, Literal(42))), o, null))
        success_expect("new int[42][]",
            ArrayCtorCall(prim("int"), l(DimExpr(o, Literal(42))), l(dim), null))
        success_expect("new int[1][2][][]",
            ArrayCtorCall(prim("int"),
                l(DimExpr(o, Literal(1)), DimExpr(o, Literal(2))),
                l(dim, dim), null))
        success_expect("new int[] { 1, 2, 3 }",
            ArrayCtorCall(prim("int"), o, l(dim), ArrayInit(l(Literal(1), Literal(2), Literal(3)))))
        success_expect("new int[] { 1, 2, }",
            ArrayCtorCall(prim("int"), o, l(dim), ArrayInit(l(Literal(1), Literal(2)))))
        success_expect("new int[] { , }",
            ArrayCtorCall(prim("int"), o, l(dim), ArrayInit(o)))
        success_expect("new int[][] { {1, 2}, {3, 4} }",
            ArrayCtorCall(prim("int"), o, l(dim, dim), ArrayInit(l(
                ArrayInit(l(Literal(1), Literal(2))),
                ArrayInit(l(Literal(3), Literal(4)))))))
        success_expect("new List<String>[1]",
            ArrayCtorCall(ClassType(l(ClassTypePart(o, "List", l(sclass("String"))))),
                l(DimExpr(o, Literal(1))), o, null))

        success_expect("Foo::bar", MaybeBoundMethodReference(sclass("Foo"), o, "bar"))
        success_expect("Foo::new", NewReference(sclass("Foo"), o))
        success_expect("Foo::<T>bar", MaybeBoundMethodReference(sclass("Foo"), l(T), "bar"))
        success_expect("Foo::<T, V>new", NewReference(sclass("Foo"), l(T, sclass("V"))))
        success_expect("List.Foo::<T>bar",
            MaybeBoundMethodReference(ClassType(l(cpart("List"), cpart("Foo"))), l(T), "bar"))
    }

    // ---------------------------------------------------------------------------------------------

    // NOTE(norswap): from this point onwards, tests are easily perfectible

    // ---------------------------------------------------------------------------------------------

    @Test fun postfix()
    {
        top { g.expr() }
        // top { javag.prefix_expr() } // to diagnose

        success("foo.this")
        success("foo.super")
        success("foo.m()")
        success("foo.<T, U>m(a, b)")
        success("foo.bar")
        success("foo.new Bar()")
        success("foo.new <U> Bar(a, b)")
        success("foo++")
        success("foo--")
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun prefix()
    {
        top { g.expr() }
        // top { javag.prefix_expr() } // to diagnose

        success("++1")
        success("--1")
        success("+1")
        success("-1")
        success("~1")
        success("!true")
        success("(String) obj")
        success("++ x.y")
        success("-- new Integer(1)")
        success("!x.y(1)")
        success("(String & Serializable & Cloneable) obj.x")
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun left_assoc_binary()
    {
        top { g.expr() }

        success("1 * 1")
        success("1/1")
        success("2%3.")
        success("x+1")
        success("1-x")
        success("1 << 3")
        success("16 >> 3")
        success("16 >>> 3")
        success("1 < 3")
        success("1 <= 3")
        success("1 > 3")
        success("1 >= 3")
        success("1 == 1")
        success("1 != 1")
        success("8 & 1")
        success("8 ^ 1")
        success("8 | 1")
        success("true && false")
        success("true || false")

        success("1 * 1")
        success("1/1")
        success("2%3.")
        success("x+1")
        success("1-x")
        success("1 << 3")
        success("16 >> 3")
        success("16 >>> 3")
        success("1 < 3")
        success("1 <= 3")
        success("1 > 3")
        success("1 >= 3")
        success("1 == 1")
        success("1 != 1")
        success("8 & 1")
        success("8 ^ 1")
        success("8 | 1")
        success("true && false")
        success("true || false")

        success("1 * 2 + 3 << 4")
        success("1 << 2 + 3 * 4")
        success("1 << 12 << 2 + 23 + 3 * 34 * 4")
        success("1 || 2 && 3 | 4 ^ 5 & 6 == 7 > 8 >> 9 + 10 * 11")
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun assignment()
    {
        top { g.expr() }

        success("x = 3")
        success("x += 3")
        success("x -= 4")
        success("x *= 3")
        success("x /= 3")
        success("x %= 3")
        success("x <<= 3")
        success("x >>= 3")
        success("x >>>= 3")
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun lambda() // no body
    {
        top { g.expr() }
        // top { javag.lambda() } // to diagnose

        success("x -> {}")
        success("x -> expr[1].lol")
        success("() -> {}")
        success("() -> expr[1].lol")
        success("(x) -> {}")
        success("(x) -> expr[1].lol")
        success("(x, y) -> {}")
        success("(x, y) -> expr[1].lol")
        success("(String x) -> {}")
        success("(String x) -> expr[1].lol")
        success("(String x, int y) -> {}")
        success("(String x, int y) -> expr[1].lol")
        success("(String... x) -> lol")
        success("(int x, int @Annot ... ys) -> lol")
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun type_decls_no_body()
    {
        top { g.type_decl() }

        success("interface Hello {}")
        success("@interface Hello {}")
        success("class Hello {}")
        success("enum Hello {}")
        success("@Annot private interface Hello<T> {}")
        success("interface Hello<@Annot T extends String & Cloneable> {}")
        success("interface Hello<T> extends A, B, C {}")
        success("class Hello<T> extends World implements A, B, C {}")
        success("@Annot public enum X {;}")
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun class_body_decl()
    {
        top { g.class_body_decl() }

        success("int x;")
        success("int x = 1;")
        success("int x = 1, y;")
        success("@Annot final List<String> name = x, stuff[] = array();")
        success("@Annot <T extends Stuff & Thing, X extends List<?>> String meth (int x, int y)[] ;")
        success("void meth() throws Exception {}")
        success("@Annot <T extends Stuff> MyClass (int x) throws Error {}")
        // todo tmp
//        success("static { someFunc(); myVar = x; }")
//        success("{ myVar = x; }")
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun statements()
    {
        top { g.stmt() }

        success("int x;")
        success("int x = 1;")
        success("int x = 1, y;")
        success("@Annot final List<String> name = x, stuff[] = array();")
        success("if (true) ; else ;")
        success("for (;;) ;")
        success("for (int i = 0 ; true ; ++i, ++j) ;")
        success("for (i = 0, j = 0 ; i < 10 ; ++i) ;")
        success("for (@Annot String x : list) ;")
        success("while (true) ;")
        success("do ; while (true);")
        success("try {} catch (Exception e) {} finally {}")
        success("try {} finally {}")
        success("try {} catch (Exception e) {}")
        success("try {} catch (@Annot Exception|Error e) {}")
        success("try (@Annot Resource res[] = myres) {} finally {}")
        success("try (Resource res[] = myres ; Resource x = youpie()) {} finally {}")
        success("switch (x) { case 1: dox(); doy(); case 2: case 3: doz(); break; default: dou();}")
        success("switch (x) {}")
        success("synchronized (expr) {}")
        success("return;")
        success("return x;")
        success("throw new Exception(lol);")
        success("assert x || y && z : lol;")
        success("assert x || y;")
        success("continue;")
        success("continue label;")
        success("break;")
        success("break label;")
        success("label: funCall();")
        success("label: while (x) {}")
        success(";")
        success("funcall();")
        success("++i;")
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun type_decls_with_bodies()
    {
        top { g.type_decl() }

        success("class C { @Annot final int var = 0; }")
        success("class C { @Annot private @Annut void method(String x) { return x; }}")
        success("interface I { void meth(int x); }")
        success("class C { class D {} }")
        success("class C { int x; static class D {}; void test(int x) {} }")
        success("@interface AI { String value(); }")
        success("@interface AI { Annot value() default @Annot; }")
        success("@interface AI { @Annot String value()[] default \"hello\" ; }")
        success("@interface AI { @interface AI2 {} }")
        success("enum E { ; }")
        success("enum E { X, Y, Z }")
        success("enum E { X, Y, Z; }")
        success("enum E { @Annot X (x) { void test(); }, Y } ")
        success("enum E { X(), Y; void test(int x); int y; }")
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun diagnose()
    {
        top { g.expr() }
        success("predicate = request -> true")
    }

    // ---------------------------------------------------------------------------------------------

    // TODO
    // - lambda with bodies
    // - expressions with lambda sub-expressions
    // - bound method reference test case
    // - top level semicolon test case
    // - test instanceof
    //      - if (!(mi instanceof ProxyMethodInvocation)) {}
}
