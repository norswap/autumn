package norswap.autumn.parsers;

import norswap.autumn.Parse;
import norswap.autumn.ParseState;
import norswap.autumn.Parser;
import norswap.autumn.SideEffect;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This is a factory to generate {@link TokenParser}s and {@link TokenChoice}s.
 *
 * <p>The point is to create a set of parsers ({@link TokenParser}) that are (a) mutually exclusive
 * (only one can succeed at any given input position) and (b) parsed efficiently by maintaining a
 * cache of input position to results. The idea being that many parsers in the set will be called at
 * the same position.
 *
 * <p>This is most often useful for tokenization (lexical analysis) hence the name, but is
 * applicable to any other scenario that fits the same conditions.
 *
 * <p>To obtain the set of mutually exclusive parsers, we start by a set of base parsers which are
 * passed to the constructor of this class.
 *
 * <p>To obtain the (mutually exclusive) token parsers, use the {@link #token_parser(Parser)}
 * method, passing it a base parsers that will be recorded in the {@code Tokens} instance. The base
 * parsers do not need to be mutually exclusive - the correct parser at each input position will be
 * determined via longest-match (as with the {@link Longest} parser).
 *
 * <p>You can also use {@link #token_choice(Parser...)} to obtain an optimized choice between token
 * parsers that have been previously defined.
 *
 * <p>This class maintains a cache (as a parse state: {@link #cache_state}) to map input positions
 * to result (including the matching parser, if any, the end position of the match and its side
 * effects). Token parsers call back into the {@link Tokens} instance in order to find if the token
 * at the current position is the one they are supposed to recognize. If the token at the current
 * position is yet unknown, it is determined and the cache is filled.
 */
@SuppressWarnings("unchecked")
public final class Tokens
{
    // ---------------------------------------------------------------------------------------------

    /**
     * The token cache as a parse state. Note that this state is not affected by backtracking.
     */
    public final ParseState<TokenCache> cache_state
        = new ParseState(Tokens.class, TokenCache::new);

    // ---------------------------------------------------------------------------------------------

    /** The array of base parsers used to parse tokens. */
    Parser[] parsers = new Parser[8];

    // ---------------------------------------------------------------------------------------------

    private int size = 0;

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns an unmodifiable list of the parsers used to parse tokens.
     */
    public List<Parser> parsers()
    {
        return Collections.unmodifiableList(Arrays.asList(parsers));
    }

    // ---------------------------------------------------------------------------------------------

    private int add (Parser parser)
    {
        parser.exclude_error = true;

        if (size == parsers.length) {
            // grow by smallest multiple of 8 that is <= 1/4 of the length
            int quarter = parsers.length / 4;
            int complement = quarter % 8 == 0 ? 0 : 8 - quarter % 8;
            parsers = Arrays.copyOf(parsers, parsers.length + quarter + complement);
        }

        parsers[size] = parser;
        return size++;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link TokenParser} wrapping the given base parser. Does not write a duplicate the
     * base parser if it already exists, and otherwise adds it as a new base parser, whose
     * {@link Parser#exclude_error} flag will be set to true.
     */
    public TokenParser token_parser (Parser base_parser)
    {
        for (int i = 0; i < size; ++i)
            if (parsers[i] == base_parser)
                return new TokenParser(this, i);

        return new TokenParser(this, add(base_parser));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link TokenChoice} wrapping the given parsers, which can be either instances of
     * {@link TokenParser} previously acquired via this object, or a base parser underlying one of
     * these instances.
     */
    public TokenChoice token_choice (Parser... base_parsers)
    {
        int[] targets = new int[base_parsers.length];

        outer: for (int j = 0; j < base_parsers.length; ++j)
        {
            Parser base = base_parsers[j];

            if (base instanceof TokenParser) {
                targets[j] = ((TokenParser) base).target_index;
                continue;
            }

            for (int i = 0; i < size; ++i)
                if (parsers[i] == base) {
                    targets[j] = i;
                    continue outer;
                }

            throw new Error("Parser " + base_parsers[j]
                + " is not a recognized token parser or base token parser.");
        }

        return new TokenChoice(this, targets);
    }

    // ---------------------------------------------------------------------------------------------


    /**
     * Tries to parse the token corresponding to the parser at the given {@code target} index,
     * returning true iff successful.
     *
     * <p>In all cases, fills the cache with the tokenization result for the current position.
     */
    boolean parse_token (Parse parse, int target)
    {
        TokenCache cache = cache_state.data(parse);
        TokenResult res  = cache.get(parse.pos);

        if (res == null) // token for position not in cache yet
            res = fill_cache(cache, parse);

        if (!res.matched() || res.parser != target) // no token or wrong token
            return false;

        // correct token!
        parse.pos = res.end_position;
        parse.log.apply(res.delta);
        return true;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Tries to parse one of the token corresponding to the parsers at the given {@code targets}
     * indices, returning true iff successful.
     *
     * <p>In all cases, fills the cache with the tokenization result for the current position.
     */
    boolean parse_token_choice (Parse parse, int[] targets)
    {
        TokenCache cache = cache_state.data(parse);
        TokenResult res  = cache.get(parse.pos);

        if (res == null) // token for position not in cache yet
            res = fill_cache(cache, parse);

        if (!res.matched()) // no token
            return false;

        for (int target: targets)
            if (res.parser == target) { // a correct token
                parse.pos = res.end_position;
                parse.log.apply(res.delta);
                return true;
            }

        return false;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Fills the cache with the result for the current position, and return the inserted result.
     *
     * <p>Assumes no result for that position exist yet.
     */
    private TokenResult fill_cache (TokenCache cache, Parse parse)
    {
        int pos0 = parse.pos;
        int log0 = parse.log.size();

        int longest = -1;
        int max_pos = pos0;
        List<SideEffect> delta = null;

        for (int i = 0; i < size; ++i)
        {
            boolean success = parsers[i].parse(parse);

            if (success) {
                if (parse.pos > max_pos) {
                    max_pos = parse.pos;
                    delta = parse.log.delta(log0);
                    longest = i;
                }

                parse.pos = pos0;
                parse.log.rollback(log0);
            }
        }

        TokenResult result = delta == null
            ? TokenResult.none(pos0)
            : new TokenResult(longest, pos0, max_pos, delta);

        cache.put(pos0, result);
        return result;
    }

    // ---------------------------------------------------------------------------------------------
}

