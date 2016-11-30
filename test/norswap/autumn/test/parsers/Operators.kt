package norswap.autumn.test.parsers
import norswap.autumn.Grammar
import norswap.autumn.Parser
import norswap.autumn.parsers.*
import norswap.autumn.test.*
import org.testng.Assert.*
import org.testng.annotations.Test

class Operators: EmptyGrammarFixture()
{
    // ---------------------------------------------------------------------------------------------

    fun success_op(input: String, expect: String) {
        success(input)
        assertEquals(g.stack.peek(), expect)
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun precedenceLeft() {
        top(PrecedenceLeft {
            higher { string("a") }
            op_stackless(
                syntax = { string("+") },
                effect = {})
        })
        success("a")
        success("a+a")
        success("a+a+a")
        failure_expect("", 0, "a")
        failure_expect("a+", 2, "a")
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun precedenceRight() {
        top(PrecedenceRight {
            higher { string("a") }
            op_stackless(
                syntax = { string("+") },
                effect = {})
        })
        success("a")
        success("a+a")
        success("a+a+a")
        failure_expect("", 0, "a")
        failure_expect("a+", 2, "a")
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun precedenceLeft2() {
        top(PrecedenceLeft {
            higher { build_str { string("a") } }
            op(2,
                syntax = { string("+") },
                effect = { "(" + it[0] + "+" + it[1] + ")" })
        })
        success_op("a", "a")
        success_op("a+a", "(a+a)")
        success_op("a+a+a", "((a+a)+a)")
        failure_expect("", 0, "a")
        failure_expect("a+", 2, "a")
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun precedenceRight2() {
        top(PrecedenceRight {
            higher { build_str { string("a") } }
            op(2,
                syntax = { string("+") },
                effect =  { "(" + it[0] + "+" + it[1] + ")" })
        })
        success_op("a", "a")
        success_op("a+a", "(a+a)")
        success_op("a+a+a", "(a+(a+a))")
        failure_expect("", 0, "a")
        failure_expect("a+", 2, "a")
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun op_transact() {
        top(PrecedenceLeft {
            higher { string("a") && opt { build_str { string("x") } } }
            op_stackless(
                syntax = { string("+") },
                effect = {})
        })
        success("a+a+a")
        assertTrue(g.stack.isEmpty())
        success("ax+ax+a")
        assertEquals(g.stack.size, 2)


        top(PrecedenceRight {
            higher { string("a") && opt { build_str { string("x") } } }
            op_stackless(
                syntax = { string("+") },
                effect = {})
        })
        success("a+a+a")
        assertTrue(g.stack.isEmpty())
        success("ax+ax+a")
        assertEquals(g.stack.size, 2)
    }

    // ---------------------------------------------------------------------------------------------

    val a_str: Parser = { build_str { string("a") } }
    val mult: Parser = { string("*") }
    val plus: Parser = { string("+") }
    val minus: Parser = { build_str { string("-") } }
    val multe: Grammar.(Array<Any?>) -> Any? = { "(" + it[0] + "*" + it[1] + ")" }
    val pluse: Grammar.(Array<Any?>) -> Any? = { "(" + it[0] + "+" + it[1] + ")" }
    val minuse: Grammar.(Array<Any?>) -> Any? = { "" + it[0] + it[1] + it[2] }

    val Grammar.aprodl: Parser
        get() = PrecedenceLeft { higher(a_str) ; op(2, mult, multe)}
    val Grammar.aprodr: Parser
        get() = PrecedenceRight { higher(a_str) ; op(2, mult, multe)}
    val Grammar.asumll: Parser
        get() = PrecedenceLeft { higher { aprodl() } ; op(2, plus, pluse) }
    val Grammar.asumlr: Parser
        get() = PrecedenceLeft { higher { aprodr() } ; op(2, plus, pluse) }
    val Grammar.asumrl: Parser
        get() = PrecedenceRight { higher { aprodl() } ; op(2, plus, pluse) }
    val Grammar.asumrr: Parser
        get() = PrecedenceRight { higher { aprodr() } ; op(2, plus, pluse) }
    val Grammar.aweirdll: Parser
        get() = PrecedenceLeft { higher(minus) ; op(3, { aprodl() }, minuse)}
    val Grammar.aweirdlr: Parser
        get() = PrecedenceLeft { higher(minus) ; op(3, { aprodr() }, minuse)}
    val Grammar.aweirdrl: Parser
        get() = PrecedenceRight { higher(minus) ; op(3, { aprodl() }, minuse)}
    val Grammar.aweirdrr: Parser
        get() = PrecedenceRight { higher(minus) ; op(3, { aprodr() }, minuse)}

    @Test fun op_nested() {
        top { asumll() }
        success_expect("a*a*a+a*a+a*a", "((((a*a)*a)+(a*a))+(a*a))")

        top { asumlr() }
        success_expect("a*a*a+a*a+a*a", "(((a*(a*a))+(a*a))+(a*a))")

        top { asumrl() }
        success_expect("a*a*a+a*a+a*a", "(((a*a)*a)+((a*a)+(a*a)))")

        top { asumrr() }
        success_expect("a*a*a+a*a+a*a", "((a*(a*a))+((a*a)+(a*a)))")

        top { aweirdll() }
        success_expect("-a*a-a*a-", "-(a*a)-(a*a)-")

        top { aweirdlr() }
        success_expect("-a*a-a*a-", "-(a*a)-(a*a)-")

        top { aweirdrl() }
        success_expect("-a*a-a*a-", "-(a*a)-(a*a)-")

        top { aweirdrr() }
        success_expect("-a*a-a*a-", "-(a*a)-(a*a)-")
    }

    // ---------------------------------------------------------------------------------------------

    fun Grammar.parenthesized(): Boolean
        = seq { string("(") && aprodrecl() && string(")") }

    val Grammar.aprodrecl: Parser
        get() = PrecedenceLeft {
            higher { build_str { string("a") }}
            op(3,
                syntax = { parenthesized() },
                effect = { "(" + it[0] + it[1] + it[2] + ")" } )
        }

    val Grammar.aprodrecr: Parser
        get() = PrecedenceRight {
            higher { build_str { string("a") }}
            op(3,
                syntax = { parenthesized() },
                effect = { "(" + it[0] + it[1] + it[2] + ")" } )
        }

    @Test fun op_recursive()
    {
        top { aprodrecl() }
        success_expect("a(a)a", "(aaa)")
        success_expect("a(a(a)a)a", "(a(aaa)a)")
        success_expect("a(a(a)a)a(a)a(a(a)a)a", "(((a(aaa)a)aa)(aaa)a)")

        top { aprodrecr() }
        success_expect("a(a)a", "(aaa)")
        success_expect("a(a(a)a)a", "(a(aaa)a)")
        success_expect("a(a(a)a)a(a)a(a(a)a)a", "(a(aaa)(aa(a(aaa)a)))")
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun right_left() {
        val pl = PrecedenceLeft {
            left  = { build_str { string("a") } }
            right = { build_str { string("b") } }
            op(2,
                syntax = { string("+") },
                effect = { "(" + it[0] + "+" + it[1] + ")" })
        }

        val pr = PrecedenceRight {
            left  = { build_str { string("a") } }
            right = { build_str { string("b") } }
            op(2,
                syntax = { string("+") },
                effect = { "(" + it[0] + "+" + it[1] + ")" })
        }

        top { pl() }
        success_expect("a+b+b", "((a+b)+b)")

        top { pr() }
        success_expect("a+b+b", "(a+(b+b))")
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun op_suffix() {
        val pl = PrecedenceLeft {
            higher { build_str { string("a") } }
            op_suffix(1,
                syntax = { string("+b") },
                effect = { "(" + it[0] + "+b)"  })
        }

        val pr = PrecedenceRight {
            higher { build_str { string("a") } }
            op_suffix(2,
                syntax = { seq { string("+b") && perform { stack.push("b") } } },
                effect = { "(" + it[0] + "+" + it[1] + ")" })
        }

        top { pl() }
        success_expect("a+b+b", "((a+b)+b)")

        top { pr() }
        success_expect("a+b+b", "(a+(b+b))")
    }

    // ---------------------------------------------------------------------------------------------
}