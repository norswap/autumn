package norswap.autumn.test.parsers
import norswap.autumn.PartialMatch
import norswap.autumn.BadMatch
import norswap.autumn.parsers.*
import norswap.autumn.test.*
import org.testng.Assert.assertTrue
import org.testng.annotations.Test

class Lookahead : EmptyGrammarFixture()
{
    // ---------------------------------------------------------------------------------------------

    @Test fun ahead() {
        top_fun { seq { ahead { string("a") } && string("a") } }
        success("a")
        failure_at("ab", 1, PartialMatch)
        failure_expect("", 0, "a")

        top_fun { seq { ahead { string("ab") } && string("a") && string("b") && string("c") } }
        success("abc")
        failure_expect("ab", 2, "c")
        failure_expect("ac", 0, "ab")
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun not() {
        top_fun { seq { not { string("a") } && string("b") } }
        success("b")
        failure_at("a", 0, BadMatch)
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun lookahead_transact() {
        // Ahead is transactional if its child is.
        // For Not, we need to check the case where the child succeeds.

        top_fun { choice { not { stack.push(42); string("a") } || string("a") } }
        success("a")
        assertTrue(g.stack.isEmpty())
    }

    // ---------------------------------------------------------------------------------------------
}