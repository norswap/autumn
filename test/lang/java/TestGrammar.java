package lang.java;

import norswap.autumn.Grammar;
import norswap.autumn.AutumnTestFixture;
import norswap.lang.java.JavaGrammar;
import norswap.lang.java.LexUtils.LexProblem;
import norswap.lang.java.ast.*;
import norswap.utils.NArrays;
import norswap.utils.data.wrappers.Pair;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static norswap.lang.java.ast.BasicType.*;
import static norswap.utils.exceptions.Exceptions.suppress;
import static norswap.utils.Util.cast;
import static norswap.utils.Vanilla.list;

@SuppressWarnings("FieldMayBeFinal")
public class TestGrammar extends AutumnTestFixture
{
    // ---------------------------------------------------------------------------------------------

    private final Object grammar;
    private final Class<?> grammarClass;

    // ---------------------------------------------------------------------------------------------

    /**
     * Use this constructor in subclasses to test alternative Java grammars that use the same rule
     * names as the original.
     */
    public TestGrammar (Object grammar) {
        this.grammar = grammar;
        this.grammarClass = grammar.getClass();
    }

    // ---------------------------------------------------------------------------------------------

    public TestGrammar() {
        this(new JavaGrammar());
    }

    // ---------------------------------------------------------------------------------------------

    private Grammar.rule rule(String name) {
        return cast(suppress(() -> grammarClass.getField(name).get(grammar)));
    }

    // ---------------------------------------------------------------------------------------------

    private List<Identifier> idList (String... strings)
    {
        return Arrays.asList(NArrays.map(strings, new Identifier[0], Identifier::mk));
    }

    // ---------------------------------------------------------------------------------------------

    private static List<TAnnotation>    noAnnotations   = Collections.emptyList();
    private static List<TType>          noTypeArgs      = Collections.emptyList();
    private static List<Expression>     noArgs          = Collections.emptyList();
    private static List<DimExpression>  noDimExprs      = Collections.emptyList();
    private static List<Dimension>      noDims          = Collections.emptyList();

    // identifier called "marker"
    private static TAnnotation marker = MarkerAnnotation.mk(list(Identifier.mk("Marker")));

    // []
    private static Dimension dim = Dimension.mk(noAnnotations);

    // primitive type (int, float, etc)
    private static PrimitiveType prim (BasicType type) {
        return PrimitiveType.mk(noAnnotations, type);
    }

    // an identifier part of a class name
    private static ClassTypePart cpart (String name) {
        return ClassTypePart.mk(noAnnotations, Identifier.mk(name), noTypeArgs);
    }

    // a class name with a single identifier and the given type arguments
    private static ClassType sclass (String name, List<TType> typeArgs) {
        return ClassType.mk(list(
            ClassTypePart.mk(noAnnotations, Identifier.mk(name), typeArgs)));
    }

    // a class name with a single identifier and no type arguments
    private static ClassType sclass (String name) {
        return sclass(name, noTypeArgs);
    }

    // class name "T"
    private static ClassType T = ClassType.mk(list(cpart("T")));

    // ---------------------------------------------------------------------------------------------

    @SuppressWarnings("OctalInteger")
    @Test public void literals()
    {
        rule = rule("literal");

        successExpect("4_2L",          Literal.mk(4_2L));
        successExpect(".42e42",        Literal.mk(.42e42));
        successExpect("0x8",           Literal.mk(0x8));
        successExpect("0x8p8",         Literal.mk(0x8p8));
        successExpect("0111",          Literal.mk(0111));
        successExpect("true",          Literal.mk(true));
        successExpect("false",         Literal.mk(false));
        successExpect("null",          Literal.mk(Null.NULL));
        successExpect("\"\\u07FF\"",   Literal.mk("\u07FF"));
        successExpect("'a'",           Literal.mk('a'));
        successExpect("\"\\177\"",     Literal.mk("\u007F"));
        successExpect("'\\177'",       Literal.mk('\u007F'));
        successExpect("'\\u07FF'",     Literal.mk('\u07FF'));
        successExpect("\"🦆\"",         Literal.mk("🦆"));
        successExpect("\"birb: 𓅭\"",  Literal.mk("birb: 𓅭"));

        // From Spring
        successExpect("\"owfie   fue&3[][[[2 \\n\\n \\r  \\t 8\\ufffd3\"",
            Literal.mk("owfie   fue&3[][[[2 \n\n \r  \t 8\ufffd3"));

        failure("#");
        failure("identifier");
        failure("_42");

        // TODO test bad hex escape (e.g. 3 or 5 digits instead of 4)

        // NOTE(norswap): stopgap to account for the fact that JavaGrammar and JavaGrammarTokens are
        //   each more permissive in certain scenarios.
        if (!getClass().equals(TestGrammarTokens.class))
        {
            failure("42_");

            successExpect(".42e-48f",
                Literal.mk(new LexProblem("Float literal is too small.")));
            successExpect("42.42e+42f",
                Literal.mk(new LexProblem("Float literal is too big.")));
            successExpect("0.1e-999",
                Literal.mk(new LexProblem("Double literal is too small.")));
            successExpect("42e999",
                Literal.mk(new LexProblem("Double literal is too big.")));

            successExpect("0x42p-999f",
                Literal.mk(new LexProblem("Float literal is too small.")));
            successExpect("0x42p999f",
                Literal.mk(new LexProblem("Float literal is too big.")));
            successExpect("0x42p-9999",
                Literal.mk(new LexProblem("Double literal is too small.")));
            successExpect("0x42p9999",
                Literal.mk(new LexProblem("Double literal is too big.")));

            successExpect("9999999999",
                Literal.mk(new LexProblem("Integer literal is too big.")));
            successExpect("9999999999999999999L",
                Literal.mk(new LexProblem("Long literal is too big.")));
        }
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void annotations()
    {
        rule = rule("annotation");

        String hairy = "true ? x.y : x.y()[1]";
        Expression hval = TernaryExpression.mk(
            Literal.mk(true),
            DotIden.mk(Identifier.mk("x"), Identifier.mk("y")),
            ArrayAccess.mk(
                MethodCall.mk(Identifier.mk("x"), noTypeArgs, Identifier.mk("y"), noArgs),
                Literal.mk(1)));

        successExpect("@Marker",
            marker);
        successExpect("@Marker()",
            marker);
        successExpect("@java.util.Marker()",
            MarkerAnnotation.strings("java", "util", "Marker"));
        successExpect("@Single(" + hairy + ")",
            SingleElementAnnotation.mk(idList("Single"), hval));
        successExpect("@Single(@Marker)",
            SingleElementAnnotation.mk(idList("Single"), marker));

        successExpect("@java.util.Single(@java.util.Marker)",
            SingleElementAnnotation.mk(
                idList("java", "util", "Single"),
                MarkerAnnotation.strings("java", "util", "Marker")));

        successExpect("@Single({@Marker, " + hairy + "})",
            SingleElementAnnotation.mk(
                idList("Single"),
                AnnotationElementList.mk(list(marker, hval))));

        successExpect("@Single({})",
            SingleElementAnnotation.mk(idList("Single"), AnnotationElementList.mk(list())));

        successExpect("@Single({,})",
            SingleElementAnnotation.mk(idList("Single"), AnnotationElementList.mk(list())));

        successExpect("@Single({x,})",
            SingleElementAnnotation.mk(
                idList("Single"),
                AnnotationElementList.mk(list(Identifier.mk("x")))));

        successExpect("@Single(x)",
            SingleElementAnnotation.mk(idList("Single"), Identifier.mk("x")));

        successExpect("@Pairs(x = @Marker)",
            NormalAnnotation.mk(
                list(Identifier.mk("Pairs")),
                list(new Pair<>(Identifier.mk("x"), marker))));

        successExpect("@Pairs(x = @Marker, y = " + hairy + ", z = {@Marker, " + hairy + "}, u = x)",
            NormalAnnotation.mk(list(Identifier.mk("Pairs")), list(
                new Pair<>(Identifier.mk("x"), marker),
                new Pair<>(Identifier.mk("y"), hval),
                new Pair<>(Identifier.mk("z"), AnnotationElementList.mk(list(marker, hval))),
                new Pair<>(Identifier.mk("u"), Identifier.mk("x")))));
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void types()
    {
        rule = rule("type");

        successExpect("char",      PrimitiveType.mk(list(), _char));
        successExpect("int",       PrimitiveType.mk(list(), _int));
        successExpect("double",    PrimitiveType.mk(list(), _double));
        successExpect("void",      PrimitiveType.mk(list(), _void));

        successExpect("java.util.String",
            ClassType.mk(list(cpart("java"), cpart("util"), cpart("String"))));
        successExpect("List<?>",
            sclass("List", list(Wildcard.mk(noAnnotations, null))));
        successExpect("List<T>",
            sclass("List", list(T)));
        successExpect("List<? super T>",
            sclass("List", list(Wildcard.mk(noAnnotations, SuperBound.mk(T)))));
        successExpect("List<? extends T>",
            sclass("List", list(Wildcard.mk(noAnnotations, ExtendsBound.mk(T)))));

        success("java.util.List<?>");
        success("java.util.List<T>");
        success("java.util.List<? super T>");

        successExpect("char[]",
            ArrayType.mk(prim(_char), list(dim)));
        successExpect("int[][][]",
            ArrayType.mk(prim(_int), list(dim, dim, dim)));
        successExpect("T[]",
            ArrayType.mk(T, list(dim)));
        successExpect("List<T>[][]",
            ArrayType.mk(sclass("List", list(T)), list(dim, dim)));

        success("java.util.String[][]");
        success("List<?>[]");
        success("List<? super T>[]");
        success("List<? extends T>[][]");
        success("java.util.List<?>[]");
        success("java.util.List<T>[][]");
        success("java.util.List<? super T>[][]");

        successExpect("List<List<T>>",
            sclass("List", list(sclass("List", list(T)))));

        successExpect("List<? extends List<? super T>>",
            sclass("List", list(Wildcard.mk(noAnnotations, ExtendsBound.mk(sclass("List",
                list(Wildcard.mk(noAnnotations, SuperBound.mk(T)))))))));

        successExpect("@Marker int",
            PrimitiveType.mk(list(marker), _int));

        successExpect("@Marker java.@test.Mbrker util . @Mcrker String",
            ClassType.mk(list(
                ClassTypePart.mk(list(marker), Identifier.mk("java"), noTypeArgs),
                ClassTypePart.mk(
                    list(MarkerAnnotation.mk(list(
                        Identifier.mk("test"),
                        Identifier.mk("Mbrker")))),
                    Identifier.mk("util"),
                    noTypeArgs),
                ClassTypePart.mk(
                    list(MarkerAnnotation.mk(list(Identifier.mk("Mcrker")))),
                    Identifier.mk("String"),
                    noTypeArgs))));

        successExpect("List<@Marker ?>",
            sclass("List", list(Wildcard.mk(list(marker), null))));

        successExpect("List<? extends @Marker T>",
            sclass("List", list(Wildcard.mk(noAnnotations, ExtendsBound.mk(ClassType.mk(list(
                    ClassTypePart.mk(list(marker), Identifier.mk("T"), noTypeArgs))))))));

        success("List<@Marker ? extends @Marker T>");

        successExpect("@Marker int @Mbrker []",
            ArrayType.mk(
                PrimitiveType.mk(list(marker), _int),
                list(Dimension.mk(list(MarkerAnnotation.mk(list(Identifier.mk("Mbrker"))))))));
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void primaryExpressions()
    {
        rule = rule("expr");

        successExpect("1",
            Literal.mk(1));
        successExpect("iden",
            Identifier.mk("iden"));
        successExpect("iden𓅭",
            Identifier.mk("iden𓅭"));
        successExpect("iden𓉐",
            Identifier.mk("iden𓉐"));
        successExpect("iden𧅄",
            Identifier.mk("iden𧅄"));
        successExpect("iden()",
            MethodCall.mk(null, noTypeArgs, Identifier.mk("iden"), noArgs));
        successExpect("iden(1, x)",
            MethodCall.mk(null, noTypeArgs, Identifier.mk("iden"),
                list(Literal.mk(1), Identifier.mk("x"))));
        successExpect("(1)",
            ParenExpression.mk(Literal.mk(1)));
        successExpect("this",
            This.mk());
        successExpect("super",
            Super.mk());
        successExpect("this()",
            ThisCall.mk(noArgs));
        successExpect("super()",
            SuperCall.mk(noArgs));
        successExpect("this(1, x)",
            ThisCall.mk(list(Literal.mk(1), Identifier.mk("x"))));
        successExpect("super(1, x)",
            SuperCall.mk(list(Literal.mk(1), Identifier.mk("x"))));

        successExpect("new String()",
            ConstructorCall.mk(noTypeArgs, sclass("String", noTypeArgs), noArgs, null));
        successExpect("new <T> Test()",
            ConstructorCall.mk(list(T), sclass("Test", noTypeArgs), noArgs, null));
        successExpect("new Test<T>()",
            ConstructorCall.mk(noTypeArgs, sclass("Test", list(T)), noArgs, null));
        successExpect("void.class",
            ClassExpression.mk(prim(_void)));
        successExpect("int.class",
            ClassExpression.mk(prim(_int)));
        successExpect("List.class",
            ClassExpression.mk(sclass("List", noTypeArgs)));
        successExpect("java.util.List.class",
            ClassExpression.mk(ClassType.mk(list(cpart("java"), cpart("util"), cpart("List")))));

        successExpect("new int[42]",
            ArrayConstructorCall.mk(prim(_int),
                list(DimExpression.mk(noAnnotations, Literal.mk(42))),
                noDims, null));
        successExpect("new int[42][]",
            ArrayConstructorCall.mk(prim(_int),
                list(DimExpression.mk(noAnnotations, Literal.mk(42))),
                list(dim), null));
        successExpect("new int[1][2][][]",
            ArrayConstructorCall.mk(prim(_int),
                list(
                    DimExpression.mk(noAnnotations, Literal.mk(1)),
                    DimExpression.mk(noAnnotations, Literal.mk(2))),
                list(dim, dim), null));
        successExpect("new int[] { 1, 2, 3 }",
            ArrayConstructorCall.mk(prim(_int), noDimExprs, list(dim),
                ArrayInitializer.mk(list(Literal.mk(1), Literal.mk(2), Literal.mk(3)))));
        successExpect("new int[] { 1, 2, }",
            ArrayConstructorCall.mk(prim(_int), noDimExprs, list(dim),
                ArrayInitializer.mk(list(Literal.mk(1), Literal.mk(2)))));
        successExpect("new int[] { , }",
            ArrayConstructorCall.mk(prim(_int), noDimExprs, list(dim),
                ArrayInitializer.mk(noArgs)));
        successExpect("new int[][] { {1, 2}, {3, 4} }",
            ArrayConstructorCall.mk(prim(_int), noDimExprs, list(dim, dim),
                ArrayInitializer.mk(list(
                    ArrayInitializer.mk(list(Literal.mk(1), Literal.mk(2))),
                    ArrayInitializer.mk(list(Literal.mk(3), Literal.mk(4)))))));
        successExpect("new List<T>[1]",
            ArrayConstructorCall.mk(
                sclass("List", list(T)),
                list(DimExpression.mk(noAnnotations, Literal.mk(1))),
                noDims, null));

        successExpect("Foo::bar",
            TypeMethodReference.mk(sclass("Foo"), noTypeArgs, Identifier.mk("bar")));
        successExpect("Foo::new",
            NewReference.mk(sclass("Foo"), noTypeArgs));
        successExpect("Foo::<T>bar",
            TypeMethodReference.mk(sclass("Foo"), list(T), Identifier.mk("bar")));
        successExpect("Foo::<T, V>new",
            NewReference.mk(sclass("Foo"), list(T, sclass("V"))));
        successExpect("List.Foo::<T>bar",
            TypeMethodReference.mk(
                ClassType.mk(list(cpart("List"), cpart("Foo"))), list(T), Identifier.mk("bar")));

        successExpect("newClass()",
            MethodCall.mk(null, noTypeArgs, Identifier.mk("newClass"), noArgs));

        failure("while");
        failure("interface"); // interesting because "int" is a prefix
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void postfix()
    {
        rule = rule("expr");

        success("foo.this");
        success("foo.super");
        success("foo.m()");
        success("foo.<T, U>m(a, b)");
        success("foo.bar");
        success("foo.new Bar()");
        success("foo.new <U> Bar(a, b)");
        success("foo++");
        success("foo--");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void prefix()
    {
        rule = rule("expr");

        success("++1");
        success("--1");
        success("+1");
        success("-1");
        success("~1");
        success("!true");
        success("(String) obj");
        success("++ x.y");
        success("-- new Integer(1)");
        success("!x.y(1)");
        success("(String & Serializable & Cloneable) obj.x");
        success("(Function<X, Y>) x -> x[1].lol");
    }

    // ---------------------------------------------------------------------------------------------

    @Test void leftAssocBinary()
    {
        rule = rule("expr");

        success("1 * 1");
        success("1/1");
        success("2%3.");
        success("x+1");
        success("1-x");
        success("1 << 3");
        success("16 >> 3");
        success("16 >>> 3");
        success("1 < 3");
        success("1 <= 3");
        success("1 > 3");
        success("1 >= 3");
        success("1 == 1");
        success("1 != 1");
        success("8 & 1");
        success("8 ^ 1");
        success("8 | 1");
        success("true && false");
        success("true || false");

        success("1 * 1");
        success("1/1");
        success("2%3.");
        success("x+1");
        success("1-x");
        success("1 << 3");
        success("16 >> 3");
        success("16 >>> 3");
        success("1 < 3");
        success("1 <= 3");
        success("1 > 3");
        success("1 >= 3");
        success("1 == 1");
        success("1 != 1");
        success("8 & 1");
        success("8 ^ 1");
        success("8 | 1");
        success("true && false");
        success("true || false");
        success("x instanceof String");

        success("1 * 2 + 3 << 4");
        success("1 << 2 + 3 * 4");
        success("1 << 12 << 2 + 23 + 3 * 34 * 4");
        success("1 || 2 && 3 | 4 ^ 5 & 6 == 7 > 8 >> 9 + 10 * 11");
        success("x()[3] instanceof java.lang.String");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void ternary()
    {
        rule = rule("expr");

        success("true ? 1 : 2");
        success("1 * 2 == 2 || z[1] == 3 ? x[1] = 4 : true || false");
        success("true ? true ? 1 : 2 : true ? 3 : 4");
        success("true ? () -> 1 : (x, y) -> x[1].lol");
    }

    // ---------------------------------------------------------------------------------------------

     @Test public void assignment()
    {
        rule = rule("expr");

        success("x = 3");
        success("x += 3");
        success("x -= 4");
        success("x *= 3");
        success("x /= 3");
        success("x %= 3");
        success("x <<= 3");
        success("x >>= 3");
        success("x >>>= 3");
        success("x[1] = 3");
        success("x.y = 3");
        success("x[1].y = 3");
        success("x = true ? 2 : 3");
        success("x = y *= 3");
        success("x = z -> 3");
        success("x = (x, y) -> expr[x].lol");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void lambda() // no body
    {
        rule = rule("expr");

        success("x -> {}");
        success("x -> expr[1].lol");
        success("() -> {}");
        success("() -> expr[1].lol");
        success("(x) -> {}");
        success("(x) -> expr[1].lol");
        success("(x, y) -> {}");
        success("(x, y) -> expr[1].lol");
        success("(String x) -> {}");
        success("(String x) -> expr[1].lol");
        success("(String x, int y) -> {}");
        success("(String x, int y) -> expr[1].lol");
        success("(String... x) -> lol");
        success("(int x, int @Annot ... ys) -> lol");
    }

    // ---------------------------------------------------------------------------------------------

    @Test void typeDeclsNoBody()
    {
        rule = rule("type_decl");

        success("interface Hello {}");
        success("@interface Hello {}");
        success("class Hello {}");
        success("enum Hello {}");
        success("@Annot private interface Hello<T> {}");
        success("interface Hello<@Annot T extends String & Cloneable> {}");
        success("interface Hello<T> extends A, B, C {}");
        success("class Hello<T> extends World implements A, B, C {}");
        success("@Annot public enum X {;}");
    }

    // ---------------------------------------------------------------------------------------------

    @Test void classBodyDecl()
    {
        rule = rule("class_body_decl");

        success("int x;");
        success("int x = 1;");
        success("int x = 1, y;");
        success("@Annot final List<String> name = x, stuff[] = array();");
        success("@Annot <T extends Stuff & Thing, X extends List<?>> String meth (int x, int y)[] ;");
        success("void meth() throws Exception {}");
        success("@Annot <T extends Stuff> MyClass (int x) throws Error {}");
        success("static { someFunc(); myVar = x; }");
        success("{ myVar = x; }");
    }

    // ---------------------------------------------------------------------------------------------

    @Test void statements()
    {
        rule = rule("stmt");

        success("int x;");
        success("int x = 1;");
        success("int x = 1, y;");
        success("@Annot final List<String> name = x, stuff[] = array();");
        success("if (true) ; else ;");
        success("for (;;) ;");
        success("for (int i = 0 ; true ; ++i, ++j) ;");
        success("for (i = 0, j = 0 ; i < 10 ; ++i) ;");
        success("for (@Annot String x : list) ;");
        success("while (true) ;");
        success("do ; while (true);");
        success("try {} catch (Exception e) {} finally {}");
        success("try {} finally {}");
        success("try {} catch (Exception e) {}");
        success("try {} catch (@Annot Exception|Error e) {}");
        success("try (@Annot Resource res[] = myres) {} finally {}");
        success("try (Resource res[] = myres ; Resource x = youpie()) {} finally {}");
        success("switch (x) { case 1: dox(); doy(); case 2: case 3: doz(); break; default: dou();}");
        success("switch (x) {}");
        success("synchronized (expr) {}");
        success("return;");
        success("return x;");
        success("throw new Exception(lol);");
        success("assert x || y && z : lol;");
        success("assert x || y;");
        success("continue;");
        success("continue label;");
        success("break;");
        success("break label;");
        success("label: funCall();");
        success("label: while (x) {}");
        success(";");
        success("funcall();");
        success("++i;");
    }

    // ---------------------------------------------------------------------------------------------

    @Test void typeDeclsWithBodies()
    {
        rule = rule("type_decl");

        success("class C { @Annot final int var = 0; }");
        success("class C { @Annot private @Annut void method(String x) { return x; }}");
        success("interface I { void meth(int x); }");
        success("class C { class D {} }");
        success("class C { int x; static class D {}; void test(int x) {} }");
        success("@interface AI { String value(); }");
        success("@interface AI { Annot value() default @Annot; }");
        success("@interface AI { @Annot String value()[] default \"hello\" ; }");
        success("@interface AI { @interface AI2 {} }");
        success("enum E { ; }");
        success("enum E { X, Y, Z }");
        success("enum E { X, Y, Z; }");
        success("enum E { @Annot X (x) { void test(); }, Y } ");
        success("enum E { X(), Y; void test(int x); int y; }");
    }

    // ---------------------------------------------------------------------------------------------

    @Test void constructorCallsWithBodies()
    {
        rule = rule("expr");

        success("new C() { @Annot final int var = 0; }");
        success("new C() { @Annot private @Annut void method(String x) { return x; }}");
        success("new C<D>(1) { int var = 0; }");
    }

    // ---------------------------------------------------------------------------------------------
}