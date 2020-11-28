package norswap.lang.java;

import java.util.Collections;
import java.util.List;

public final class Token implements norswap.autumn.positions.Token
{
    // ---------------------------------------------------------------------------------------------

    public final TokenKind kind;

    // ---------------------------------------------------------------------------------------------

    /** Start position of the token in the input string. */
    public final int start;

    // ---------------------------------------------------------------------------------------------

    /** End position of the token in the input string. */
    public final int end;

    // ---------------------------------------------------------------------------------------------

    /** String representation of the token. */
    public final String string;

    // ---------------------------------------------------------------------------------------------

    /** Radix for {@link TokenKind.Tag#NUMERIC} tokens, 0 otherwise. */
    public final int radix;

    // ---------------------------------------------------------------------------------------------

    /** A list of comments that precede the token. */
    public final List<Comment> comments;

    // ---------------------------------------------------------------------------------------------

    /**
     * Trailing whitespace indicator, only true for ">" tokens followed by whitespace.
     */
    public final boolean trailing_whitespace;

    // ---------------------------------------------------------------------------------------------

    @Override public int start() {
        return start;
    }

    @Override public int end () {
        return end;
    }

    @Override public int length () {
        return end - start;
    }

    // ---------------------------------------------------------------------------------------------

    public Token (TokenKind kind, int start, int end, String string, List<Comment> comments,
                  int radix, boolean trailing_whitespace)
    {
        this.kind = kind;
        this.start = start;
        this.end = end;
        this.string = string;
        this.radix = radix;
        this.comments = comments == null
            ? Collections.emptyList()
            : comments;
        this.trailing_whitespace = trailing_whitespace;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toString ()
    {
        return "Token[" +
            start +
            ((start == end - 1) ? "" : ("-" + (end - 1))) +
            "]{kind=" + kind +
            ", string='" + string + '\'' +
            ", radix=" + radix +
            (trailing_whitespace ? ", trailing_whitespace" : "") +
            '}';
    }

    // ---------------------------------------------------------------------------------------------

    public enum CommentKind
    {
        LINE,
        BLOCK,
        JAVADOC
    }

    // ---------------------------------------------------------------------------------------------

    public static final class Comment
    {
        public final CommentKind kind;

        public final int start;

        public final int end;

        public final String string;

        public Comment (CommentKind kind, int start, int end, String string)
        {
            this.kind = kind;
            this.string = string;
            this.start = start;
            this.end = end;
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * If this is a numeric token, parses its value and returns it as a long (beware that this will
     * work for floating-point numbers too, inducing the usual conversation).
     *
     * @throws IllegalStateException if this isn't a {@link TokenKind.Tag#NUMERIC} token.
     */
    public long long_value() {
        if (kind.tag != TokenKind.Tag.NUMERIC)
            throw new IllegalStateException("Not a numeric token.");
        return (long) LexUtils.parse_integer(string).get();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * If this is a numeric token, parses its value and returns it as a double (beware that this will
     * work for integers too, inducing the usual conversation).
     *
     * @throws IllegalStateException if this isn't a {@link TokenKind.Tag#NUMERIC} token.
     */
    public double double_value() {
        if (kind.tag != TokenKind.Tag.NUMERIC)
            throw new IllegalStateException("Not a numeric token.");
        return (double) LexUtils.parse_floating(string).get();
    }

    // ---------------------------------------------------------------------------------------------
}
