package norswap.autumn.parsers;

import norswap.autumn.Parser;
import norswap.autumn.ParserVisitor;
import java.util.Collections;

/**
 * This is an abstract base class for quickly implementing parsers that do not have any sub-parsers.
 *
 * <p>What this class buys you is that you can often avoid to adapt {@link ParserVisitor}
 * implementations for its subclasses. When you don't override {@link #accept(ParserVisitor)}, the
 * parser will be visited as a {@link AbstractPrimitive} and visitors are able to make useful
 * default assumptions for this class of parsers (namely that they don't have any sub-parsers — as
 * for nullability, we ask to user to specify it explicitly via {@link #nullable}). Other methods
 * also come pre-implemented.
 */
public abstract class AbstractPrimitive extends Parser
{
    // ---------------------------------------------------------------------------------------------

    /**
     * The display name for this parser, if {@link #setRule(String)} hasn't been called.
     */
    public final String name;

    // ---------------------------------------------------------------------------------------------

    /**
     * Whether this parser is "nullable" (i.e. can succeed while matching no input).
     */
    public final boolean nullable;

    // ---------------------------------------------------------------------------------------------

    public AbstractPrimitive (String name, boolean nullable)
    {
        this.name = name;
        this.nullable = nullable;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void accept (ParserVisitor visitor) {
        visitor.visit(this);
    }

    // ---------------------------------------------------------------------------------------------

    @Override final public Iterable<Parser> children() {
        return Collections.emptyList();
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toStringFull() {
        return name;
    }

    // ---------------------------------------------------------------------------------------------
}
