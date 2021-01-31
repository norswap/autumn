package norswap.autumn.parsers;

import norswap.autumn.Grammar;
import norswap.autumn.Parse;
import norswap.autumn.Parser;
import norswap.autumn.ParserVisitor;
import java.util.Collections;

/**
 * A parser that always fails.
 *
 * <p>Build with {@link Grammar#fail}
 */
public final class Fail extends Parser
{
    @Override protected boolean doparse (Parse parse) {
        return false;
    }

    @Override public void accept (ParserVisitor visitor) {
        visitor.visit(this);
    }

    @Override public Iterable<Parser> children() {
        return Collections.emptyList();
    }

    @Override public String toStringFull() {
        return "fail";
    }
}
