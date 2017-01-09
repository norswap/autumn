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
        top_fun { transact { x += 1; true } }
        success("")
        assertEquals(x(), 1)

        top_fun { transact { x += 2; false } }
        failure_at("", 0, UnspecifiedFailure)
        assertEquals(x(), 0)
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun perform() {
        val x = g.undo_slot(0)
        top_fun { perform { x += 1 } }
        success("")
        assertEquals(x(), 1)

        top_fun { seq { perform { x += 2 } && string("a") } }
        success("a")
        assertEquals(x(), 2)
        failure_expect("b", 0, "a")
        assertEquals(x(), 0)

    }

    // ---------------------------------------------------------------------------------------------

    @Test fun ignore_errors() {
        top_fun { ignore_errors { fail(1) { "failed" } } }
        failure_at("", 0, UnspecifiedFailure)
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun ignore_errors_if_successful() {
        top_fun { seq { ignore_errors_if_successful { fail(2) { "failed" }; true } && fail(1) { "boom" } } }
        failure_at("", 1, { "boom" })

        top_fun { ignore_errors_if_successful { fail(1) { "failed" } } }
        failure_at("", 1, { "failed" })
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun contain() {
        top_fun { contain({ "caught" }) { false } }
        failure_at("", 0, { "caught" })

        top_fun { seq { string("ab") && contain({ "caught" }) { false } } }
        failure_at("ab", 2, { "caught" })

        top_fun { seq { string("ab") && contain({ "caught" }) { fail(42) { "nenuphar" } } } }
        failure_at("ab", 2, { "caught" })

        top_fun { fail_force(9) { "cthulhu" } ; contain({ "caught" }) { false } }
        failure_at("", 9, { "cthulhu" })
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun catch()
    {
        val e = Exception("oops")

        top_fun { throw e }
        failure_at("", 0, UncaughtException(e))

        top_fun { catch { throw e } }
        failure_at("", 0, CaughtException(e))

        top_fun { catch { throw AutumnLogicException { "oops" } } }
        failure_at("", 0, { "oops" })

        top_fun { fail_force(9) { "cthulhu" } ; catch { throw e } }
        failure_at("", 9, { "cthulhu" })

        top_fun { catch_contain { throw e } }
        failure_at("", 0, CaughtException(e))

        top_fun { catch_contain { throw AutumnLogicException { "oops" } } }
        failure_at("", 0, { "oops" })

        top_fun { catch { fail_force(9) { "cthulhu" } ; throw e } }
        failure_at("", 9, { "cthulhu" })

        top_fun { catch_contain { fail_force(9) { "cthulhu" } ; throw e } }
        failure_at("", 0, CaughtException(e))
    }

    // ---------------------------------------------------------------------------------------------
}