package lang.java;

import norswap.autumn.TestFixture;
import norswap.lang.java.Grammar;
import norswap.lang.java.LexUtils.LexProblem;
import norswap.lang.java.ast.*;
import norswap.utils.Pair;
import org.testng.annotations.Test;

import static norswap.utils.Vanilla.list;

public final class TestGrammar extends TestFixture
{
    // ---------------------------------------------------------------------------------------------

    private Grammar grammar = new Grammar();

    // ---------------------------------------------------------------------------------------------

    @SuppressWarnings("OctalInteger")
    @Test public void literals()
    {
        parser = grammar.literal.get();

        success_expect("4_2L",          new Literal(4_2L));
        success_expect(".42e42",        new Literal(.42e42));
        success_expect("0x8",           new Literal(0x8));
        success_expect("0x8p8",         new Literal(0x8p8));
        success_expect("0111",          new Literal(0111));
        success_expect("true",          new Literal(true));
        success_expect("false",         new Literal(false));
        success_expect("null",          new Literal(Null.NULL));
        success_expect("\"\\u07FF\"",   new Literal("\u07FF"));
        success_expect("'a'",           new Literal('a'));
        success_expect("\"\\177\"",     new Literal("\u007F"));
        success_expect("'\\177'",       new Literal('\u007F'));
        success_expect("'\\u07FF'",     new Literal('\u07FF'));

        failure("#");
        failure("identifier");
        failure("_42");
        failure("42_");

        success_expect(".42e-48f",
            new Literal(new LexProblem("Float literal is too small.")));
        success_expect("42.42e+42f",
            new Literal(new LexProblem("Float literal is too big.")));
        success_expect("0.1e-999",
            new Literal(new LexProblem("Double literal is too small.")));
        success_expect("42e999",
            new Literal(new LexProblem("Double literal is too big.")));

        success_expect("0x42p-999f",
            new Literal(new LexProblem("Float literal is too small.")));
        success_expect("0x42p999f",
            new Literal(new LexProblem("Float literal is too big.")));
        success_expect("0x42p-9999",
            new Literal(new LexProblem("Double literal is too small.")));
        success_expect("0x42p9999",
            new Literal(new LexProblem("Double literal is too big.")));

        success_expect("9999999999",
            new Literal(new LexProblem("Integer literal is too big.")));
        success_expect("9999999999999999999L",
            new Literal(new LexProblem("Long literal is too big.")));
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void annotations()
    {
        parser = grammar.annotation.get();

        String hairy = "42"; // TODO ???
        Literal hval = new Literal(42);

        TAnnotation marker = new MarkerAnnotation(list("Marker"));

        // TODO
        // candidate: "true ? x.y : x.y()[1]"

        // TODO $hairy

        success_expect("@Marker",
            marker);
        success_expect("@Marker()",
            marker);
        success_expect("@java.util.Marker()",
            new MarkerAnnotation(list("java", "util", "Marker")));
        success_expect("@Single($hairy)",
            new SingleElementAnnotation(list("Single"), hval));
        success_expect("@Single(@Marker)",
            new SingleElementAnnotation(list("Single"), marker));
        success_expect("@java.util.Single(@java.util.Marker)",
            new SingleElementAnnotation(list("java", "util", "Single"), new MarkerAnnotation(list("java", "util", "Marker"))));
        success_expect("@Single({@Marker, $hairy})",
            new SingleElementAnnotation(list("Single"), new AnnotationElementList(list(marker, hval))));
        success_expect("@Single({})",
            new SingleElementAnnotation(list("Single"), new AnnotationElementList(list())));
        success_expect("@Single({,})",
            new SingleElementAnnotation(list("Single"), new AnnotationElementList(list())));
        success_expect("@Single({x,})",
            new SingleElementAnnotation(list("Single"), new AnnotationElementList(list(new Identifier("x")))));
        success_expect("@Single(x)",
            new SingleElementAnnotation(list("Single"), new Identifier("x")));
        success_expect("@Pairs(x = @Marker)",
            new NormalAnnotation(list("Pairs"), list(new Pair<>("x", marker))));
        success_expect("@Pairs(x = @Marker, y = $hairy, z = {@Marker, $hairy}, u = x)",
            new NormalAnnotation(list("Pairs"), list(
                new Pair<>("x", marker),
                new Pair<>("y", hval),
                new Pair<>("z", new AnnotationElementList(list(marker, hval))),
                new Pair<>("u", new Identifier("x")))));

        /*
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
            NormalAnnotation(l("Pairs"), l("x"), l(marker)))
        success_expect("@Pairs(x = @Marker, y = $hairy, z = {@Marker, $hairy}, u = x)",
            NormalAnnotation(l("Pairs"), l("x", "y", "z", "u"),
                l(marker, hyval, AnnotationElementList(l(marker, hyval)), Identifier("x"))))

        // TODO multipart annotation name
         */
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void types()
    {
        parser = grammar.type.get();

        /*
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
         */
    }

    // ---------------------------------------------------------------------------------------------
}