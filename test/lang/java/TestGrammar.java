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
        return Arrays.asList(NArrays.map(strings, new Identifier[0], Identifier::make));
    }

    // ---------------------------------------------------------------------------------------------

    private static List<TAnnotation> no_annotations = Collections.emptyList();
    private static List<TType>       no_type_args   = Collections.emptyList();

    private static TAnnotation marker = MarkerAnnotation.make(list(Identifier.make("Marker")));

    private static Dimension dim = Dimension.make(no_annotations);

    private static PrimitiveType prim (BasicType type) {
        return PrimitiveType.make(no_annotations, type);
    }

    private static ClassTypePart cpart (String name) {
        return ClassTypePart.make(no_annotations, Identifier.make(name), no_type_args);
    }

    private static ClassType sclass (String name, List<TType> type_args) {
        return ClassType.make(list(
            ClassTypePart.make(no_annotations, Identifier.make(name), type_args)));
    }

    private static ClassType T = ClassType.make(list(cpart("T")));

    // ---------------------------------------------------------------------------------------------

    @SuppressWarnings("OctalInteger")
    @Test public void literals()
    {
        parser = grammar.literal.get();

        success_expect("4_2L",          Literal.make(4_2L));
        success_expect(".42e42",        Literal.make(.42e42));
        success_expect("0x8",           Literal.make(0x8));
        success_expect("0x8p8",         Literal.make(0x8p8));
        success_expect("0111",          Literal.make(0111));
        success_expect("true",          Literal.make(true));
        success_expect("false",         Literal.make(false));
        success_expect("null",          Literal.make(Null.NULL));
        success_expect("\"\\u07FF\"",   Literal.make("\u07FF"));
        success_expect("'a'",           Literal.make('a'));
        success_expect("\"\\177\"",     Literal.make("\u007F"));
        success_expect("'\\177'",       Literal.make('\u007F'));
        success_expect("'\\u07FF'",     Literal.make('\u07FF'));

        failure("#");
        failure("identifier");
        failure("_42");
        failure("42_");

        success_expect(".42e-48f",
            Literal.make(new LexProblem("Float literal is too small.")));
        success_expect("42.42e+42f",
            Literal.make(new LexProblem("Float literal is too big.")));
        success_expect("0.1e-999",
            Literal.make(new LexProblem("Double literal is too small.")));
        success_expect("42e999",
            Literal.make(new LexProblem("Double literal is too big.")));

        success_expect("0x42p-999f",
            Literal.make(new LexProblem("Float literal is too small.")));
        success_expect("0x42p999f",
            Literal.make(new LexProblem("Float literal is too big.")));
        success_expect("0x42p-9999",
            Literal.make(new LexProblem("Double literal is too small.")));
        success_expect("0x42p9999",
            Literal.make(new LexProblem("Double literal is too big.")));

        success_expect("9999999999",
            Literal.make(new LexProblem("Integer literal is too big.")));
        success_expect("9999999999999999999L",
            Literal.make(new LexProblem("Long literal is too big.")));
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void annotations()
    {
        parser = grammar.annotation.get();

        // TODO hairy
        // candidate: "true ? x.y : x.y()[1]"
        String hairy = "42";
        Literal hval = Literal.make(42);

        success_expect("@Marker",
            marker);
        success_expect("@Marker()",
            marker);
        success_expect("@java.util.Marker()",
            MarkerAnnotation.strings("java", "util", "Marker"));
        success_expect("@Single(" + hairy + ")",
            SingleElementAnnotation.make(id_list("Single"), hval));
        success_expect("@Single(@Marker)",
            SingleElementAnnotation.make(id_list("Single"), marker));

        success_expect("@java.util.Single(@java.util.Marker)",
            SingleElementAnnotation.make(
                id_list("java", "util", "Single"),
                MarkerAnnotation.strings("java", "util", "Marker")));

        success_expect("@Single({@Marker, " + hairy + "})",
            SingleElementAnnotation.make(
                id_list("Single"),
                AnnotationElementList.make(list(marker, hval))));

        success_expect("@Single({})",
            SingleElementAnnotation.make(id_list("Single"), AnnotationElementList.make(list())));

        success_expect("@Single({,})",
            SingleElementAnnotation.make(id_list("Single"), AnnotationElementList.make(list())));

        success_expect("@Single({x,})",
            SingleElementAnnotation.make(
                id_list("Single"),
                AnnotationElementList.make(list(Identifier.make("x")))));

        success_expect("@Single(x)",
            SingleElementAnnotation.make(id_list("Single"), Identifier.make("x")));

        success_expect("@Pairs(x = @Marker)",
            NormalAnnotation.make(
                list(Identifier.make("Pairs")),
                list(new Pair<>(Identifier.make("x"), marker))));

        success_expect("@Pairs(x = @Marker, y = " + hairy + ", z = {@Marker, " + hairy + "}, u = x)",
            NormalAnnotation.make(list(Identifier.make("Pairs")), list(
                new Pair<>(Identifier.make("x"), marker),
                new Pair<>(Identifier.make("y"), hval),
                new Pair<>(Identifier.make("z"), AnnotationElementList.make(list(marker, hval))),
                new Pair<>(Identifier.make("u"), Identifier.make("x")))));
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void types()
    {
        parser = grammar.type.get();

        success_expect("char",      PrimitiveType.make(list(), _char));
        success_expect("int",       PrimitiveType.make(list(), _int));
        success_expect("double",    PrimitiveType.make(list(), _double));
        success_expect("void",      PrimitiveType.make(list(), _void));

        success_expect("java.util.String",
            ClassType.make(list(cpart("java"), cpart("util"), cpart("String"))));
        success_expect("List<?>",
            sclass("List", list(Wildcard.make(no_annotations, null))));
        success_expect("List<T>",
            sclass("List", list(T)));
        success_expect("List<? super T>",
            sclass("List", list(Wildcard.make(no_annotations, SuperBound.make(T)))));
        success_expect("List<? extends T>",
            sclass("List", list(Wildcard.make(no_annotations, ExtendsBound.make(T)))));

        success("java.util.List<?>");
        success("java.util.List<T>");
        success("java.util.List<? super T>");

        success_expect("char[]",
            ArrayType.make(prim(_char), list(dim)));
        success_expect("int[][][]",
            ArrayType.make(prim(_int), list(dim, dim, dim)));
        success_expect("T[]",
            ArrayType.make(T, list(dim)));
        success_expect("List<T>[][]",
            ArrayType.make(sclass("List", list(T)), list(dim, dim)));

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
            sclass("List", list(Wildcard.make(no_annotations, ExtendsBound.make(sclass("List",
                list(Wildcard.make(no_annotations, SuperBound.make(T)))))))));

        success_expect("@Marker int",
            PrimitiveType.make(list(marker), _int));
        success_expect("@Marker java.@test.Mbrker util . @Mcrker String",
            ClassType.make(list(
                ClassTypePart.make(list(marker), Identifier.make("java"), no_type_args),
                ClassTypePart.make(
                    list(MarkerAnnotation.make(list(
                        Identifier.make("test"),
                        Identifier.make("Mbrker")))),
                    Identifier.make("util"),
                    no_type_args),
                ClassTypePart.make(
                    list(MarkerAnnotation.make(list(Identifier.make("Mcrker")))),
                    Identifier.make("String"),
                    no_type_args))));
        success_expect("List<@Marker ?>",
            sclass("List", list(Wildcard.make(list(marker), null))));
        success_expect("List<? extends @Marker T>",
            sclass("List", list(Wildcard.make(no_annotations, ExtendsBound.make(
                ClassType.make(list(ClassTypePart.make(list(marker), Identifier.make("T"), no_type_args))))))));
        success("List<@Marker ? extends @Marker T>");
        success_expect("@Marker int @Mbrker []",
            ArrayType.make(
                PrimitiveType.make(list(marker), _int),
                list(Dimension.make(list(MarkerAnnotation.make(list(Identifier.make("Mbrker"))))))));
    }

    // ---------------------------------------------------------------------------------------------
}