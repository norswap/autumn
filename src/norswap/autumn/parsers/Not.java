package norswap.autumn.parsers;

import norswap.autumn.Parse;
import norswap.autumn.Parser;
import norswap.autumn.ParserCallStack;
import norswap.autumn.ParserVisitor;
import java.util.Collections;

/**
 * Succeeds only if its child fails. This never consumes any input ({@link Parse#pos} remains
 * unchanged). Similarly, no other side-effects are applied, no matter if this parser succeeds or
 * fails.
 *
 * <p>Build with {@link norswap.autumn.Grammar.rule#not()}
 */
public final class Not extends Parser
{
    // ---------------------------------------------------------------------------------------------

    public final Parser child;

    // ---------------------------------------------------------------------------------------------

    public Not (Parser child)
    {
        this.child = child;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public boolean doparse (Parse parse)
    {
        int err0 = parse.error;
        String errmsg0 = parse.errorMessage();
        ParserCallStack stk0 = parse.errorCallStack;
        // if the child matches, #parse will undo its side effects
        boolean success = !child.parse(parse);
        // negated parsers should not count towards the furthest error
        parse.error = err0;
        //noinspection StringEquality
        if (parse.errorMessage() != errmsg0)
            parse.setErrorMessage(errmsg0);
        parse.errorCallStack = stk0;
        return success;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void accept (ParserVisitor visitor) {
        visitor.visit(this);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public Iterable<Parser> children() {
        return Collections.singletonList(child);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toStringFull() {
        return "not(" + child + ")";
    }

    // ---------------------------------------------------------------------------------------------
}
