package norswap.autumn.parsers;

import norswap.autumn.DSL;
import norswap.autumn.Parse;
import norswap.autumn.ParseState;
import norswap.autumn.Parser;
import norswap.autumn.SideEffect;
import norswap.autumn.memo.MemoEntry;
import norswap.autumn.memo.Memoizer;
import norswap.utils.NArrays;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * This is a factory to generate {@link TokenParser}s and {@link TokenChoice}s.
 *
 * <p>The point is to create a set of parsers ({@link TokenParser}) that are (a) mutually exclusive
 * (only one can succeed at any given input position) and (b) parsed efficiently by maintaining a
 * map of input position to results. The idea being that many parsers in the set will be called at
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
 * determined via longest-match (as with the {@link Longest} parser). If multiple parsers can parse
 * the same amount of input, then the parser that was added to the {@link Tokens} instance earlier
 * will be preferred (typically, the one declared first using {@link
 * norswap.autumn.DSL.rule#token}).
 *
 * <p>You can also use {@link #token_choice(Parser...)} to obtain an optimized choice between token
 * parsers that have been previously defined.
 *
 * <p>This class maintains a {@link Memoizer} (as a parse state: {@link #memo_state}) to map input
 * positions to result (including the matching parser, if any, the end position of the match and its
 * side effects). Token parsers call back into the {@link Tokens} instance in order to find if the
 * token at the current position is the one they are supposed to recognize. If the token at the
 * current position is yet unknown, it is determined and the table is filled.
 */
@SuppressWarnings("unchecked")
public final class Tokens
{
    // ---------------------------------------------------------------------------------------------

    /**
     * The memoizer as a parse state. Note that this state is not affected by backtracking.
     */
    public final ParseState<Memoizer> memo_state;

    // ---------------------------------------------------------------------------------------------

    /** The array of base parsers used to parse tokens. */
    Parser[] parsers = new Parser[8];

    // ---------------------------------------------------------------------------------------------

    private int size = 0;

    // ---------------------------------------------------------------------------------------------

    public Tokens (Supplier<Memoizer> memo) {
        this.memo_state = new ParseState<>(Tokens.class, memo);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns an unmodifiable list of the parsers used to parse tokens.
     */
    public List<Parser> parsers() {
        return Collections.unmodifiableList(Arrays.asList(NArrays.packed(parsers)));
    }

    // ---------------------------------------------------------------------------------------------

    private void add (Parser parser)
    {
        parser.exclude_errors = true;

        if (size == parsers.length) {
            // grow by smallest multiple of 8 that is <= 1/4 of the length
            int quarter = parsers.length / 4;
            int complement = quarter % 8 == 0 ? 0 : 8 - quarter % 8;
            parsers = Arrays.copyOf(parsers, parsers.length + quarter + complement);
        }

        parsers[size++] = parser;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link TokenParser} wrapping the given base parser. Does not write a duplicate the
     * base parser if it already exists, and otherwise adds it as a new base parser, whose
     * {@link Parser#exclude_errors} flag will be set to true.
     */
    public TokenParser token_parser (Parser base_parser)
    {
        if (NArrays.contains(parsers, base_parser))
            return new TokenParser(this, base_parser);

        add(base_parser);
        return new TokenParser(this, base_parser);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link TokenChoice} wrapping the given parsers, which can be either instances of
     * {@link TokenParser} previously acquired via this object, or a base parser underlying one of
     * these instances.
     */
    public TokenChoice token_choice (Parser... base_parsers)
    {
        Parser[] parsers1 = NArrays.map(base_parsers, new Parser[0], it ->
        {
            if (it instanceof TokenParser)
                it = ((TokenParser) it).target;

            if (NArrays.contains(parsers, it))
                return it;

            throw new Error("Parser " + it
                + " is not a recognized token parser or base token parser.");
        });

        return new TokenChoice(this, parsers1);
    }

    // ---------------------------------------------------------------------------------------------


    /**
     * Tries to parse the token corresponding to the given target parser, returning true iff
     * successful.
     *
     * <p>In all cases, fills the cache with the tokenization result for the current position.
     */
    boolean parse_token (Parse parse, Parser target)
    {
        Memoizer memo = memo_state.data(parse);
        MemoEntry e = memo.get(null, parse.pos, null);

        if (e == null) // token for position not in table yet
            e = fill_cache(memo, parse);

        if (!e.succeeded() || e.parser != target) // no token or wrong token
            return false;

        // correct token!
        parse.pos = e.end_position;
        parse.log.apply(e.delta);
        return true;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Tries to parse one of the token corresponding to the given target parsers, returning true iff
     * successful.
     *
     * <p>In all cases, fills the cache with the tokenization result for the current position.
     */
    boolean parse_token_choice (Parse parse, Parser[] targets)
    {
        Memoizer memo = memo_state.data(parse);
        MemoEntry e = memo.get(null, parse.pos, null);

        if (e == null) // token for position not in table yet
            e = fill_cache(memo, parse);

        if (!e.succeeded()) // no token
            return false;

        for (Parser target: targets)
            if (e.parser == target) { // a correct token
                parse.pos = e.end_position;
                parse.log.apply(e.delta);
                return true;
            }

        return false;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Fills the cache with the result for the current position, and return the inserted result.
     *
     * <p>Assumes no entry for that position exist yet.
     */
    private MemoEntry fill_cache (Memoizer memo, Parse parse)
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

        boolean success = delta != null;
        MemoEntry entry = new MemoEntry(
            success, success ? parsers[longest] : null, pos0, max_pos, delta, null);

        memo.memoize(entry);
        return entry;
    }

    // ---------------------------------------------------------------------------------------------
}
