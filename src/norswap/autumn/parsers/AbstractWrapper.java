package norswap.autumn.parsers;

import norswap.autumn.Parser;
import norswap.autumn.ParserVisitor;
import java.util.Collections;
import java.util.List;

/**
 * This is an abstract base class for easier implementation of parsers that wrap a singular child
 * parser <b>and can only match the same thing as this child</b> (if parsing is entirely delegated
 * to the child, you should extend a {@link AbstractForwarding} instead). The parser may however
 * incur supplemental restriction, such as context-sensitive ones.
 *
 * <p>What this class buys you is that you can often avoid to adapt {@link ParserVisitor}
 * implementations for its subclasses. When you don't override {@link #accept(ParserVisitor)}, the
 * parser will be visited as an {@link AbstractWrapper} and visitors are able to make useful default
 * assumptions for this class of parsers (namely that they can match the same thing as any of their
 * sub-parsers). Other methods also come pre-implemented.
 */
public abstract class AbstractWrapper extends Parser
{
    // ---------------------------------------------------------------------------------------------

    /**
     * Name for the sub-class of parser, used in the full string representation of the parser
     * ({@link #toStringFull()}).
     */
    public final String name;

    // ---------------------------------------------------------------------------------------------

    public final Parser child;

    // ---------------------------------------------------------------------------------------------

    @Override public Iterable<Parser> children() {
        return Collections.singleton(child);
    }

    // ---------------------------------------------------------------------------------------------

    public AbstractWrapper (String name, Parser child)
    {
        this.name = name;
        this.child = child;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void accept (ParserVisitor visitor) {
        visitor.visit(this);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toStringFull() {
        return name + "(" + child + ")";
    }

    // ---------------------------------------------------------------------------------------------
}
