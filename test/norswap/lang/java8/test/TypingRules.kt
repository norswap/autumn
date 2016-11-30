package norswap.lang.java8.test
import norswap.lang.java8.Java8Grammar
import norswap.lang.java8.typing.*
import norswap.whimsy.Reactor
import norswap.whimsy.test.GrammarReactorFixture
import org.testng.annotations.Test

class TypingRules: GrammarReactorFixture()
{
    // ---------------------------------------------------------------------------------------------

    override val g = Java8Grammar()

    // ---------------------------------------------------------------------------------------------

    override fun Reactor.init()
        = install_java8_typing_rules()

    // ---------------------------------------------------------------------------------------------

    @Test fun literal()
    {
        top { g.literal() }
        attr("1", "type", TInt)
        attr("1L", "type", TLong)
        attr("1f", "type", TFloat)
        attr("1.0", "type", TDouble)
        attr("'a'", "type", TChar)
        attr("\"a\"", "type", TString)
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun not()
    {

    }

    // ---------------------------------------------------------------------------------------------
}