package norswap.autumn.parsers;

import norswap.autumn.AutumnUtil;
import norswap.autumn.Parse;
import norswap.autumn.Parser;

/**
 * Matches a literal string, within {@code Parse#string}.
 */
public final class StringMatch extends Parser {
    // ---------------------------------------------------------------------------------------------

    public final String string;

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
        this.string = string;
        this.whitespace = whitespace;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public boolean doparse (Parse parse)
    {
        assert parse.string != null;
        int end = Math.min(parse.pos + string.length(), parse.string.length());
        if (string.equals(parse.string.substring(parse.pos, end))) {
            parse.pos += string.length();
            return whitespace == null || whitespace.parse(parse);
        }
        return false;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toStringFull()
    {
        StringBuilder b = new StringBuilder();
        b.append("match(");
        b.append("[").append(AutumnUtil.replace_closing_square_brackets(string)).append("]");
        if (whitespace != null)
            b.append(", ").append(whitespace);
        b.append(")");
        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------
}
