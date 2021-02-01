package norswap.autumn.parsers;

import norswap.autumn.Parse;
import norswap.autumn.Parser;
import norswap.autumn.ParserVisitor;
import norswap.autumn.positions.Span;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Matches its child parser and then optionally matches a whitespace parser.
 *
 * <p>Also sets whitespace-related fields in {@link Parse}, which are used to set
 * whitespace information in {@link Span}.
 *
 * <p>If you only need to match a literal string, use {@link StringMatch} parser instead.
 *
 * <p>Build with {@link norswap.autumn.Grammar.rule#word()}.
 */
public final class TrailingWhitespace extends Parser
{
    // ---------------------------------------------------------------------------------------------

    public final Parser child;
    public final Parser whitespace;

    // ---------------------------------------------------------------------------------------------

    @Override public List<Parser> children() {
        return Collections.unmodifiableList(Arrays.asList(child, whitespace));
    }

    // ---------------------------------------------------------------------------------------------

    public TrailingWhitespace (Parser child, Parser whitespace) {
        this.child = child;
        this.whitespace = whitespace;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public boolean doparse (Parse parse)
    {
        if (!child.parse(parse))
            return false;

        int pos0 = parse.pos;
        if (whitespace.parse(parse))
            parse.setWhitespaceFrom(pos0);

        return true;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void accept (ParserVisitor visitor) {
        visitor.visit(this);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toStringFull() {
        return String.format("trailing_whitespace(%s, %s)", child, whitespace);
    }

    // ---------------------------------------------------------------------------------------------
}
