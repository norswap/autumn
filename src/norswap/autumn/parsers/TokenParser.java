package norswap.autumn.parsers;

import norswap.autumn.DSL;
import norswap.autumn.Parse;
import norswap.autumn.Parser;
import norswap.autumn.ParserVisitor;
import java.util.List;

/**
 * Parses one of the tokens from an associated {@link Tokens} instance.
 *
 * <p>Beware that instances of {@code TokenParser} report as {@link #children()} the underlying
 * parsers for <b>all</b> token types. This makes sense, as the {@code TokenParser} may cause the
 * underlying parsers for all token types to be run — but it might violate some of your assumptions.
 */
public final class TokenParser extends Parser
{
    // ---------------------------------------------------------------------------------------------

    private final Tokens tokens;

    /**
     * Target token parser, must be held within {@link #tokens}.
     */
    public final Parser target;

    // ---------------------------------------------------------------------------------------------

    /**
     * Create a new token parser for the target base parser.
     *
     * <p>You shouldn't normally use this, rely on {@link DSL.rule#token} or {@link
     * Tokens#token_parser} if you can.
     */
    public TokenParser (Tokens tokens, Parser target)
    {
        this.tokens = tokens;
        this.target = target;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void set_rule (String name)
    {
        if (tokens.parsers == null)
            throw new Error(
                "set_rule called on a TokenParser before the token set has been built.\n" +
                "Did you forget to call DSL#build_tokenizer() beforehand?");
        target.set_rule(name + "(token target)");
        super.set_rule(name);
    }

    // ---------------------------------------------------------------------------------------------

    @Override protected boolean doparse (Parse parse) {
        return tokens.parse_token(parse, target);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void accept (ParserVisitor visitor) {
        visitor.visit(this);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public List<Parser> children() {
        return tokens.parsers();
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toStringFull() {
        return "token(" + target + ")";
    }

    // ---------------------------------------------------------------------------------------------
}
