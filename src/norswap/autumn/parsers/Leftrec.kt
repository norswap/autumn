package norswap.autumn.parsers
import norswap.autumn.*
import norswap.autumn.undoable.UndoList

// -------------------------------------------------------------------------------------------------

@PublishedApi
internal data class Invocation (
    val invocation_pos: Int,
    var max_pos: Int,
    var max_delta: List<SideEffect>)
{
    fun is_at (pos: Int): Boolean
        = invocation_pos == pos

    fun has_delta(): Boolean
        = invocation_pos != max_pos

    fun grow (g: Grammar, pos: Int, ptr0: Int): Boolean
    {
        if (pos <= max_pos) return false
        max_pos = pos
        max_delta = g.diff(ptr0)
        return true
    }
}

// -------------------------------------------------------------------------------------------------

/**
 * Creates a left-recursive parser from the parser [p].
 *
 * This returns a parser that wraps [p] with logic that enables left-recursion
 * through recursive calls to itself (not to [p]).
 *
 * The returned parser works as follows for calls that are not left-recursive
 * (the call is not nested inside another call at the same input position):
 *
 * 1. The parser records the input position.
 *
 * 2. The parser runs [p]. All left-recursive calls (i.e. nested calls to the parser at the same
 * input position) will fail immediately.
 *
 * 3. After successfully parsing [p], the parser records its result (final input position, side
 * effects), then re-invokes it; but this time left-recursive calls will incur the result of the
 * previous successful invocation of [p].
 *
 * 4. This process (3) repeats itself until either [p] fails, or the result's input position
 * stops growing.
 */
inline fun Grammar.leftrec (crossinline p: Grammar.(self: Parser) -> Boolean)
    = object: Parser
    {
        val invocations = UndoList<Invocation>(this@leftrec)

        init {
            // avoid checking if stack is empty
            invocations._push(Invocation(-1, -1, emptyList<SideEffect>()))
        }

        override fun invoke(): Boolean
        {
            var invoc = invocations.peek()

            if (invoc.is_at(pos)) {
                if (!invoc.has_delta())
                    return false
                merge(invoc.max_pos, invoc.max_delta)
                return true
            }

            // This is the initial invocation at this position.

            invoc = Invocation(pos, pos, emptyList<SideEffect>())
            invocations.push(invoc)

            val ptr0 = log.size
            val pos0 = pos

            while (true)
            {
                p { invoke() }
                val grew = invoc.grow(this@leftrec, pos, ptr0)
                undo(pos0, ptr0)
                if (!grew) break
            }

            merge(invoc.max_pos, invoc.max_delta)
            return invoc.has_delta()
        }
    }

// -------------------------------------------------------------------------------------------------