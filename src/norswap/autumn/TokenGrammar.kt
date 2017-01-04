package norswap.autumn
import norswap.autumn.parsers.ignore_errors
import norswap.autumn.parsers.longest
import norswap.autumn.parsers.string
import java.util.*

// -------------------------------------------------------------------------------------------------

typealias TokenGenerator = (String) -> Any?

// -------------------------------------------------------------------------------------------------

abstract class TokenGrammar: Grammar()
{
    // ---------------------------------------------------------------------------------------------

    protected open class CacheEntry (val end: Int, val type: Int, val value: Any?)
    protected object CachedFailure: CacheEntry(-1, -1, null)

    // ---------------------------------------------------------------------------------------------

    /** @suppress */ protected val cache       = HashMap<Int, CacheEntry>()
    /** @suppress */ protected val parsers     = ArrayList<Parser>()
    /** @suppress */ protected val generators  = ArrayList<TokenGenerator>()
    /** @suppress */ protected var type_gen    = 0
    /** @suppress */ protected val parser_array by lazy { parsers.toTypedArray() }

    /** @suppress */ var max_pos  = -1
    /** @suppress */ var max_type = -1

    // ---------------------------------------------------------------------------------------------

    override fun reset_state() {
        super.reset_state()
        cache.clear()
    }

    // ---------------------------------------------------------------------------------------------

    protected inline fun token (noinline generator: TokenGenerator = { it }, crossinline p: Parser): Parser
    {
        val type = type_gen++

        parsers.add({
            val result = ignore_errors(p)
            if (result && pos > max_pos) {
                max_pos  = pos
                max_type = type
            }
            result
        })

        generators.add(generator)
        return TokenParser(intArrayOf(type))
    }

    // ---------------------------------------------------------------------------------------------

    inner class TokenParser (val types: IntArray): Parser
    {
        override fun invoke(): Boolean {

            val entry = cache.getOrPut(pos) {
                var out: CacheEntry = CachedFailure
                val pos0 = pos
                val ptr0 = log.size
                if (longest(*parser_array)) {
                    val value = generators[max_type](text.substring(pos0, pos))
                    out = CacheEntry(pos, max_type, value)
                    max_pos  = -1
                    max_type = -1
                    undo(pos0, ptr0)
                }
                out
            }

            if (entry == CachedFailure || !types.contains(entry.type)) {
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
        val types = parsers.fold(intArrayOf()) { a, b -> a + b.types }
        return TokenParser(types)
    }

    // ---------------------------------------------------------------------------------------------
}

