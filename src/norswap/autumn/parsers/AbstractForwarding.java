package norswap.autumn.parsers;

import norswap.autumn.Parse;
import norswap.autumn.ParseOptions;
import norswap.autumn.Parser;
import norswap.autumn.ParserVisitor;
import norswap.autumn.ParserWalker;
import java.util.Collections;

/**
 * This is an abstract base class for easier implementation of parsers that forward their {@link
 * #parse} method to that of another parser (the "forwardee").
 *
 * <p>The point of a forwarding parser is to be able to signal a specific parsing pattern while
 * being able to leverage existing parsers' implementation (and so not be forced to copy their
 * logic in the new parser's implementation).
 *
 * <p>The new parser class will stand on its own in the sense that it will show up in parser call
 * stacks ({@link ParseOptions#recordCallStack}, will be traversed by a {@link ParserWalker}, and
 * can be specialized on via a {@link ParserVisitor}.
 *
 * <p>Regarding visitors, if you don't override {@link  #accept(ParserVisitor)}, the parser will be
 * visited as an instance of {@link AbstractForwarding}, which often enables useful defaults through
 * reference to the {@link #forwardee}.
 */
public abstract class AbstractForwarding extends Parser
{
    // ---------------------------------------------------------------------------------------------

    /**
     * Name for the sub-class of parser, used in the full string representation of the parser
     * ({@link #toStringFull()}).
     */
    public final String name;

    // ---------------------------------------------------------------------------------------------

    /**
     * The parser to which this parser forwards its execution to.
     */
    public final Parser forwardee;

    // ---------------------------------------------------------------------------------------------

    public AbstractForwarding (String name, Parser forwardee)
    {
        this.name = name;
        this.forwardee = forwardee;
    }

    // ---------------------------------------------------------------------------------------------

    @Override final protected boolean doparse (Parse parse) {
        return forwardee.parse(parse);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void accept (ParserVisitor visitor) {
        visitor.visit(this);
    }

    // ---------------------------------------------------------------------------------------------

    @Override final public Iterable<Parser> children() {
        return Collections.singleton(forwardee);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toStringFull() {
        return name + "(" + forwardee + ")";
    }

    // ---------------------------------------------------------------------------------------------
}
