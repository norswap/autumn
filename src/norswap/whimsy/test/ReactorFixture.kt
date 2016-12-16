package norswap.whimsy.test
import norswap.autumn.Grammar
import norswap.autumn.test.*
import norswap.whimsy.Attribute
import norswap.whimsy.Node
import norswap.whimsy.Reactor
import norswap.whimsy.ReactorError
import org.testng.Assert.*

/**
 * TODO CHANGE THIS
 *
 *  A superclass for test class that wish to test the behaviour of a reactor.
 *
 *  Since it is often easier to go through a parse step to get a tree of nodes, this class extends
 *  [GrammarFixture] and redefines [top_fun] so that it also resets the reactor each time.
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

    fun parse (input: String): Node
    {
        success(input)
        val root = g.stack.peek() as Node
        reactor = Reactor()
        reactor.init()
        reactor.visit(root)
        reactor.start()
        return root
    }

    // ---------------------------------------------------------------------------------------------

    fun attr (input: String, name: String, value: Any?)
    {
        val root = parse(input)
        assertEquals(root[name], value)
    }

    // ---------------------------------------------------------------------------------------------

    fun error (input: String, attribute: Attribute, msg: String)
    {
        parse(input)
        assertEquals(reactor.errors.size, 1)
        assertTrue(reactor.errors[0].affected.contains(attribute))
        assertEquals(reactor.errors[0].msg, msg)
    }

    // ---------------------------------------------------------------------------------------------

    fun root_error (input: String, name: String, klass: Class<out ReactorError>)
    {
        val root = parse(input)
        success(input)
        if (reactor.errors.size > 1) {
            println(reactor.errors[0])
            println(reactor.errors[1])
        }
        assertEquals(reactor.errors.size, 1)
        assertTrue(reactor.errors[0].affected.contains(root(name)))
        assertEquals(reactor.errors[0].javaClass, klass)
    }

    // ---------------------------------------------------------------------------------------------
}