package norswap.autumn.test.parsers
import norswap.autumn.Grammar
import norswap.autumn.parsers.*
import norswap.autumn.test.*
import org.testng.annotations.Test
import org.testng.Assert.*

class Choice: EmptyGrammarFixture()
{
    // ---------------------------------------------------------------------------------------------

    @Test fun choice()
    {
        top { choice { string("a") || string("b") } }
        success("a")
        success("b")
        failure_expect("", 0, "a")
        failure_expect("c", 0, "a")
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun longest()
    {
        fun Grammar.enda()
            = seq { string("a") && repeat1 { string("ba") } }

        fun Grammar.endb()
            = repeat1 { string("ab") }

        top { longest({ enda() }, { endb() }) }
        success("ab")
        success("aba")
        success("abab")
        failure_expect("", 0, "a") // expect failure on first alternate
        failure_expect("a", 1, "ba")
        failure_expect("abc", 2, "ab")
        failure_expect("abac", 3, "ba")
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun choice_transact()
    {
        // Choices are naturally transactional if their children are, nothing to test there.

        top { longest({ perform { stack.push(42) } }, { string("a") }) }
        success("a")

        top { longest({ perform { stack.push(42) } }, { stack.push(32); string("a") }) }
        success_expect("a", 32)
        assertEquals(g.stack.size, 1)
    }

    // ---------------------------------------------------------------------------------------------
}