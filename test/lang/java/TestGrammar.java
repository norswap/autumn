package lang.java;

import norswap.autumn.Parser;
import norswap.lang.java.Grammar;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public final class TestGrammar
{
    // ---------------------------------------------------------------------------------------------

    private Grammar grammar = new Grammar();
    private Parser parser;

    // ---------------------------------------------------------------------------------------------

    @Test public void literals()
    {
        parser = grammar.literal.get();

        /*

        success_expect("4_2L", Literal(42L))
        success_expect(".42e42", Literal(0.42e42))
        success_expect("0x8", Literal(8))
        success_expect("0x8p8", Literal((8 * 2 shl 7).toDouble()))
        success_expect("0111", Literal(73))
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
         */
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void annotations()
    {
        parser = grammar.annotation.get();

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
