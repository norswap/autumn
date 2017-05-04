package norswap.autumn.naive

import norswap.autumn.parsers.ahead
import norswap.autumn.parsers.ahead_pure
import norswap.autumn.parsers.not


// -------------------------------------------------------------------------------------------------

/**
 * Succeeds if [p] succeeds, but does not advance the input position (all other side effects of
 * [p] are retained).
 */
class Ahead (val p: Parser): Parser()
{
    override fun invoke() = grammar.ahead(p)
}

// -------------------------------------------------------------------------------------------------

/**
 * Succeeds if [p] succeeds, but does produce any side effect (does not even change the input
 * position).
 */
class AheadPure (val p: Parser): Parser()
{
    override fun invoke() = grammar.ahead_pure(p)
}

// -------------------------------------------------------------------------------------------------

/**
 * Succeeds only if [p] fails.
 */
class Not (val p: Parser): Parser()
{
    override fun invoke() = grammar.not(p)
}

// -------------------------------------------------------------------------------------------------
