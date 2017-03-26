package norswap.utils
import norswap.utils.thread_local.*

/**
 * Thread-local context for readable [Object.toString] implementation with consistent
 * indentation.
 */
object StringContext
{
    // ---------------------------------------------------------------------------------------------

    /**
     * Thread-local counter that represents indentation (number of space chars).
     */
    var indentation by thread_local(0)

    // ---------------------------------------------------------------------------------------------

    /**
     * Return a string made of [indentation] space chars.
     */
    fun spaces() = " ".repeat(indentation)

    // ---------------------------------------------------------------------------------------------

    /**
     * Runs [f] with indentation increased by [amount] (default: 4).
     */
    inline fun indented (amount: Int = 4, f: () -> String): String
    {
        indentation += amount
        val str = f()
        indentation -= amount
        return str
    }

    // ---------------------------------------------------------------------------------------------
}