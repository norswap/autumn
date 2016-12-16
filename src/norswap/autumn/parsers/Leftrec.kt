package norswap.autumn.parsers
import norswap.autumn.*
import norswap.autumn.undoable.UndoList

// -------------------------------------------------------------------------------------------------

data class Invocation (
    val invocation_pos: Int,
    var max_pos: Int,
    var max_delta: List<Change>)
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

inline fun Grammar.leftrec (crossinline p: Grammar.(self: Parser) -> Boolean)
    = object: Parser
    {
        val invocations = UndoList<Invocation>(this@leftrec)

        init {
            // avoid checking if stack is empty
            invocations._push(Invocation(-1, -1, emptyList<Change>()))
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

            invoc = Invocation(pos, pos, emptyList<Change>())
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