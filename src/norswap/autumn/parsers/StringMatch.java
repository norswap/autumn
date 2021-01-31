package norswap.autumn.parsers;

import norswap.autumn.Grammar;
import norswap.autumn.Parse;
import norswap.autumn.Parser;
import norswap.autumn.ParserVisitor;
import norswap.autumn.positions.Span;
import java.util.Collections;

import static norswap.autumn.util.ParserStringsUtil.escapeQuotedSection;

/**
 * Matches a literal string, within {@code Parse#string}, optionally (does not need to succeed)
 * followed by an optional (does not need to be supplied) whitespace parser.
 *
 * <p>Also sets whitespace-related fields in {@link Parse}, which are used to set
 * whitespace information in {@link Span}.
 *
 * <p>Build with {@link Grammar#str(String)}, {@link Grammar#word(String)}, or the built-in conversion
 * from strings to {@code StringMatch} available for many methods in {@link Grammar}.
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
     * parser will be used to skip whitespace following the matched string.
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

        if (whitespace == null)
            return true;

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

    @Override public Iterable<Parser> children() {
        return whitespace == null
            ? Collections.emptyList()
            : Collections.singletonList(whitespace);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toStringFull() {
        return String.format("match([%s]%s)", escapeQuotedSection(string),
            whitespace == null ? "" : (", " + whitespace));
    }

    // ---------------------------------------------------------------------------------------------
}
