package norswap.lang.java8.test
import norswap.lang.java8.Java8Grammar
import norswap.lang.java8.typing.*
import norswap.whimsy.Reactor
import norswap.whimsy.ReactorError
import norswap.whimsy.test.GrammarReactorFixture
import org.testng.annotations.Test
import java.lang.Class

class TypingRules: GrammarReactorFixture()
{
    // ---------------------------------------------------------------------------------------------

    override val g = Java8Grammar()

    // ---------------------------------------------------------------------------------------------

    override fun Reactor.init()
        = install_java8_typing_rules()

    // ---------------------------------------------------------------------------------------------

    fun type_error (input: String, klass: Class<out ReactorError>)
    {
        root_error(input, "type", klass)
    }

    // ---------------------------------------------------------------------------------------------

    fun type (input: String, value: Type)
    {
        attr(input, "type", value)
    }

    // ---------------------------------------------------------------------------------------------

    val str = "\"a\""

    // ---------------------------------------------------------------------------------------------

    @Test fun literal()
    {
        top_fun { g.literal() }
        type("1",       TInt)
        type("1L",      TLong)
        type("1f",      TFloat)
        type("1.0",     TDouble)
        type("'a'",     TChar)
        type("\"a\"",   TString)
        type("true",    TBool)
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun not()
    {
        top_fun { g.not() }
        type("!true",   TBool)
        type("!!false", TBool)
        type_error("!1", NotTypeError::class.java)
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun complement()
    {
        top_fun { g.complement() }
        type("~1",  TInt)
        type("~1L", TLong)
        // todo short etc
        type_error("~1.0",  ComplementTypeError::class.java)
        type_error("~$str", ComplementTypeError::class.java)
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun unary_arith()
    {
        top_fun { g.prefix_expr() }
        type("+1",      TInt)
        type("-1",      TInt)
        type("+1L",     TLong)
        type("-1.0",    TDouble)
        // todo short etc
        type_error("+true", UnaryArithTypeError::class.java)
        type_error("+$str", UnaryArithTypeError::class.java)
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun binary_arith()
    {
        top_fun { g.add_expr() }
        type("1+1",     TInt)
        type("1L-1",    TLong)
        type("1.0*1",   TDouble)
        type("1f/1",    TFloat)
        type("1L%1",    TLong)
        type("$str+1",  TString)
        type("1+$str",  TString)
        // todo short etc
        type_error("1*true", BinaryArithTypeError::class.java)
        type_error("1-$str", BinaryArithTypeError::class.java)
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun shift()
    {
        top_fun { g.shift_expr() }
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun ordering()
    {
        top_fun { g.order_expr() }
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun instanceof()
    {
        top_fun { g.order_expr() }
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun equal()
    {
        top_fun { g.eq_expr() }
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun bitwise()
    {
        top_fun { g.binary_or_expr() }
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun logical()
    {
        top_fun { g.or_expr() }
    }

    // ---------------------------------------------------------------------------------------------
}