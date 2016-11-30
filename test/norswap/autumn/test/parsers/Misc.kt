package norswap.autumn.test.parsers
import norswap.autumn.*
import norswap.autumn.parsers.*
import norswap.autumn.test.*
import norswap.autumn.undoable.undo_slot
import org.testng.Assert.*
import org.testng.annotations.Test

class Misc: EmptyGrammarFixture()
{
    // ---------------------------------------------------------------------------------------------

    @Test fun transact() {
        val x = g.undo_slot(0)
        top { transact { x += 1; true } }
        success("")
        assertEquals(x(), 1)

        top { transact { x += 2; false } }
        failure_at("", 0, UnspecifiedFailure)
        assertEquals(x(), 0)
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun perform() {
        val x = g.undo_slot(0)
        top { perform { x += 1 } }
        success("")
        assertEquals(x(), 1)

        top { seq { perform { x += 2 } && string("a") } }
        success("a")
        assertEquals(x(), 2)
        failure_expect("b", 0, "a")
        assertEquals(x(), 0)

    }

    // ---------------------------------------------------------------------------------------------

    @Test fun ignore_errors() {
        top { ignore_errors { fail(1) { "failed" } } }
        failure_at("", 0, UnspecifiedFailure)
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun ignore_errors_if_successful() {
        top { seq { ignore_errors_if_successful { fail(2) { "failed" }; true } && fail(1) { "boom" } } }
        failure_at("", 1, { "boom" })

        top { ignore_errors_if_successful { fail(1) { "failed" } } }
        failure_at("", 1, { "failed" })
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun affect() {
        fun Grammar.test_syntax() = transact {
            stack.push("a")
            stack.push("b")
            true
        }

        fun Grammar.affect_test(backlog: Int) = affect(backlog,
            syntax = { test_syntax() },
            effect = { stack.push(it.fold("", String::plus)) })

        top { affect_test(0) }
        success_expect("", "ab")

        top { stack.push("c"); affect_test(1) }
        success_expect("", "cab")

        top { build(Grammar::test_syntax) { it.fold("", String::plus) } }
        success_expect("", "ab")

        top { stack.push("c"); build(1, Grammar::test_syntax) { it.fold("", String::plus) } }
        success_expect("", "cab")

        fun Grammar.transact_test() = affect(
            syntax = { string("a") },
            effect = { stack.push("x") })

        top { choice { transact_test() || string("b") } }
        success("b")
        assertTrue(g.stack.isEmpty())
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun affect_recur() {
        fun Grammar.test_syntax1() = transact {
            stack.push("a")
            stack.push("b")
            true
        }

        fun Grammar.affect_test1(backlog: Int) = affect(backlog,
            syntax = { test_syntax1() },
            effect = { stack.push(it.fold("", String::plus)) })

        fun Grammar.test_syntax2(backlog: Int)
            = seq { affect_test1(backlog) && perform { stack.push("c") } }

        fun Grammar.affect_test2(backlog: Int) = affect(backlog,
            syntax = { test_syntax2(if (backlog == 0) 0 else backlog - 1) },
            effect = { stack.push(it.fold("", String::plus)) })

        top { affect_test2(0) }
        success_expect("", "abc")

        top { stack.push("u"); stack.push("v"); affect_test2(2) }
        success_expect("", "uvabc")
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun contain() {
        top { contain({ "caught" }) { false } }
        failure_at("", 0, { "caught" })

        top { seq { string("ab") && contain({ "caught" }) { false } } }
        failure_at("ab", 2, { "caught" })

        top { seq { string("ab") && contain({ "caught" }) { fail(42) { "nenuphar" } } } }
        failure_at("ab", 2, { "caught" })

        top { fail_force(9) { "cthulhu" } ; contain({ "caught" }) { false } }
        failure_at("", 9, { "cthulhu" })
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun catch()
    {
        val e = Exception("oops")

        top { throw e }
        failure_at("", 0, UncaughtException(e))

        top { catch { throw e } }
        failure_at("", 0, CaughtException(e))

        top { catch { throw AutumnLogicException { "oops" } } }
        failure_at("", 0, { "oops" })

        top { fail_force(9) { "cthulhu" } ; catch { throw e } }
        failure_at("", 9, { "cthulhu" })

        top { catch_contain { throw e } }
        failure_at("", 0, CaughtException(e))

        top { catch_contain { throw AutumnLogicException { "oops" } } }
        failure_at("", 0, { "oops" })

        top { catch { fail_force(9) { "cthulhu" } ; throw e } }
        failure_at("", 9, { "cthulhu" })

        top { catch_contain { fail_force(9) { "cthulhu" } ; throw e } }
        failure_at("", 0, CaughtException(e))
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun left_recusive() {
        val recursive = g.leftrec { choice { seq { it() && string("a") } || string("a") } }
        top { recursive() }
        success("a")
        success("aa")
        success("aaa")
        failure_expect("b", 0, "a")
        failure_expect("", 0, "a")

        val recursive2 = g.leftrec { choice { seq { it() && string("b") } || recursive() } }
        top { recursive2() }
        success("ab")
        success("aaab")
        success("abbb")
        success("aaabbb")
        failure_expect("b", 0, "a")

    }

    // ---------------------------------------------------------------------------------------------

    @Test fun leftrec_builders() {
        fun Grammar.base()
            = transact { stack.push("1"); string("a") }

        fun Grammar.rec(p: Parser)
            = seq { p() && string("a") && perform { stack.push("" + stack.pop() + "+") } }

        val recursive
            = g.leftrec { choice { rec(it) || base() } }

        top { recursive() }
        success_expect("a", "1")
        success_expect("aaa", "1++")
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun maybe() {
        top { maybe { stack.push("aaa") ; true } }
        success_expect("", "aaa")

        top { maybe { transact { stack.push("aaa") ; false } } }
        success_expect("", null)
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun build_str()
    {
        top { build_str { seq { word { string("xx") } && string("yy") } }}
        success_expect("xx  yy", "xx  yy")
    }

    // ---------------------------------------------------------------------------------------------
}