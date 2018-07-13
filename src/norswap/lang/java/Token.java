package norswap.lang.java;

// TODO add way to extract int / floating point value

import java.util.Collections;
import java.util.List;

public final class Token
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

    public Token (TokenKind kind, int start, int end, String string, List<Comment> comments, int radix)
    {
        this.kind = kind;
        this.start = start;
        this.end = end;
        this.string = string;
        this.radix = radix;
        this.comments = comments == null
            ? Collections.emptyList()
            : comments;
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
}
