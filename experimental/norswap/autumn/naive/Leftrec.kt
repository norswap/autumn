package norswap.autumn.naive

import norswap.autumn.Grammar
import norswap.autumn.parsers.leftrec
import norswap.autumn.undoable.UndoList

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
class Leftrec (val p: Grammar.(self: Parser) -> Boolean): Parser()
{
    // IS THIS CORRECT ???
    override fun invoke() = grammar.leftrec { p(this@Leftrec)}.invoke()
}

// -------------------------------------------------------------------------------------------------