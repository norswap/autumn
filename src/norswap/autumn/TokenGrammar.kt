package norswap.autumn
import norswap.autumn.parsers.LongestPure
import norswap.autumn.parsers.string

// -------------------------------------------------------------------------------------------------

typealias TokenGenerator = (String) -> Any?

// -------------------------------------------------------------------------------------------------

abstract class TokenGrammar: Grammar()
{
    // ---------------------------------------------------------------------------------------------

    protected open class CacheEntry (val end: Int, val index: Int, val value: Any?)
    protected object CachedFailure: CacheEntry(-1, -1, null)

    // ---------------------------------------------------------------------------------------------

    /** @suppress */ protected val cache            = HashMap<Int, CacheEntry>()
    /** @suppress */ protected val parsers          = ArrayList<Parser>()
    /** @suppress */ protected val generators       = ArrayList<TokenGenerator>()
    /** @suppress */ protected var next_index       = 0
    /** @suppress */ protected var _parser: LongestPure? = null
    /** @suppress */ protected val parser:  LongestPure
        get() {
            if (_parser == null)
                _parser = LongestPure(this, parsers.toTypedArray())
            return _parser as LongestPure
        }

    // ---------------------------------------------------------------------------------------------

    override fun reset() {
        super.reset()
        cache.clear()
    }

    // ---------------------------------------------------------------------------------------------

    protected fun token (generator: TokenGenerator = { it }, p: Parser): Parser
    {
        val index = next_index++
        parsers.add(p)
        generators.add(generator)
        return TokenParser(intArrayOf(index))
    }

    // ---------------------------------------------------------------------------------------------

    inner class TokenParser (val indices: IntArray): Parser
    {
        override fun invoke(): Boolean
        {
            val entry = cache.getOrPut(pos) {
                var out: CacheEntry = CachedFailure
                val pos0 = pos
                val fail_pos0 = fail_pos
                val failure0 = failure
                val selected = parser.select()
                if (selected >= 0) {
                    val value = generators[selected](text.substring(pos0, pos))
                    out = CacheEntry(pos, selected, value)
                    pos = pos0
                }
                fail_pos = fail_pos0
                failure = failure0
                out
            }

            if (entry == CachedFailure || !indices.contains(entry.index)) {
                return fail(pos, UnexpectedToken)
            } else {
                pos = entry.end
                if (entry.value != null) stack.push(entry.value)
                parse_whitespace()
                return true
            }
        }
    }

    // ---------------------------------------------------------------------------------------------

    val String.token: Parser
        get() = token { string(this@token) }

    // ---------------------------------------------------------------------------------------------

    val String.keyword: Parser
        get() = token ({ null }) { string(this@keyword) }

    // ---------------------------------------------------------------------------------------------

    fun token_choice (vararg tokens: Parser): Parser
    {
        @Suppress("UNCHECKED_CAST")
        val parsers = tokens.toList() as List<TokenParser>
        val indices = parsers.fold(intArrayOf()) { a, b -> a + b.indices }
        return TokenParser(indices)
    }

    // ---------------------------------------------------------------------------------------------
}

