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
        top_val {
            this.assoc_left {
                operands = { string("a") }
                op_stackless(
                    syntax = { string("+") },
                    effect = {})
            }
        }
        success("a")
        success("a+a")
        success("a+a+a")
        failure_expect("", 0, "a")
        failure_expect("a+", 2, "a")
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun precedenceRight() {
        top_val {
            g.assoc_right {
                operands = { g.string("a") }
                op_stackless(
                    syntax = { g.string("+") },
                    effect = {})
            }
        }
        success("a")
        success("a+a")
        success("a+a+a")
        failure_expect("", 0, "a")
        failure_expect("a+", 2, "a")
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun precedenceLeft2() {
        top_val {
            assoc_left {
                operands = { build_str { string("a") } }
                op( syntax = { string("+") },
                    effect = { "(" + it[0] + "+" + it[1] + ")" })
            }
        }
        success_op("a", "a")
        success_op("a+a", "(a+a)")
        success_op("a+a+a", "((a+a)+a)")
        failure_expect("", 0, "a")
        failure_expect("a+", 2, "a")
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun precedenceRight2() {
        top_val {
            assoc_right {
                operands = { build_str { string("a") } }
                op(2,
                    syntax = { string("+") },
                    effect =  { "(" + it[0] + "+" + it[1] + ")" })
            }
        }
        success_op("a", "a")
        success_op("a+a", "(a+a)")
        success_op("a+a+a", "(a+(a+a))")
        failure_expect("", 0, "a")
        failure_expect("a+", 2, "a")
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun op_transact() {
        top_val {
            assoc_left {
                operands = { string("a") && opt { build_str { string("x") } } }
                op_stackless(
                    syntax = { string("+") },
                    effect = {})
            }
        }
        success("a+a+a")
        assertTrue(g.stack.isEmpty())
        success("ax+ax+a")
        assertEquals(g.stack.size, 2)


        top_val {
            assoc_right {
                operands = { string("a") && opt { build_str { string("x") } } }
                op_stackless(
                    syntax = { string("+") },
                    effect = {})
            }
        }
        success("a+a+a")
        assertTrue(g.stack.isEmpty())
        success("ax+ax+a")
        assertEquals(g.stack.size, 2)
    }

    // ---------------------------------------------------------------------------------------------

    val a_str   : Parser = with(g) { { build_str { string("a") } } }
    val mult    : Parser = with(g) { { string("*") } }
    val plus    : Parser = with(g) { { string("+") } }
    val minus   : Parser = with(g) { { build_str { string("-") } } }

    val multe   : Grammar.(Array<Any?>) -> Any? = { "(" + it[0] + "*" + it[1] + ")" }
    val pluse   : Grammar.(Array<Any?>) -> Any? = { "(" + it[0] + "+" + it[1] + ")" }
    val minuse  : Grammar.(Array<Any?>) -> Any? = { "" + it[0] + it[1] + it[2] }

    val Grammar.aprodl: Parser
        get() = assoc_left { operands = a_str ; op(mult, multe)}
    val Grammar.aprodr: Parser
        get() = assoc_right { operands = a_str ; op(2, mult, multe)}
    val Grammar.asumll: Parser
        get() = assoc_left { operands = { aprodl() } ; op(plus, pluse) }
    val Grammar.asumlr: Parser
        get() = assoc_left { operands = { aprodr() } ; op(plus, pluse) }
    val Grammar.asumrl: Parser
        get() = assoc_right { operands = { aprodl() } ; op(2, plus, pluse) }
    val Grammar.asumrr: Parser
        get() = assoc_right { operands = { aprodr() } ; op(2, plus, pluse) }
    val Grammar.aweirdll: Parser
        get() = assoc_left { operands = minus ; op({ aprodl() }, minuse)}
    val Grammar.aweirdlr: Parser
        get() = assoc_left { operands = minus ; op({ aprodr() }, minuse)}
    val Grammar.aweirdrl: Parser
        get() = assoc_right { operands = minus ; op(3, { aprodl() }, minuse)}
    val Grammar.aweirdrr: Parser
        get() = assoc_right { operands = minus ; op(3, { aprodr() }, minuse)}

    @Test fun op_nested() {
        top_fun { asumll() }
        success_expect("a*a*a+a*a+a*a", "((((a*a)*a)+(a*a))+(a*a))")

        top_fun { asumlr() }
        success_expect("a*a*a+a*a+a*a", "(((a*(a*a))+(a*a))+(a*a))")

        top_fun { asumrl() }
        success_expect("a*a*a+a*a+a*a", "(((a*a)*a)+((a*a)+(a*a)))")

        top_fun { asumrr() }
        success_expect("a*a*a+a*a+a*a", "((a*(a*a))+((a*a)+(a*a)))")

        top_fun { aweirdll() }
        success_expect("-a*a-a*a-", "-(a*a)-(a*a)-")

        top_fun { aweirdlr() }
        success_expect("-a*a-a*a-", "-(a*a)-(a*a)-")

        top_fun { aweirdrl() }
        success_expect("-a*a-a*a-", "-(a*a)-(a*a)-")

        top_fun { aweirdrr() }
        success_expect("-a*a-a*a-", "-(a*a)-(a*a)-")
    }

    // ---------------------------------------------------------------------------------------------

    fun Grammar.parenthesized(): Boolean
        = seq { string("(") && aprodrecl() && string(")") }

    val Grammar.aprodrecl: Parser
        get() = assoc_left {
            operands = { build_str { string("a") }}
            op( syntax = { parenthesized() },
                effect = { "(" + it[0] + it[1] + it[2] + ")" } )
        }

    val Grammar.aprodrecr: Parser
        get() = assoc_right {
            operands = { build_str { string("a") }}
            op(3,
                syntax = { parenthesized() },
                effect = { "(" + it[0] + it[1] + it[2] + ")" } )
        }

    @Test fun op_recursive()
    {
        top_fun { aprodrecl() }
        success_expect("a(a)a", "(aaa)")
        success_expect("a(a(a)a)a", "(a(aaa)a)")
        success_expect("a(a(a)a)a(a)a(a(a)a)a", "(((a(aaa)a)aa)(aaa)a)")

        top_fun { aprodrecr() }
        success_expect("a(a)a", "(aaa)")
        success_expect("a(a(a)a)a", "(a(aaa)a)")
        success_expect("a(a(a)a)a(a)a(a(a)a)a", "(a(aaa)(aa(a(aaa)a)))")
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun right_left()
    {
        top_val {
            assoc_left {
                left  = { build_str { string("a") } }
                right = { build_str { string("b") } }
                op( syntax = { string("+") },
                    effect = { "(" + it[0] + "+" + it[1] + ")" })
            }
        }
        success_expect("a+b+b", "((a+b)+b)")

        top_val {
            assoc_right {
                left  = { build_str { string("a") } }
                right = { build_str { string("b") } }
                op(2,
                    syntax = { string("+") },
                    effect = { "(" + it[0] + "+" + it[1] + ")" })
            }
        }
        success_expect("a+b+b", "(a+(b+b))")
    }

    // ---------------------------------------------------------------------------------------------

    @Test fun op_suffix()
    {
        top_val {
            assoc_left {
                operands = { build_str { string("a") } }
                postfix(syntax = { string("+b") },
                        effect = { "(" + it[0] + "+b)"  })
            }
        }
        success_expect("a+b+b", "((a+b)+b)")
    }

    // ---------------------------------------------------------------------------------------------
}