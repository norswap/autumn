package lang.java;

import norswap.autumn.TestFixture;
import norswap.lang.java.Grammar;
import norswap.lang.java.LexUtils.LexProblem;
import norswap.lang.java.ast.*;
import norswap.utils.NArrays;
import norswap.utils.Pair;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static norswap.lang.java.ast.BasicType.*;
import static norswap.utils.Vanilla.list;

public final class TestGrammar extends TestFixture
{
    // ---------------------------------------------------------------------------------------------

    private Grammar grammar = new Grammar();

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

    // a class name with a single identifier an the given type arguments
    private static ClassType sclass (String name, List<TType> type_args) {
        return ClassType.mk(list(
            ClassTypePart.mk(no_annotations, Identifier.mk(name), type_args)));
    }

    // class name "T"
    private static ClassType T = ClassType.mk(list(cpart("T")));

    // ---------------------------------------------------------------------------------------------

    @SuppressWarnings("OctalInteger")
    @Test public void literals()
    {
        parser = grammar.literal.get();

        success_expect("4_2L",          Literal.mk(4_2L));
        success_expect(".42e42",        Literal.mk(.42e42));
        success_expect("0x8",           Literal.mk(0x8));
        success_expect("0x8p8",         Literal.mk(0x8p8));
        success_expect("0111",          Literal.mk(0111));
        success_expect("true",          Literal.mk(true));
        success_expect("false",         Literal.mk(false));
        success_expect("null",          Literal.mk(Null.NULL));
        success_expect("\"\\u07FF\"",   Literal.mk("\u07FF"));
        success_expect("'a'",           Literal.mk('a'));
        success_expect("\"\\177\"",     Literal.mk("\u007F"));
        success_expect("'\\177'",       Literal.mk('\u007F'));
        success_expect("'\\u07FF'",     Literal.mk('\u07FF'));

        failure("#");
        failure("identifier");
        failure("_42");
        failure("42_");

        success_expect(".42e-48f",
            Literal.mk(new LexProblem("Float literal is too small.")));
        success_expect("42.42e+42f",
            Literal.mk(new LexProblem("Float literal is too big.")));
        success_expect("0.1e-999",
            Literal.mk(new LexProblem("Double literal is too small.")));
        success_expect("42e999",
            Literal.mk(new LexProblem("Double literal is too big.")));

        success_expect("0x42p-999f",
            Literal.mk(new LexProblem("Float literal is too small.")));
        success_expect("0x42p999f",
            Literal.mk(new LexProblem("Float literal is too big.")));
        success_expect("0x42p-9999",
            Literal.mk(new LexProblem("Double literal is too small.")));
        success_expect("0x42p9999",
            Literal.mk(new LexProblem("Double literal is too big.")));

        success_expect("9999999999",
            Literal.mk(new LexProblem("Integer literal is too big.")));
        success_expect("9999999999999999999L",
            Literal.mk(new LexProblem("Long literal is too big.")));
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void annotations()
    {
        parser = grammar.annotation.get();

        // TODO hairy
        // candidate: "true ? x.y : x.y()[1]"
        String hairy = "42";
        Literal hval = Literal.mk(42);

        success_expect("@Marker",
            marker);
        success_expect("@Marker()",
            marker);
        success_expect("@java.util.Marker()",
            MarkerAnnotation.strings("java", "util", "Marker"));
        success_expect("@Single(" + hairy + ")",
            SingleElementAnnotation.mk(id_list("Single"), hval));
        success_expect("@Single(@Marker)",
            SingleElementAnnotation.mk(id_list("Single"), marker));

        success_expect("@java.util.Single(@java.util.Marker)",
            SingleElementAnnotation.mk(
                id_list("java", "util", "Single"),
                MarkerAnnotation.strings("java", "util", "Marker")));

        success_expect("@Single({@Marker, " + hairy + "})",
            SingleElementAnnotation.mk(
                id_list("Single"),
                AnnotationElementList.mk(list(marker, hval))));

        success_expect("@Single({})",
            SingleElementAnnotation.mk(id_list("Single"), AnnotationElementList.mk(list())));

        success_expect("@Single({,})",
            SingleElementAnnotation.mk(id_list("Single"), AnnotationElementList.mk(list())));

        success_expect("@Single({x,})",
            SingleElementAnnotation.mk(
                id_list("Single"),
                AnnotationElementList.mk(list(Identifier.mk("x")))));

        success_expect("@Single(x)",
            SingleElementAnnotation.mk(id_list("Single"), Identifier.mk("x")));

        success_expect("@Pairs(x = @Marker)",
            NormalAnnotation.mk(
                list(Identifier.mk("Pairs")),
                list(new Pair<>(Identifier.mk("x"), marker))));

        success_expect("@Pairs(x = @Marker, y = " + hairy + ", z = {@Marker, " + hairy + "}, u = x)",
            NormalAnnotation.mk(list(Identifier.mk("Pairs")), list(
                new Pair<>(Identifier.mk("x"), marker),
                new Pair<>(Identifier.mk("y"), hval),
                new Pair<>(Identifier.mk("z"), AnnotationElementList.mk(list(marker, hval))),
                new Pair<>(Identifier.mk("u"), Identifier.mk("x")))));
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void types()
    {
        parser = grammar.type.get();

        success_expect("char",      PrimitiveType.mk(list(), _char));
        success_expect("int",       PrimitiveType.mk(list(), _int));
        success_expect("double",    PrimitiveType.mk(list(), _double));
        success_expect("void",      PrimitiveType.mk(list(), _void));

        success_expect("java.util.String",
            ClassType.mk(list(cpart("java"), cpart("util"), cpart("String"))));
        success_expect("List<?>",
            sclass("List", list(Wildcard.mk(no_annotations, null))));
        success_expect("List<T>",
            sclass("List", list(T)));
        success_expect("List<? super T>",
            sclass("List", list(Wildcard.mk(no_annotations, SuperBound.mk(T)))));
        success_expect("List<? extends T>",
            sclass("List", list(Wildcard.mk(no_annotations, ExtendsBound.mk(T)))));

        success("java.util.List<?>");
        success("java.util.List<T>");
        success("java.util.List<? super T>");

        success_expect("char[]",
            ArrayType.mk(prim(_char), list(dim)));
        success_expect("int[][][]",
            ArrayType.mk(prim(_int), list(dim, dim, dim)));
        success_expect("T[]",
            ArrayType.mk(T, list(dim)));
        success_expect("List<T>[][]",
            ArrayType.mk(sclass("List", list(T)), list(dim, dim)));

        success("java.util.String[][]");
        success("List<?>[]");
        success("List<? super T>[]");
        success("List<? extends T>[][]");
        success("java.util.List<?>[]");
        success("java.util.List<T>[][]");
        success("java.util.List<? super T>[][]");

        success_expect("List<List<T>>",
            sclass("List", list(sclass("List", list(T)))));

        success_expect("List<? extends List<? super T>>",
            sclass("List", list(Wildcard.mk(no_annotations, ExtendsBound.mk(sclass("List",
                list(Wildcard.mk(no_annotations, SuperBound.mk(T)))))))));

        success_expect("@Marker int",
            PrimitiveType.mk(list(marker), _int));

        success_expect("@Marker java.@test.Mbrker util . @Mcrker String",
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

        success_expect("List<@Marker ?>",
            sclass("List", list(Wildcard.mk(list(marker), null))));

        success_expect("List<? extends @Marker T>",
            sclass("List", list(Wildcard.mk(no_annotations, ExtendsBound.mk(ClassType.mk(list(
                    ClassTypePart.mk(list(marker), Identifier.mk("T"), no_type_args))))))));

        success("List<@Marker ? extends @Marker T>");

        success_expect("@Marker int @Mbrker []",
            ArrayType.mk(
                PrimitiveType.mk(list(marker), _int),
                list(Dimension.mk(list(MarkerAnnotation.mk(list(Identifier.mk("Mbrker"))))))));
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void primary_expressions()
    {
        parser = grammar.expr.get(); // TODO expr

        success_expect("1",
            Literal.mk(1));
        success_expect("iden",
            Identifier.mk("iden"));
        success_expect("iden()",
            MethodCall.mk(null, no_type_args, Identifier.mk("iden"), no_args));
        success_expect("iden(1, x)",
            MethodCall.mk(null, no_type_args, Identifier.mk("iden"),
                list(Literal.mk(1), Identifier.mk("x"))));
        success_expect("(1)",
            ParenExpression.mk(Literal.mk(1)));
        success_expect("this",
            This.mk());
        success_expect("super",
            Super.mk());
        success_expect("this()",
            ThisCall.mk(no_args));
        success_expect("super()",
            SuperCall.mk(no_args));
        success_expect("this(1, x)",
            ThisCall.mk(list(Literal.mk(1), Identifier.mk("x"))));
        success_expect("super(1, x)",
            SuperCall.mk(list(Literal.mk(1), Identifier.mk("x"))));

        success_expect("new String()",
            ConstructorCall.mk(no_type_args, sclass("String", no_type_args), no_args, null));
        success_expect("new <T> Test()",
            ConstructorCall.mk(list(T), sclass("Test", no_type_args), no_args, null));
        success_expect("new Test<T>()",
            ConstructorCall.mk(no_type_args, sclass("Test", list(T)), no_args, null));
        success_expect("void.class",
            ClassExpression.mk(prim(_void)));
        success_expect("int.class",
            ClassExpression.mk(prim(_int)));
        success_expect("List.class",
            ClassExpression.mk(sclass("List", no_type_args)));
        success_expect("java.util.List.class",
            ClassExpression.mk(ClassType.mk(list(cpart("java"), cpart("util"), cpart("List")))));

        success_expect("new int[42]",
            ArrayConstructorCall.mk(prim(_int), list(DimExpression.mk(no_annotations, Literal.mk(42))), no_dims, null));
        success_expect("new int[42][]",
            ArrayConstructorCall.mk(prim(_int), list(DimExpression.mk(no_annotations, Literal.mk(42))), list(dim), null));
        success_expect("new int[1][2][][]",
            ArrayConstructorCall.mk(prim(_int),
                list(DimExpression.mk(no_annotations, Literal.mk(1)), DimExpression.mk(no_annotations, Literal.mk(2))),
                list(dim, dim), null));
        success_expect("new int[] { 1, 2, 3 }",
            ArrayConstructorCall.mk(prim(_int), no_dim_exprs, list(dim), ArrayInitializer.mk(list(Literal.mk(1), Literal.mk(2), Literal.mk(3)))));
        success_expect("new int[] { 1, 2, }",
            ArrayConstructorCall.mk(prim(_int), no_dim_exprs, list(dim), ArrayInitializer.mk(list(Literal.mk(1), Literal.mk(2)))));
        success_expect("new int[] { , }",
            ArrayConstructorCall.mk(prim(_int), no_dim_exprs, list(dim), ArrayInitializer.mk(no_args)));
        success_expect("new int[][] { {1, 2}, {3, 4} }",
            ArrayConstructorCall.mk(prim(_int), no_dim_exprs, list(dim, dim), ArrayInitializer.mk(list(
                ArrayInitializer.mk(list(Literal.mk(1), Literal.mk(2))),
                ArrayInitializer.mk(list(Literal.mk(3), Literal.mk(4)))))));
        success_expect("new List<T>[1]",
            ArrayConstructorCall.mk(
                sclass("List", list(T)),
                list(DimExpression.mk(no_annotations, Literal.mk(1))),
                no_dims, null));

//        success_expect("Foo::bar", MaybeBoundMethodReference(sclass("Foo"), o, "bar"))
//        success_expect("Foo::new", NewReference(sclass("Foo"), o))
//        success_expect("Foo::<T>bar", MaybeBoundMethodReference(sclass("Foo"), list(T), "bar"))
//        success_expect("Foo::<T, V>new", NewReference(sclass("Foo"), list(T, sclass("V"))))
//        success_expect("List.Foo::<T>bar",
//            MaybeBoundMethodReference(ClassType(l(cpart("List"), cpart("Foo"))), list(T), "bar"))
    }

    // ---------------------------------------------------------------------------------------------
}