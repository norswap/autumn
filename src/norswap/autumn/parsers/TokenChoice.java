package norswap.autumn.parsers;

import norswap.autumn.DSL;
import norswap.autumn.Parse;
import norswap.autumn.Parser;
import norswap.autumn.ParserVisitor;
import java.util.Arrays;
import java.util.Collections;

import static norswap.utils.Vanilla.pop;

/**
 * Parses one token out a set of target tokens from an associated {@link Tokens} instance.
 *
 * <p>This is a more efficient version of putting multiple {@link TokenParser} within a {@link
 * Choice} parser, but the result is semantically equivalent.
 */
public final class TokenChoice extends Parser
{
    // ---------------------------------------------------------------------------------------------

    private final Tokens tokens;

    /** Target token types, must be within {@link #tokens}. */
    private final Parser[] targets;

    // ---------------------------------------------------------------------------------------------

    /**
     * Create a new token choice parser for the target base parsers.
     *
     * <p>You shouldn't normally use this, rely on {@link DSL#token_choice} or {@link
     * Tokens#token_choice} if you can.
     */
    public TokenChoice (Tokens tokens, Parser[] targets)
    {
        this.tokens = tokens;
        this.targets = targets;
    }

    // ---------------------------------------------------------------------------------------------

    @Override protected boolean doparse (Parse parse) {
        return tokens.parse_token_choice(parse, targets);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void accept (ParserVisitor visitor) {
        visitor.visit(this);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public Iterable<Parser> children() {
        return Collections.unmodifiableList(Arrays.asList(targets));
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toStringFull()
    {
        StringBuilder b = new StringBuilder();
        b.append("token_choice(");
        for (Parser child: targets)
            b.append(child).append(", ");
        if (targets.length > 0)
            pop(b, 2);
        b.append(")");
        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------
}
