package norswap.autumn.parsers;

import norswap.autumn.Parse;
import norswap.autumn.Parser;
import norswap.autumn.ParserVisitor;
import java.util.Collections;
import java.util.function.Predicate;

/**
 * This is an abstract base class for quickly implementing parsers that do not have any sub-parsers.
 *
 * <p>The idea is that you supply the parser implementation as an instance of {@link
 * Predicate<Parse>} (which is just the same as if you overrode the {@link Parser#doparse(Parse)}
 * method, except syntitatically a bit shorter).
 *
 * <p>What this class really buys you, however, is that you can often avoid to adapt
 * {@link ParserVisitor} implementations for its subclasses. When you don't override
 * {@link #accept(ParserVisitor)}, the parser will be visited as a {@link PredicateParser}
 * and visitors are able to make useful default assumptions for this class of parsers (namely that
 * they don't have any sub-parsers — as for nullability, we ask to user to specify it explicitly
 * via {@link #nullable}).
 */
public abstract class PredicateParser extends Parser
{
    // ---------------------------------------------------------------------------------------------

    /**
     * The display name for this parser, if {@link #set_rule(String)} hasn't been called.
     */
    public final String name;

    // ---------------------------------------------------------------------------------------------

    /**
     * Whether this parser is "nullable" (i.e. can succeed while matching no input).
     */
    public final boolean nullable;

    // ---------------------------------------------------------------------------------------------

    /**
     * The predicate function used to implement this parser. Follows the same specification as
     * {@link Parser#doparse(Parse)}, but should not call to other parsers!
     */
    public final Predicate<Parse> predicate;

    // ---------------------------------------------------------------------------------------------

    public PredicateParser (String name, boolean nullable, Predicate<Parse> predicate)
    {
        this.name = name;
        this.nullable = nullable;
        this.predicate = predicate;
    }

    // ---------------------------------------------------------------------------------------------

    @Override final protected boolean doparse (Parse parse) {
        return predicate.test(parse);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void accept (ParserVisitor visitor) {
        visitor.visit(this);
    }

    // ---------------------------------------------------------------------------------------------

    @Override final public Iterable<Parser> children () {
        return Collections.emptyList();
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toStringFull () {
        return name;
    }

    // ---------------------------------------------------------------------------------------------
}
