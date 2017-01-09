package norswap.autumn.test.parsers
import norswap.autumn.Grammar
import norswap.autumn.parsers.*
import norswap.autumn.test.EmptyGrammarFixture
import org.testng.Assert
import org.testng.annotations.Test

class Stack: EmptyGrammarFixture()
{
    // ---------------------------------------------------------------------------------------------

    @Test fun maybe()
    {
        top_fun { maybe { stack.push("aaa") ; true } }
        success_expect("", "aaa")

        top_fun { maybe { transact { stack.push("aaa") ; false } } }
        success_expect("", null)
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun build_str()
    {
        top_fun { build_str { seq { word { string("xx") } && string("yy") } }}
        success_expect("xx  yy", "xx  yy")
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun affect()
    {
        fun Grammar.test_syntax() = transact {
            stack.push("a")
            stack.push("b")
            true
        }

        fun Grammar.affect_test(backlog: Int) = affect(backlog,
            syntax = { test_syntax() },
            effect = { stack.push(it.fold("", String::plus)) })

        top_fun { affect_test(0) }
        success_expect("", "ab")

        top_fun { stack.push("c"); affect_test(1) }
        success_expect("", "cab")

        top_fun { build(this::test_syntax) { it.fold("", String::plus) } }
        success_expect("", "ab")

        top_fun { stack.push("c"); build(1, this::test_syntax) { it.fold("", String::plus) } }
        success_expect("", "cab")

        fun Grammar.transact_test() = affect(
            syntax = { string("a") },
            effect = { stack.push("x") })

        top_fun { choice { transact_test() || string("b") } }
        success("b")
        Assert.assertTrue(g.stack.isEmpty())
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun affect_recur()
    {
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

        top_fun { affect_test2(0) }
        success_expect("", "abc")

        top_fun { stack.push("u"); stack.push("v"); affect_test2(2) }
        success_expect("", "uvabc")
    }

    // ---------------------------------------------------------------------------------------------
}