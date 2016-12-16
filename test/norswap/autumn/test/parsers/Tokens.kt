package norswap.autumn.test.parsers
import norswap.autumn.TokenGrammar
import norswap.autumn.UnexpectedToken
import norswap.autumn.parsers.*
import norswap.autumn.test.*
import org.testng.Assert.*
import org.testng.annotations.*

class Tokens: GrammarFixture()
{
    // ---------------------------------------------------------------------------------------------

    class TokenTestGrammar: TokenGrammar()
    {
        val foo     = token { string("foo" ) }
        val bar     = token ({ null }) { string("bar" ) }
        val foobar  = token { string("foobar" ) }
        val keyword = "keyword".keyword

        fun foo_bar() = seq { foo() && bar() }
        fun foo_foo() = seq { foo() && foo() }
        fun foo_x()   = choice { foo_bar() || foo_foo() }

        override fun root() = true
    }

    // ---------------------------------------------------------------------------------------------

    override val g = TokenTestGrammar()

    // ---------------------------------------------------------------------------------------------

    @Test fun test()
    {
        top_fun { g.foo() }
        success_expect("foo", "foo")
        success_expect("foo  ", "foo")
        failure_at(" foo", 0, UnexpectedToken)
        failure_at("bar", 0, UnexpectedToken)

        top_fun { g.bar() }
        success("bar")
        assertTrue(g.stack.isEmpty())
        success("bar  ")
        assertTrue(g.stack.isEmpty())

        top_fun { g.foobar() }
        success_expect("foobar", "foobar")
        success_expect("foobar  ", "foobar")

        top_fun { g.keyword() }
        success("keyword")
        assertTrue(g.stack.isEmpty())

        top_fun { g.foo_x() }
        success_expect("foo foo", "foo")
        assertEquals(g.stack.size, 2)
    }

    // ---------------------------------------------------------------------------------------------
}