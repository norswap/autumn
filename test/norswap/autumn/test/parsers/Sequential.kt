package norswap.autumn.test.parsers
import norswap.autumn.PartialMatch
import norswap.autumn.parsers.*
import norswap.autumn.test.*
import org.testng.Assert.assertTrue
import org.testng.annotations.Test

class Sequential: EmptyGrammarFixture()
{
    // ---------------------------------------------------------------------------------------------

    @Test fun seq() {
        top { seq { string("a") && string("b") && string("c") } }
        success("abc")
        failure_expect("bbc", 0, "a")
        failure_expect("aac", 1, "b")
        failure_expect("aba", 2, "c")

        top { choice { seq { perform { stack.push("a") } && string("a") } || string("b") } }
        success("b")
        assertTrue(g.stack.isEmpty())
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun opt() {
        top { opt { string("a") } }
        success("")
        success("a")
        failure_expect("b", 0, "a")
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun repeat0() {
        top { repeat0 { string("a") } }
        success("")
        success("a")
        success("aaa")
        failure_expect("b", 0, "a")
        failure_expect("aab", 2, "a")
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun repeat1() {
        top { repeat1 { string("a") } }
        success("a")
        success("aaa")
        failure_expect("", 0, "a")
        failure_expect("b", 0, "a")
        failure_expect("aab", 2, "a")
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun repeat() {
        top { repeat(3) { string("a") } }
        success("aaa")
        failure_expect("", 0, "a")
        failure_expect("aa", 2, "a")
        failure_at("aaaa", 3, PartialMatch)

        top { choice { repeat(3) { seq { perform { stack.push("a") } && string("a") } } || string("aa") } }
        success("aa")
        assertTrue(g.stack.isEmpty())
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun around0() {
        top { around0({ string("a") }, { string(",") }) }
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
        top { around1({ string("a") }, { string(",") }) }
        success("a")
        success("a,a")
        success("a,a,a")
        failure_expect("", 0, "a")
        failure_expect(",", 0, "a")
        failure_expect("a,", 2, "a")
        failure_expect(",a", 0, "a")
    }

    // ---------------------------------------------------------------------------------------------
}