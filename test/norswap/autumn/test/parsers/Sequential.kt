package norswap.autumn.test.parsers
import norswap.autumn.EarlyTermination
import norswap.autumn.Grammar
import norswap.autumn.PartialMatch
import norswap.autumn.parsers.*
import norswap.autumn.test.*
import org.testng.Assert.assertTrue
import org.testng.annotations.Test

class Sequential: EmptyGrammarFixture()
{
    // ---------------------------------------------------------------------------------------------

    @Test fun seq() {
        top_fun { seq { string("a") && string("b") && string("c") } }
        success("abc")
        failure_expect("bbc", 0, "a")
        failure_expect("aac", 1, "b")
        failure_expect("aba", 2, "c")

        top_fun { choice { seq { perform { stack.push("a") } && string("a") } || string("b") } }
        success("b")
        assertTrue(g.stack.isEmpty())
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun opt() {
        top_fun { opt { string("a") } }
        success("")
        success("a")
        failure_expect("b", 0, "a")
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun repeat0() {
        top_fun { repeat0 { string("a") } }
        success("")
        success("a")
        success("aaa")
        failure_expect("b", 0, "a")
        failure_expect("aab", 2, "a")
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun repeat1() {
        top_fun { repeat1 { string("a") } }
        success("a")
        success("aaa")
        failure_expect("", 0, "a")
        failure_expect("b", 0, "a")
        failure_expect("aab", 2, "a")
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun repeat() {
        top_fun { repeat(3) { string("a") } }
        success("aaa")
        failure_expect("", 0, "a")
        failure_expect("aa", 2, "a")
        failure_at("aaaa", 3, PartialMatch)

        top_fun { choice { repeat(3) { seq { perform { stack.push("a") } && string("a") } } || string("aa") } }
        success("aa")
        assertTrue(g.stack.isEmpty())
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun around0() {
        top_fun { around0({ string("a") }, { string(",") }) }
        success("")
        success("a")
        success("a,a")
        success("a,a,a")
        failure_expect(",", 0, "a")
        failure_expect("a,", 2, "a")
        failure_expect(",a", 0, "a")
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun around1() {
        top_fun { around1({ string("a") }, { string(",") }) }
        success("a")
        success("a,a")
        success("a,a,a")
        failure_expect("", 0, "a")
        failure_expect(",", 0, "a")
        failure_expect("a,", 2, "a")
        failure_expect(",a", 0, "a")
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun until0() {
        top_fun { until0({ string("a") }, { string("b") }) }
        success("b")
        success("ab")
        success("aaab")
        failure_expect("", 0, "b")
        failure_expect("a", 1, "b")
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun until1() {
        top_fun { until1({ string("a") }, { string("b") }) }
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

        top_fun { choice { until_ex0() || string("aac") } }
        success("aac")
        assertTrue(g.stack.isEmpty())

        top_fun { choice { until_ex1() || string("aac") } }
        success("aac")
        assertTrue(g.stack.isEmpty())
    }

    // ---------------------------------------------------------------------------------------------
}