package norswap.autumn.parsers;

import norswap.autumn.DSL;
import norswap.autumn.Parse;
import norswap.autumn.Parser;
import norswap.autumn.ParserVisitor;
import java.util.Collections;

/**
 * Parses one of the tokens from an associated {@link Tokens} instance.
 */
public final class TokenParser extends Parser
{
    // ---------------------------------------------------------------------------------------------

    private final Tokens tokens;

    /**
     * Index of the parser for the target token type within {@link #tokens}.
     *
     * <p>Insignificant to the user, only public for the sake of {@link DSL#token_choice(Object...)}.
     */
    public final int target_index;

    // ---------------------------------------------------------------------------------------------

    /**
     * Create a new token parser for the given {@code target} token of the given token set.
     *
     * <p>You shouldn't normally use this, rely on {@link DSL.Wrapper#token} or {@link
     * Tokens#token_parser} if you can.
     */
    public TokenParser (Tokens tokens, int target)
    {
        this.tokens = tokens;
        this.target_index = target;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the underlying parser targetted by this token parser.
     */
    public Parser target() {
        return tokens.parsers[target_index];
    }

    // ---------------------------------------------------------------------------------------------

    @Override protected boolean doparse (Parse parse) {
        return tokens.parse_token(parse, target_index);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void accept (ParserVisitor visitor) {
        visitor.visit(this);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public Iterable<Parser> children() {
        return Collections.singletonList(target());
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toStringFull() {
        return "token::" + target().toStringFull();
    }

    // ---------------------------------------------------------------------------------------------
}
