package norswap.autumn.test.parsers
import norswap.autumn.EarlyTermination
import norswap.autumn.Grammar
import norswap.autumn.parsers.*
import norswap.autumn.test.*
import org.testng.Assert.assertTrue
import org.testng.annotations.Test

class Until : EmptyGrammarFixture()
{
    // ---------------------------------------------------------------------------------------------

    @Test fun until0() {
        top { until0({ string("a") }, { string("b") }) }
        success("b")
        success("ab")
        success("aaab")
        failure_expect("", 0, "b")
        failure_expect("a", 1, "b")
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun until1() {
        top { until1({ string("a") }, { string("b") }) }
        success("ab")
        success("aaab")
        failure_at("b", 0, EarlyTermination)
        failure_expect("", 0, "b")
        failure_expect("a", 1, "b")
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun until_transact() {
        fun Grammar.until_ex0()
            = until0({ seq { perform { stack.push("a") } && string("a") } }, { string("b") })

        fun Grammar.until_ex1()
            = until1({ seq { perform { stack.push("a") } && string("a") } }, { string("b") })

        top { choice { until_ex0() || string("aac") } }
        success("aac")
        assertTrue(g.stack.isEmpty())

        top { choice { until_ex1() || string("aac") } }
        success("aac")
        assertTrue(g.stack.isEmpty())
    }

    // ---------------------------------------------------------------------------------------------
}