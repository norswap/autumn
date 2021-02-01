package norswap.autumn.parsers;

import norswap.autumn.Grammar;
import norswap.autumn.Parse;
import norswap.autumn.ParseState;
import norswap.autumn.Parser;
import norswap.autumn.ParserVisitor;
import norswap.autumn.visitors.WellFormednessChecker;
import java.util.Collections;
import java.util.function.Predicate;

/**
 * A parser that runs a predicate against a {@link Parse} in order to determine whether
 * it succeeds. Typically, the aim is to make a context-sensitive decision by querying some
 * {@link ParseState}.
 *
 * <p>In principle, the predicate is allowed to modify the {@link Parse} object it receives.
 * However beware that Autumn will always consider that a predicate can succeed without consuming
 * any input in its well-formedness check (cf. {@link WellFormednessChecker}).
 *
 * <b>Build with {@link Grammar#context(Predicate)} and name with {@link
 * norswap.autumn.Grammar.rule#named(String)}.
 */
public final class ContextPredicate extends Parser
{
    // ---------------------------------------------------------------------------------------------

    /**
     * The display name for this parser, if {@link #setRule(String)} hasn't been called.
     */
    public String name;

    // ---------------------------------------------------------------------------------------------

    public final Predicate<Parse> predicate;

    // ---------------------------------------------------------------------------------------------

    /**
     * See {@link ContextPredicate}. {@code name} will be used as the display name of this parser
     * if it isn't assigned to a rule.
     */
    public ContextPredicate (String name, Predicate<Parse> predicate)
    {
        this.name = name;
        this.predicate = predicate;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public boolean doparse (Parse parse) {
        return predicate.test(parse);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void accept (ParserVisitor visitor) {
        visitor.visit(this);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public Iterable<Parser> children() {
        return Collections.emptyList();
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toStringFull() {
        return name;
    }

    // ---------------------------------------------------------------------------------------------
}
