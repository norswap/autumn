package norswap.autumn.parsers;

import norswap.autumn.Parser;
import norswap.autumn.ParserVisitor;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static norswap.utils.Strings.joinArray;

/**
 * This is an abstract base class for easier implementation of parsers that behave like {@link
 * Choice} parser: i.e. they may match the same things as any of their sub-parsers, often depending
 * on some piece of context.
 *
 * <p>What this class buys you is that you can often avoid to adapt {@link ParserVisitor}
 * implementations for its subclasses. When you don't override {@link #accept(ParserVisitor)}, the
 * parser will be visited as an {@link AbstractChoice} and visitors are able to make useful default
 * assumptions for this class of parsers (namely that they can match the same thing as any of their
 * sub-parsers). Other methods also come pre-implemented.
 */
public abstract class AbstractChoice extends Parser
{
    // ---------------------------------------------------------------------------------------------

    /**
     * Name for the sub-class of parser, used in the full string representation of the parser
     * ({@link #toStringFull()}).
     */
    public final String name;

    // ---------------------------------------------------------------------------------------------

    private final Parser[] children;

    // ---------------------------------------------------------------------------------------------

    @Override public List<Parser> children() {
        return Collections.unmodifiableList(Arrays.asList(children));
    }

    // ---------------------------------------------------------------------------------------------

    public AbstractChoice (String name, Parser... children)
    {
        this.name = name;
        this.children = children;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void accept (ParserVisitor visitor) {
        visitor.visit(this);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toStringFull()
    {
        StringBuilder b = new StringBuilder();
        b.append(name).append("(");
        joinArray(b, ", ", children);
        b.append(")");
        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------
}
