package norswap.autumn

// -------------------------------------------------------------------------------------------------

/**
 * A parse failure is simply a function that returns a string describing the failure.
 *
 * Since only one failure is reported to the user, using functions avoid paying the cost
 * of putting together a whole lot of failure messages.
 */
typealias Failure = (() -> String)

// -------------------------------------------------------------------------------------------------

/**
 * Represents an exception intended to be caught by [Grammar.catch], carrying the [Failure]
 * to be recorded.
 */
class AutumnLogicException (val failure: Failure): Exception()
{
    override val message: String
        get() = failure()
}

// -------------------------------------------------------------------------------------------------
/*

The rest of this file defines all the failures that the pre-defined parser can record.
This makes it easy to consult them in one place.

 */
// -------------------------------------------------------------------------------------------------

object UnexpectedChar: Failure
{
    override fun invoke() = "unexpected character"
}

// -------------------------------------------------------------------------------------------------

class NoString (val str: String): Failure
{
    override fun invoke() = "could not match string \"$str\""
}

// -------------------------------------------------------------------------------------------------

class Expected (val what: String): Failure
{
    override fun invoke() = "expected $what"
}

// -------------------------------------------------------------------------------------------------

object ExpectedIdentifier: Failure
{
    override fun invoke() = "expected an identifier"
}

// -------------------------------------------------------------------------------------------------

object PartialMatch: Failure
{
    override fun invoke() = "partial match"
}

// -------------------------------------------------------------------------------------------------

object BadMatch: Failure
{
    override fun invoke() = "parser matched, but shouldn't have"
}

// -------------------------------------------------------------------------------------------------

object EarlyTermination: Failure
{
    override fun invoke() = "early termination: could not match any item before the terminator"
}

// -------------------------------------------------------------------------------------------------

class UnspecifiedFailureAt (val pos: String): Failure
{
    override fun invoke() = "unspecified failure at $pos"
}

// -------------------------------------------------------------------------------------------------

object UnspecifiedFailure: Failure
{
    override fun invoke() = "unspecified failure"
}

// -------------------------------------------------------------------------------------------------

object UnexpectedToken: Failure
{
    override fun invoke() = "unexpected token"
}

// -------------------------------------------------------------------------------------------------

class CaughtException (val e: Throwable): Failure
{
    override fun invoke() = "$e"
}

// -------------------------------------------------------------------------------------------------

class UncaughtException (val e: Throwable): Failure
{
    override fun invoke() = "uncaught: $e"
}

// -------------------------------------------------------------------------------------------------