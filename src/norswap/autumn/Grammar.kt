package norswap.autumn
import norswap.autumn.parsers.*
import norswap.autumn.undoable.UndoList
import norswap.utils.arrayOfSize
import java.io.PrintStream
import java.util.ArrayList

abstract class Grammar
{
    // ---------------------------------------------------------------------------------------------

    /**
     * Input position for the current parse.
     */
    var pos = 0

    /**
     * Null-terminated input text for the current parse.
     */
    var text: String = ""
        private set

    /**
     * Anything printed during the parse will go to this stream.
     * Nothing is printed by default.
     */
    var out: PrintStream = System.out

    /**
     * A backtrack-safe stack available for use, typically to build up AST nodes.
     */
    val stack  = UndoList<Any?>(this)

    // ---------------------------------------------------------------------------------------------

    /**
     * A list of issues to be reported even if the issues occured in parsers who did not contribute
     * to the parse.
     */
    val persistent_issues = ArrayList<Failure>()

    /**
     * A list of issues to be reported, only if they occurred in parsers who did contribute
     * to the parse.
     */
    val transient_issues = UndoList<Failure>(this)

    /**
     * Returns all issues detected by the parse (a concatenation of [persistent_issues] and
     * [transient_issues]).
     */
    val issues: List<Failure>
        get() = persistent_issues + transient_issues

    // ---------------------------------------------------------------------------------------------

    private var input: ParseInput? = null

    val log = ArrayList<AppliedChange>()

    // ---------------------------------------------------------------------------------------------

    /**
     * Position of the candidate failure for the current parse.
     * @suppress
     */
    var fail_pos = -1

    /**
     * Provides the error message for the candidate failure.
     * @suppress
     */
    var failure: Failure? = null

    // ---------------------------------------------------------------------------------------------

    /**
     * If [pos] is superior to the candidate position, record it as the candidate failure position
     * and [failure] as its associated failure.
     */
    fun fail (pos: Int, failure: () -> String): Boolean
    {
        if (pos > fail_pos)
        {
            fail_pos = pos
            this.failure = failure
        }
        return false
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Record [pos] as the candidate failure position and [failure] as its associated
     * failure.
     */
    fun fail_force (pos: Int, failure: () -> String): Boolean
    {
        fail_pos = pos
        this.failure = failure
        return false
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Resets the grammar for a new parse.
     */
    fun reset()
    {
        reset_state()
        pos = 0
        text = ""
        input = null
        fail_pos = -1
        failure = null
        persistent_issues.clear()
    }

    /**
     * Called by [reset] to reset the state for a new parse. The default implementation simply
     * undoes the whole log, but subclasses can override this method to implement their custom
     * (possibly more expeditive) reset logic.
     */
    protected open fun reset_state()
    {
        undo(0, 0)
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * The root parser for this grammar, which will be invoked by [parse].
     */
    abstract fun root(): Boolean

    // ---------------------------------------------------------------------------------------------

    /**
     * The parser used by the [word] parser to skip whitespace.
     *
     * Failures within this parser will be ignored.
     */
    open fun whitespace(): Boolean
        = repeat0 { space_char() }

    // ---------------------------------------------------------------------------------------------

    fun parse_whitespace(): Boolean
        = ignore_errors { opt { whitespace() } }

    // ---------------------------------------------------------------------------------------------

    /**
     * Starts a parse, using the supplied parser as root. [allow_prefix] controls whether
     * the whole input must match, or if a prefix match is sufficient.
     */
    fun parse (input: ParseInput, allow_prefix: Boolean, parser: Parser): Boolean
    {
        this.input = input
        this.text = input.text

        var result =
            try { parser() }
            catch (e: Throwable) {
                fail_force(pos, UncaughtException(e))
                false
            }

        val partial_match = result && pos != input.size

        if (partial_match && !allow_prefix) {
            // If we matched up to pos < length, and no errors are available at the reached position
            // or beyond; use "partial match" as the failure.

            if (fail_pos < pos || fail_pos == -1)
                fail_force(pos, PartialMatch)

            result = false
        }

        if (result) {
            fail_pos = -1
            failure = null
        }
        else {
            // in case the failure wasn't set (bad!)
            if (failure == null)
                if (fail_pos >= 0)
                    failure = UnspecifiedFailureAt(input.offsetToString(fail_pos))
                else
                    failure = UnspecifiedFailure

            // in case the failure position wasn't set (bad!)
            if (fail_pos < 0) fail_pos = 0

            // in case the root wasn't transactional (bad!)
            // or the failure results from a partial match
            undo(0, 0)
        }

        return result
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Starts a parse. The parse must match the whole input text or a failure is returned.
     */
    fun parse (input: ParseInput): Boolean
    {
        return parse(input, false) { root() }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Starts a parse. The parse must match the whole input string or a failure is returned.
     */
    fun parse (str: String): Boolean
    {
        return parse(ParseInput(str), false)  { root() }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Starts a parse. The parse may match only a prefix of the input text.
     */
    fun parsePrefix (input: ParseInput): Boolean
    {
        return parse(input, true)  { root() }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Starts a parse. The parse may match only a prefix of the input string.
     */
    fun parsePrefix (str: String): Boolean
    {
        return parse(ParseInput(str), true)  { root() }
    }

    // ---------------------------------------------------------------------------------------------

    fun undo (pos0: Int, ptr0: Int)
    {
        pos = pos0

        while (log.size > ptr0) {
            val change = log.removeAt(log.lastIndex)
            change.undo(this)
        }
    }

    // ---------------------------------------------------------------------------------------------

    fun diff (ptr: Int): List<Change>
    {
        if (ptr == log.size) return emptyList()
        return log.subList(ptr, log.size).map { it.change }
    }

    // ---------------------------------------------------------------------------------------------

    fun merge (pos1: Int, changes: List<Change>)
    {
        pos = pos1

        if (changes.isEmpty())
            return
        for (change in changes)
            apply(change)
    }

    // ---------------------------------------------------------------------------------------------

    fun apply (change: Change)
    {
        log.add(AppliedChange(change, change(this)))
    }

    // ---------------------------------------------------------------------------------------------

    private fun frame_check_backlog (backlog: Int)
    {
        if (stack.size < backlog)
            throw NoSuchElementException(
                "Build stack exhausted, but a backlog of $backlog was specified.")
    }

    // ---------------------------------------------------------------------------------------------

    fun frame_start (backlog: Int = 0): Int
    {
        frame_check_backlog(backlog)
        return stack.size - backlog
    }

    // ---------------------------------------------------------------------------------------------

    @Suppress("UNCHECKED_CAST")
    fun frame_end (frame: Int): Array<Any?>
    {
        val len = stack.size - frame
        val out = arrayOfSize<Any?>(len)
        for (i in 1..len) out[len - i] = stack.pop()
        return out
    }

    // ---------------------------------------------------------------------------------------------

    fun frame (backlog: Int): Array<Any?>
    {
        frame_check_backlog(backlog)
        val out = arrayOfSize<Any?>(backlog)
        for (i in 1..backlog) out[backlog - i] = stack.pop()
        return out
    }

    // ---------------------------------------------------------------------------------------------

    @Suppress("UNCHECKED_CAST")
    operator fun <T> Array<Any?>.invoke (i: Int): T
    {
        return this[i] as T
    }

    // ---------------------------------------------------------------------------------------------

    @Suppress("UNCHECKED_CAST")
    fun <T> Array<Any?>.list(start: Int = 0, end: Int = size - 1): List<T>
    {
        return this.slice(start..end) as List<T>
    }

    // ---------------------------------------------------------------------------------------------

    val String.str: Boolean
        get() = string(this@str)

    // ---------------------------------------------------------------------------------------------

    val String.word: Boolean
        get() = word { string(this@word) }

    // ---------------------------------------------------------------------------------------------

    val String.set: Boolean
        get() = char_set(this)

    // ---------------------------------------------------------------------------------------------

    operator fun String.unaryPlus() = word(this)

    // ---------------------------------------------------------------------------------------------

    fun offsetToString(offset: Int)
        = input!!.offsetToString(offset)

    // ---------------------------------------------------------------------------------------------
}
