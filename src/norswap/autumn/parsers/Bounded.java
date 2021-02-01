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
 * manipulating {@link Parse#endOfInput}). If the {@code coarse} parser fails then the bounded
 * parser fails. If the {@code fine} parser fails to match the whole input span matched by {@code
 * coarse}, then the {@link #fallback} predicate is run. If it returns true, the parser is reset
 * to its state after matching {@code coarse}, and the parser succeeds. If it returns false, the
 * parser fails as though {@code coarse} didn't succeed.
 *
 * <p>Note that {@link #coarse} is not rolled back after succeeded, so any change it makes to the
 * parse state are preserved, in particular any items it pushes onto the value stack.
 *
 * <p>Similarly, {@link #coarse} doesn't have it's {@link Parser#excludeErrors} flag modified by
 * this parser, so unless you've set it yourself, errors ncountered during its invocation will count
 * towards the furthest error. The changes it makes to the context are also <b>not</b> undone before
 * calling {@link #fine}.
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
        int log0 = parse.log.size();

        if (!coarse.parse(parse))
            return false;

        int log1 = parse.log.size();
        int end0 = parse.endOfInput;
        int end1 = parse.pos;
        parse.endOfInput = end1;
        parse.pos = pos0;

        boolean success = fine.parse(parse);
        parse.endOfInput = end0;

        if (success && parse.pos == end1)
            return true;

        if (fallback.test(parse)) {
            parse.pos = end1;
            if (success) parse.log.rollback(log1);
            return true;
        }

        parse.pos = pos0;
        parse.log.rollback(log0);
        return false;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void accept (ParserVisitor visitor) {
        visitor.visit(this);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public List<Parser> children() {
        return Collections.unmodifiableList(Arrays.asList(coarse, fine));
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toStringFull() {
        return String.format("bounded(%s, %s)", coarse, fine);
    }

    // ---------------------------------------------------------------------------------------------
}
