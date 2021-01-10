package lang.java;

import norswap.autumn.DSL;
import norswap.autumn.TestFixture;
import norswap.lang.java.Grammar;
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
public class TestGrammar extends TestFixture
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
        this(new Grammar());
    }

    // ---------------------------------------------------------------------------------------------

    private DSL.rule rule(String name) {
        return cast(suppress(() -> grammarClass.getField(name).get(grammar)));
    }

    // ---------------------------------------------------------------------------------------------

    private List<Identifier> id_list (String... strings)
    {
        return Arrays.asList(NArrays.map(strings, new Identifier[0], Identifier::mk));
    }

    // ---------------------------------------------------------------------------------------------

    private static List<TAnnotation>   no_annotations = Collections.emptyList();
    private static List<TType>         no_type_args   = Collections.emptyList();
    private static List<Expression>    no_args        = Collections.emptyList();
    private static List<DimExpression> no_dim_exprs   = Collections.emptyList();
    private static List<Dimension>     no_dims        = Collections.emptyList();

    // identifier called "marker"
    private static TAnnotation marker = MarkerAnnotation.mk(list(Identifier.mk("Marker")));

    // []
    private static Dimension dim = Dimension.mk(no_annotations);

    // primitive type (int, float, etc)
    private static PrimitiveType prim (BasicType type) {
        return PrimitiveType.mk(no_annotations, type);
    }

    // an identifier part of a class name
    private static ClassTypePart cpart (String name) {
        return ClassTypePart.mk(no_annotations, Identifier.mk(name), no_type_args);
    }

    // a class name with a single identifier and the given type arguments
    private static ClassType sclass (String name, List<TType> type_args) {
        return ClassType.mk(list(
            ClassTypePart.mk(no_annotations, Identifier.mk(name), type_args)));
    }

    // a class name with a single identifier and no type arguments
    private static ClassType sclass (String name) {
        return sclass(name, no_type_args);
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

        // NOTE(norswap): stopgap to account for the fact that Grammar and GrammarTokens are
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
                MethodCall.mk(Identifier.mk("x"), no_type_args, Identifier.mk("y"), no_args),
                Literal.mk(1)));

        successExpect("@Marker",
            marker);
        successExpect("@Marker()",
            marker);
        successExpect("@java.util.Marker()",
            MarkerAnnotation.strings("java", "util", "Marker"));
        successExpect("@Single(" + hairy + ")",
            SingleElementAnnotation.mk(id_list("Single"), hval));
        successExpect("@Single(@Marker)",
            SingleElementAnnotation.mk(id_list("Single"), marker));

        successExpect("@java.util.Single(@java.util.Marker)",
            SingleElementAnnotation.mk(
                id_list("java", "util", "Single"),
                MarkerAnnotation.strings("java", "util", "Marker")));

        successExpect("@Single({@Marker, " + hairy + "})",
            SingleElementAnnotation.mk(
                id_list("Single"),
                AnnotationElementList.mk(list(marker, hval))));

        successExpect("@Single({})",
            SingleElementAnnotation.mk(id_list("Single"), AnnotationElementList.mk(list())));

        successExpect("@Single({,})",
            SingleElementAnnotation.mk(id_list("Single"), AnnotationElementList.mk(list())));

        successExpect("@Single({x,})",
            SingleElementAnnotation.mk(
                id_list("Single"),
                AnnotationElementList.mk(list(Identifier.mk("x")))));

        successExpect("@Single(x)",
            SingleElementAnnotation.mk(id_list("Single"), Identifier.mk("x")));

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
            sclass("List", list(Wildcard.mk(no_annotations, null))));
        successExpect("List<T>",
            sclass("List", list(T)));
        successExpect("List<? super T>",
            sclass("List", list(Wildcard.mk(no_annotations, SuperBound.mk(T)))));
        successExpect("List<? extends T>",
            sclass("List", list(Wildcard.mk(no_annotations, ExtendsBound.mk(T)))));

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
            sclass("List", list(Wildcard.mk(no_annotations, ExtendsBound.mk(sclass("List",
                list(Wildcard.mk(no_annotations, SuperBound.mk(T)))))))));

        successExpect("@Marker int",
            PrimitiveType.mk(list(marker), _int));

        successExpect("@Marker java.@test.Mbrker util . @Mcrker String",
            ClassType.mk(list(
                ClassTypePart.mk(list(marker), Identifier.mk("java"), no_type_args),
                ClassTypePart.mk(
                    list(MarkerAnnotation.mk(list(
                        Identifier.mk("test"),
                        Identifier.mk("Mbrker")))),
                    Identifier.mk("util"),
                    no_type_args),
                ClassTypePart.mk(
                    list(MarkerAnnotation.mk(list(Identifier.mk("Mcrker")))),
                    Identifier.mk("String"),
                    no_type_args))));

        successExpect("List<@Marker ?>",
            sclass("List", list(Wildcard.mk(list(marker), null))));

        successExpect("List<? extends @Marker T>",
            sclass("List", list(Wildcard.mk(no_annotations, ExtendsBound.mk(ClassType.mk(list(
                    ClassTypePart.mk(list(marker), Identifier.mk("T"), no_type_args))))))));

        success("List<@Marker ? extends @Marker T>");

        successExpect("@Marker int @Mbrker []",
            ArrayType.mk(
                PrimitiveType.mk(list(marker), _int),
                list(Dimension.mk(list(MarkerAnnotation.mk(list(Identifier.mk("Mbrker"))))))));
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void primary_expressions()
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
            MethodCall.mk(null, no_type_args, Identifier.mk("iden"), no_args));
        successExpect("iden(1, x)",
            MethodCall.mk(null, no_type_args, Identifier.mk("iden"),
                list(Literal.mk(1), Identifier.mk("x"))));
        successExpect("(1)",
            ParenExpression.mk(Literal.mk(1)));
        successExpect("this",
            This.mk());
        successExpect("super",
            Super.mk());
        successExpect("this()",
            ThisCall.mk(no_args));
        successExpect("super()",
            SuperCall.mk(no_args));
        successExpect("this(1, x)",
            ThisCall.mk(list(Literal.mk(1), Identifier.mk("x"))));
        successExpect("super(1, x)",
            SuperCall.mk(list(Literal.mk(1), Identifier.mk("x"))));

        successExpect("new String()",
            ConstructorCall.mk(no_type_args, sclass("String", no_type_args), no_args, null));
        successExpect("new <T> Test()",
            ConstructorCall.mk(list(T), sclass("Test", no_type_args), no_args, null));
        successExpect("new Test<T>()",
            ConstructorCall.mk(no_type_args, sclass("Test", list(T)), no_args, null));
        successExpect("void.class",
            ClassExpression.mk(prim(_void)));
        successExpect("int.class",
            ClassExpression.mk(prim(_int)));
        successExpect("List.class",
            ClassExpression.mk(sclass("List", no_type_args)));
        successExpect("java.util.List.class",
            ClassExpression.mk(ClassType.mk(list(cpart("java"), cpart("util"), cpart("List")))));

        successExpect("new int[42]",
            ArrayConstructorCall.mk(prim(_int),
                list(DimExpression.mk(no_annotations, Literal.mk(42))),
                no_dims, null));
        successExpect("new int[42][]",
            ArrayConstructorCall.mk(prim(_int),
                list(DimExpression.mk(no_annotations, Literal.mk(42))),
                list(dim), null));
        successExpect("new int[1][2][][]",
            ArrayConstructorCall.mk(prim(_int),
                list(
                    DimExpression.mk(no_annotations, Literal.mk(1)),
                    DimExpression.mk(no_annotations, Literal.mk(2))),
                list(dim, dim), null));
        successExpect("new int[] { 1, 2, 3 }",
            ArrayConstructorCall.mk(prim(_int), no_dim_exprs, list(dim),
                ArrayInitializer.mk(list(Literal.mk(1), Literal.mk(2), Literal.mk(3)))));
        successExpect("new int[] { 1, 2, }",
            ArrayConstructorCall.mk(prim(_int), no_dim_exprs, list(dim),
                ArrayInitializer.mk(list(Literal.mk(1), Literal.mk(2)))));
        successExpect("new int[] { , }",
            ArrayConstructorCall.mk(prim(_int), no_dim_exprs, list(dim),
                ArrayInitializer.mk(no_args)));
        successExpect("new int[][] { {1, 2}, {3, 4} }",
            ArrayConstructorCall.mk(prim(_int), no_dim_exprs, list(dim, dim),
                ArrayInitializer.mk(list(
                    ArrayInitializer.mk(list(Literal.mk(1), Literal.mk(2))),
                    ArrayInitializer.mk(list(Literal.mk(3), Literal.mk(4)))))));
        successExpect("new List<T>[1]",
            ArrayConstructorCall.mk(
                sclass("List", list(T)),
                list(DimExpression.mk(no_annotations, Literal.mk(1))),
                no_dims, null));

        successExpect("Foo::bar",
            TypeMethodReference.mk(sclass("Foo"), no_type_args, Identifier.mk("bar")));
        successExpect("Foo::new",
            NewReference.mk(sclass("Foo"), no_type_args));
        successExpect("Foo::<T>bar",
            TypeMethodReference.mk(sclass("Foo"), list(T), Identifier.mk("bar")));
        successExpect("Foo::<T, V>new",
            NewReference.mk(sclass("Foo"), list(T, sclass("V"))));
        successExpect("List.Foo::<T>bar",
            TypeMethodReference.mk(
                ClassType.mk(list(cpart("List"), cpart("Foo"))), list(T), Identifier.mk("bar")));

        successExpect("newClass()",
            MethodCall.mk(null, no_type_args, Identifier.mk("newClass"), no_args));
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

    @Test void left_assoc_binary()
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

    @Test void type_decls_no_body()
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

    @Test void class_body_decl()
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

    @Test void type_decls_with_bodies()
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

    @Test void constructor_calls_with_bodies()
    {
        rule = rule("expr");

        success("new C() { @Annot final int var = 0; }");
        success("new C() { @Annot private @Annut void method(String x) { return x; }}");
        success("new C<D>(1) { int var = 0; }");
    }

    // ---------------------------------------------------------------------------------------------
}