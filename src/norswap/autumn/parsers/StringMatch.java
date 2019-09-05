package norswap.autumn.parsers;

import norswap.autumn.DSL;
import norswap.autumn.Parse;
import norswap.autumn.Parser;
import norswap.autumn.ParserVisitor;
import java.util.Collections;

import static norswap.autumn.util.ParserStringsUtil.escape_quoted_section;

/**
 * Matches a literal string, within {@code Parse#string}.
 *
 * <p>Build with {@link DSL#str(String)}
 */
public final class StringMatch extends Parser {
    // ---------------------------------------------------------------------------------------------

    public final String string;
    public final int[] codepoints;

    // ---------------------------------------------------------------------------------------------

    public final Parser whitespace;

    // ---------------------------------------------------------------------------------------------

    /**
     * Creates a parser that will match the given string. If {@code whitespace} is non-null, this
     * parser will be used to skip whitespace following the matched string and its return value
     * value will be used as return value for this parser.
     */
    public StringMatch (String string, Parser whitespace)
    {
    	this.codepoints = string.codePoints().toArray();
        this.string = string;
        this.whitespace = whitespace;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public boolean doparse (Parse parse)
    {
        if (!parse.match(parse.pos, codepoints))
            return false;

        parse.pos += codepoints.length;
        return whitespace == null || whitespace.parse(parse);
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

    @Override public String toStringFull()
    {
        StringBuilder b = new StringBuilder();
        b.append("match(");
        b.append("[").append(escape_quoted_section(string)).append("]");
        if (whitespace != null)
            b.append(", ").append(whitespace);
        b.append(")");
        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------
}
