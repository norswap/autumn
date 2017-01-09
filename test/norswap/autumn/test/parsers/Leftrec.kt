package norswap.autumn.test.parsers
import norswap.autumn.Grammar
import norswap.autumn.Parser
import norswap.autumn.parsers.*
import norswap.autumn.test.EmptyGrammarFixture
import org.testng.annotations.Test

class Leftrec: EmptyGrammarFixture()
{
    // ---------------------------------------------------------------------------------------------

    @Test fun left_recusive()
    {
        val recursive = g.leftrec { choice { seq { it() && string("a") } || string("a") } }
        top_fun { recursive() }
        success("a")
        success("aa")
        success("aaa")
        failure_expect("b", 0, "a")
        failure_expect("", 0, "a")

        val recursive2 = g.leftrec { choice { seq { it() && string("b") } || recursive() } }
        top_fun { recursive2() }
        success("ab")
        success("aaab")
        success("abbb")
        success("aaabbb")
        failure_expect("b", 0, "a")

    }

    // ---------------------------------------------------------------------------------------------

    @Test fun leftrec_builders()
    {
        fun Grammar.base()
            = transact { stack.push("1"); string("a") }

        fun Grammar.rec(p: Parser)
            = seq { p() && string("a") && perform { stack.push("" + stack.pop() + "+") } }

        val recursive
            = g.leftrec { choice { rec(it) || base() } }

        top_fun { recursive() }
        success_expect("a", "1")
        success_expect("aaa", "1++")
    }

    // ---------------------------------------------------------------------------------------------
}