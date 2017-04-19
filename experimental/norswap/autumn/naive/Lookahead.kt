package norswap.autumn.naive


// -------------------------------------------------------------------------------------------------

/**
 * Succeeds if [p] succeeds, but does not advance the input position (all other side effects of
 * [p] are retained).
 */
class ahead (val p: Parser): Parser()
{
    override fun invoke(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

// -------------------------------------------------------------------------------------------------

/**
 * Succeeds if [p] succeeds, but does produce any side effect (does not even change the input
 * position).
 */
class ahead_pure (val p: Parser): Parser()
{
    override fun invoke(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

// -------------------------------------------------------------------------------------------------

/**
 * Succeeds only if [p] fails.
 */
class not (val p: Parser): Parser()
{
    override fun invoke(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

// -------------------------------------------------------------------------------------------------
