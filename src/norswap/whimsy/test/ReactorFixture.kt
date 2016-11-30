package norswap.whimsy.test
import norswap.autumn.Grammar
import norswap.autumn.Parser
import norswap.autumn.test.*
import norswap.whimsy.Node
import norswap.whimsy.Reactor
import org.testng.Assert.assertEquals

/**
 *  A superclass for test class that wish to test the behaviour of a reactor.
 *
 *  Since it is often easier to go through a parse step to get a tree of nodes, this class extends
 *  [GrammarFixture] and redefines [top] so that it also resets the reactor each time.
 */
abstract class GrammarReactorFixture: GrammarFixture()
{
    // ---------------------------------------------------------------------------------------------

    override val g: Grammar = EmptyGrammar()

    // ---------------------------------------------------------------------------------------------

    private var reactor = Reactor()

    // ---------------------------------------------------------------------------------------------

    /**
     * Initializes the reactor with rules.
     */
    abstract fun Reactor.init()

    // ---------------------------------------------------------------------------------------------

    /**
     * Like [GrammarFixture.top], but also replaces the reactor by a fresh one, on which
     * [init] is called.
     */
    override fun top (p: Parser)
    {
        reactor = Reactor()
        reactor.init()
        super.top(p)
    }

    // ---------------------------------------------------------------------------------------------

    fun attr (input: String, name: String, value: Any?)
    {
        success(input)
        val root = g.stack.peek() as Node
        reactor.visit(root)
        reactor.start()
        assertEquals(root[name], value)
    }

    // ---------------------------------------------------------------------------------------------
}