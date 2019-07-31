package norswap.autumn.parsers;

import norswap.autumn.Parse;
import norswap.autumn.Parser;
import norswap.autumn.ParserVisitor;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * This parse matches its {@link #coarse} parser, then tries to match its {@link #fine} parser on
 * the input span matched by the {@code coarse} parser (achieved by resetting {@link Parse#pos} and
 * manipulating {@link Parse#end_of_input}). If the {@code coarse} parser fails then the bounded
 * parser fails. If the {@code fine} parser fails to match the whole input span matched by {@code
 * coarse}, then the parse is reset to its state after matching {@code coarse} and the {@link
 * #fallback} predicate is run. Its return value determines the success of the parser.
 *
 * <p>Note that {@link #coarse} doesn't have it's {@link Parser#exclude_errors} flag set, so errors
 * encountered during its invocation will count towards the furthest error. The changes it makes to
 * the context are also <b>not</b> undone before calling {@link #fine}.
 */
public final class Bounded extends Parser
{
    // ---------------------------------------------------------------------------------------------

    public final Parser coarse;

    // ---------------------------------------------------------------------------------------------

    public final Parser fine;

    // ---------------------------------------------------------------------------------------------

    public final Predicate<Parse> fallback;

    // ---------------------------------------------------------------------------------------------

    public Bounded (Parser coarse, Parser fine, Predicate<Parse> fallback)
    {
        this.coarse = coarse;
        this.fine = fine;
        this.fallback = fallback;
    }

    // ---------------------------------------------------------------------------------------------

    @Override protected boolean doparse (Parse parse)
    {
        int pos0 = parse.pos;

        if (!coarse.parse(parse))
            return false;

        int log0 = parse.log.size();
        int end0 = parse.end_of_input;
        parse.end_of_input = parse.pos;
        parse.pos = pos0;

        boolean success = fine.parse(parse);

        if (success && parse.pos == parse.end_of_input) {
            parse.end_of_input = end0;
            return true;
        }

        parse.pos = parse.end_of_input;
        parse.end_of_input = end0;
        if (success) parse.log.rollback(log0);

        return fallback.test(parse);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void accept (ParserVisitor visitor) {
        visitor.visit(this);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public List<Parser> children () {
        return Collections.unmodifiableList(Arrays.asList(coarse, fine));
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toStringFull () {
        return "bounded(" + coarse + ", " + fine + ")";
    }

    // ---------------------------------------------------------------------------------------------
}
