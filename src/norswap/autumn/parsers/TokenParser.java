package norswap.autumn.parsers;

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

    public final Tokens tokens;

    // ---------------------------------------------------------------------------------------------

    /**
     * Target token parser, must be held within {@link #tokens}.
     */
    public final Parser target;

    // ---------------------------------------------------------------------------------------------

    /**
     * Create a new token parser for the target base parser.
     *
     * <p>You shouldn't normally use this, rely on {@link norswap.autumn.DSL.rule#token} or {@link
     * Tokens#tokenParser} if you can.
     */
    public TokenParser (Tokens tokens, Parser target)
    {
        this.tokens = tokens;
        this.target = target;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void setRule (String name)
    {
        target.setRule(name + "(token target)");
        super.setRule(name);
    }

    // ---------------------------------------------------------------------------------------------

    @Override protected boolean doparse (Parse parse) {
        return tokens.parseToken(parse, target);
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
