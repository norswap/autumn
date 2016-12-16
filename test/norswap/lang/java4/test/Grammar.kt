package norswap.lang.java4.test
import norswap.autumn.UnexpectedToken
import norswap.autumn.test.*
import norswap.lang.java4.*
import norswap.lang.java4.ast.*
import org.testng.annotations.*
import kotlin.collections.listOf as l

class Grammar: GrammarFixture()
{
    // ---------------------------------------------------------------------------------------------

    override val g = Java4Grammar()

    // ---------------------------------------------------------------------------------------------

    val o = emptyList<Nothing>()
    val T = ClassType(kotlin.collections.listOf("T"))

    fun prim   (name: String) = PrimitiveType(name)
    fun sclass (name: String) = ClassType(kotlin.collections.listOf(name))

    // ---------------------------------------------------------------------------------------------

    @Test fun literals()
    {
        top_fun { g.literal() }
        success_expect("0", Literal(0))
        success_expect("0L", Literal(0L))
        success_expect("42",  Literal(42))
        success_expect("42l", Literal(42L))
        success_expect("42.", Literal(42.0))
        success_expect(".42", Literal(0.42))
        success_expect("42f", Literal(42f))
        success_expect("42d", Literal(42.0))
        success_expect(".42f", Literal(0.42f))
        success_expect("42e42", Literal(42e42))
        success_expect("42e+42", Literal(42e42))
        success_expect(".42e42", Literal(0.42e42))
        success_expect(".42e38f", Literal(0.42e38f))
        success_expect("42.42e+24f", Literal(42.42e24f))
        success_expect("42.e-42F", Literal(42e-42f))
        success_expect("0111", Literal(73))
        success_expect("0111L", Literal(73L))
        success_expect("0x8", Literal(8))
        success_expect("0x18L", Literal(24L))
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

        success_expect("0f", Literal(0f))
        success_expect("0d", Literal(0.0))
        success_expect("0e999999999f", Literal(0f))
        success_expect("0e999999999", Literal(0.0))

        success_issue(".42e-48f", Literal(0.0f),
                "Float literal is too small: rounded to 0.")
        success_issue("42.42e+42f", Literal(Float.POSITIVE_INFINITY),
                "Float literal is too big: rounded to infinity.")
        success_issue("0.1e-999", Literal(0.0),
                "Double literal is too small: rounded to 0.")
        success_issue("42e999", Literal(Double.POSITIVE_INFINITY),
                "Double literal is too big: rounded to infinity.")

        success_issue("9999999999", Literal(Int.MAX_VALUE),
                "Integer literal is too big: rounded to max value.")
        success_issue("9999999999999999999L", Literal(Long.MAX_VALUE),
                "Long literal is too big: rounded to max value.")
        }

    // ---------------------------------------------------------------------------------------------

    @Test fun types()
    {
        top_fun { g.type() }

        fun prim   (name: String) = PrimitiveType(name)
        fun sclass (name: String) = ClassType(kotlin.collections.listOf(name))

        val T = sclass("T")

        success_expect("char",      prim("char"))
        success_expect("int",       prim("int"))
        success_expect("double",    prim("double"))
        success_expect("void",      prim("void"))
        success_expect("String",    sclass("String"))

        success_expect("java.util.String",  ClassType(kotlin.collections.listOf("java", "util", "String")))
        success_expect("char[]",            ArrayType(prim("char"), 1))
        success_expect("int[][][]",         ArrayType(prim("int"), 3))
        success_expect("T[]",               ArrayType(T, 1))
        success("java.util.String[][]")
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun primary_expression()
    {
        top_fun { g.primary_expr() }

        success_expect("1", Literal(1))
        success_expect("iden", Identifier("iden"))
        success_expect("iden()", MethodCall(null, "iden", o))
        success_expect("iden(1, x)", MethodCall(null, "iden", kotlin.collections.listOf(Literal(1), Identifier("x"))))
        success_expect("(1)", ParenExpr(Literal(1)))
        success_expect("this", This)
        success_expect("super", Super)
        success_expect("this()", ThisCall(o))
        success_expect("super()", SuperCall(o))
        success_expect("this(1, x)", ThisCall(kotlin.collections.listOf(Literal(1), Identifier("x"))))
        success_expect("super(1, x)", SuperCall(kotlin.collections.listOf(Literal(1), Identifier("x"))))
        success_expect("new String()", CtorCall(sclass("String"), o, null))
        success_expect("void.class", ClassExpr(prim("void")))
        success_expect("int.class", ClassExpr(prim("int")))
        success_expect("List.class", ClassExpr(sclass("List")))
        success_expect("java.util.List.class", ClassExpr(ClassType(kotlin.collections.listOf("java", "util", "List"))))
        success_expect("new int[42]", ArrayCtorCall(prim("int"), kotlin.collections.listOf(Literal(42)), 0, null))
        success_expect("new int[42][]", ArrayCtorCall(prim("int"), kotlin.collections.listOf(Literal(42)), 1, null))
        success_expect("new int[1][2][][]",
            ArrayCtorCall(prim("int"), kotlin.collections.listOf(Literal(1), Literal(2)), 2, null))
        success_expect("new int[] { 1, 2, 3 }",
            ArrayCtorCall(prim("int"), o, 1, ArrayInit(kotlin.collections.listOf(Literal(1), Literal(2), Literal(3)))))
        success_expect("new int[] { 1, 2, }",
            ArrayCtorCall(prim("int"), o, 1, ArrayInit(kotlin.collections.listOf(Literal(1), Literal(2)))))
        success_expect("new int[] { , }",
            ArrayCtorCall(prim("int"), o, 1, ArrayInit(o)))
        success_expect("new int[][] { {1, 2}, {3, 4} }",
            ArrayCtorCall(prim("int"), o, 2, ArrayInit(kotlin.collections.listOf(
                ArrayInit(kotlin.collections.listOf(Literal(1), Literal(2))),
                ArrayInit(kotlin.collections.listOf(Literal(3), Literal(4)))))))
    }

    // ---------------------------------------------------------------------------------------------
}
