package norswap.lang.java;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A self-contained lexer for Java8, adapted from the Javac lexer.
 *
 * <p>IMPORTANT NOTE: This is only used for {@link JavaGrammarTokens}. The other two Java grammars
 * ({@link JavaGrammar} and {@link JavaGrammar}) do not need this lexer!
 *
 * <p>WARNING: Not tested quite extensively enough.
 *
 * <p>https://github.com/dmlloyd/openjdk/blob/jdk8u/jdk8u/langtools/src/share/classes/com/sun/tools/javac/parser/JavaTokenizer.java
 *
 * Retrieve tokens one by one through {@link #next()} or all at once through {@link #lex()}.
 *
 * Errors are handled in two ways. For lexical errors where the intent is clear, such as
 * underscore in illegal locations, the error is reported as warning in {@link #warnings}.
 * For other errors, the confusing sequence characters is added to a {@link TokenKind#ERROR} token,
 * which is then emitted. This way, it is still possible to lex the whole input even in the
 * presence of errors.
 *
 * Difference with JDK lexer:
 *
 * <ul>
 * <li>The JDK lexer allows non-ascii digits in hex literals, emitting a warning.</li>
 * <li>The JDK lexer performs unicode escape translation everywhere, not just in string and
 * character literals.
 * (see <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-3.html#jls-3.2">
 * the relevant JLS section</a>)</li>
 * </ul>
 *
 * This lexer could be improved in a few ways:
 *
 * <ul>
 * <li>Perform unicode escape translation globally.</li>
 * <li>Support for progressive streams of text, not just whole strings.</li>
 * <li>Support multiple Java versions through a version flag, not just Java 8.</li>
 * <li>Generate better errors for invalid numbers/literals. e.g. the invalid octal escape '\777'
 *     currently generates "unclosed char literal".</li>
 * </ul>
 */
public final class Lexer
{
    // ---------------------------------------------------------------------------------------------

    /** Line feed character. */
    private final static byte LF = 0xA;

    /** Form feed character. */
    private final static byte FF = 0xC;

    /** Carriage return character. */
    final static byte CR = 0xD;

    /** End of input character. */
    final static byte EOI = 0x1A;

    // ---------------------------------------------------------------------------------------------

    /**
     * Input string to be tokenized.
     */
    public final int[] string;

    // ---------------------------------------------------------------------------------------------

    /**
     * A list of warnings generated while emitting tokens. The lexer only ever appends to this list.
     */
    public final List<Warning> warnings = new ArrayList<>();

    // ---------------------------------------------------------------------------------------------

    /**
     * Whether to accept characters made out of two UTF-16 surrogate characters inside identifiers.
     */
    public boolean support_surrogate_pairs = true;

    // ---------------------------------------------------------------------------------------------

    public Lexer (String string)
    {
    	this.string = string.codePoints().toArray();
    }

    // ---------------------------------------------------------------------------------------------

    /** Input position. */
    private int i;

    /**
     * Buffer used to read in data whenever characters can be skipped.
     *
     * <p>For identifiers this includes for instance some control characters.
     *
     * <p>For numbers, this includes everything that would preclude the literal from being parsed
     * by {@link Integer#parseInt} & co, so things like underscores and some suffixes.
     */
    private int[] buf = new int[128];

    /** Buffer Pointer */
    private int bp = 0;

    /** Position of the first character of {@link #buf} in the input. */
    private int start;

    /** List of comments for the next emitted token. */
    private List<Token.Comment> comments;

    // ---------------------------------------------------------------------------------------------

    /**
     * Appends {@code c} to {@link #buf}.
     */
    private void put_char (int c)
    {
        if (bp == buf.length)
            buf = Arrays.copyOf(buf, (int) (buf.length * 1.5 + 1));

        buf[bp++] = c;
    }

    private void put_char_and_advance (int c)
    {
        if (bp == buf.length)
            buf = Arrays.copyOf(buf, (int) (buf.length * 1.5 + 1));

        buf[bp++] = c;
        ++i;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Emits a token, adding in the accumulated comments, then reset the comment list.
     */
    private Token token (TokenKind kind, int start, int end, String string,
                         int radix, boolean trailing_whitespace)
    {
        List<Token.Comment> cs = comments;
        comments = null;
        return new Token(kind, start, end, string, cs, radix, trailing_whitespace);
    }

    // ---------------------------------------------------------------------------------------------

    private Token gt_token (boolean trailing_whitespace)
    {
        int len = bp;
        bp = 0;
        return token(TokenKind.GT, start, i, ">", 0, trailing_whitespace);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Emits a string literal token out of the content of {@link #buf}, then resets the buffer and
     * the comments.
     */
    private Token str_token()
    {
        int len = bp;
        bp = 0;
        String str = new String(buf, 0, len);
        return token(TokenKind.STRINGLITERAL, start, i, str, 0, false);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Emits a {@link TokenKind.Tag#NUMERIC} token out of the content of {@link #buf}, then resets
     * the buffer and the comments.
     */
    private Token num_token (TokenKind kind, int radix)
    {
        int len = bp;
        bp = 0;
        String str = new String(buf, 0, len);
        return token(kind, start, i, str, radix, false);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Emits a {@link TokenKind.Tag#NAMED} token out of the content of {@link #buf}, then resets the
     * buffer and the comments.
     */
    private Token name_token ()
    {
        int len = bp;
        bp = 0;
        String str = new String(buf, 0, len);
        TokenKind kind = TokenKind.lookup(str);
        return (kind == TokenKind.IDENTIFIER)
            ? token(kind, start, i, str, 0, false)
            : token(kind, start, i, kind.name, 0, false);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Emits a token of the given kind, with its name as its string representation.
     * Use only for operators (or operator-like).
     */
    private Token operator (TokenKind kind)
    {
        return token(kind, start, i, kind.name, 0, false);
    }

    // ---------------------------------------------------------------------------------------------

    private void add_comment (Token.CommentKind kind)
    {
        if (comments == null)
            comments = new ArrayList<>();
        comments.add(new Token.Comment(kind, start, i, new String(string, start, i-start)));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Convert a digit from its base (2, 8, 10, or 16) to its value.
     *
     * <p>Unlike the JDK implementation, we straight out forbid non-ascii digits.
     */
    private int digit (int digit, int base)
    {
        if (base <= 10) {
            int x = digit - '0';
            return (0 <= x && x < base) ? x : -1;
        }
        if ('0' <= digit && digit <= '9')
            return digit - '0';
        if ('a' <= digit && digit <= 'f')
            return 10 + digit - 'a';
        if ('A' <= digit && digit <= 'F')
            return 10 + digit - 'A';
        else
            return -1;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Reserved hook to process white space.
     */
    private void process_white_space() { }

    // ---------------------------------------------------------------------------------------------

    /**
     * Reserved hook method to process line terminators.
     */
    private void process_line_terminator() { }

    // ---------------------------------------------------------------------------------------------

    /**
     * Reserved hook method to process comments.
     */
    private void process_comment() { }

    // ---------------------------------------------------------------------------------------------

    private int get_char (int i)
    {
        return i < string.length
            ? string[i]
            : EOI;
    }

    // ---------------------------------------------------------------------------------------------

    private boolean is_whitespace (int c) {
        return c == ' ' || c == LF || c == CR || c == '\t' || c == FF;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Skips past and emits the next token.
     */
    public Token next()
    {
        int c, old;
        while (true) {
            start = i;

            switch (get_char(i))
            {
                // skip whitespace

                case ' ':
                case '\t':
                case FF:
                    do { c = get_char(++i); }
                    while (c == ' ' || c == '\t' || c == FF);
                    process_white_space();
                    break;

                case LF:
                    ++i;
                    process_line_terminator();
                    break;

                case CR:
                    ++i;
                    if (get_char(i) == LF) ++i;
                    process_line_terminator();
                    break;

                // identifiers

                case 'A': case 'B': case 'C': case 'D': case 'E':
                case 'F': case 'G': case 'H': case 'I': case 'J':
                case 'K': case 'L': case 'M': case 'N': case 'O':
                case 'P': case 'Q': case 'R': case 'S': case 'T':
                case 'U': case 'V': case 'W': case 'X': case 'Y':
                case 'Z':
                case 'a': case 'b': case 'c': case 'd': case 'e':
                case 'f': case 'g': case 'h': case 'i': case 'j':
                case 'k': case 'l': case 'm': case 'n': case 'o':
                case 'p': case 'q': case 'r': case 's': case 't':
                case 'u': case 'v': case 'w': case 'x': case 'y':
                case 'z':
                case '$': case '_':
                    return scan_ident();

                // numbers and dots

                case '0':
                    c = get_char(++i);
                    if (c == 'x' || c == 'X') {
                        ++i;
                        return scan_number(16);
                    }
                    else if (c == 'b' || c == 'B') {
                        ++i;
                        return scan_number(2);
                    }
                    else {
                        --i;
                        return scan_number(8);
                    }

                case '1': case '2': case '3': case '4':
                case '5': case '6': case '7': case '8': case '9':
                    return scan_number(10);

                case '.':
                    c = get_char(++i);
                    if ('0' <= c && c <= '9') {
                        --i;
                        return scan_number(10);
                    }
                    else if (c == '.') {
                        c = get_char(++i);
                        if (c == '.') {
                            ++i;
                            return operator(TokenKind.ELLIPSIS);
                        }
                        --i;
                    }
                    return operator(TokenKind.DOT);

                // end of input

                case EOI:
                    return (i >= string.length - 1)
                        ? null
                        : scan_ident();

                // operators

                case ',':
                    ++i; return operator(TokenKind.COMMA);
                case ';':
                    ++i; return operator(TokenKind.SEMI);
                case '(':
                    ++i; return operator(TokenKind.LPAREN);
                case ')':
                    ++i; return operator(TokenKind.RPAREN);
                case '[':
                    ++i; return operator(TokenKind.LBRACKET);
                case ']':
                    ++i; return operator(TokenKind.RBRACKET);
                case '{':
                    ++i; return operator(TokenKind.LBRACE);
                case '}':
                    ++i; return operator(TokenKind.RBRACE);
                case '@':
                    ++i; return operator(TokenKind.MONKEYS_AT);
                case '?':
                    ++i; return operator(TokenKind.QUES);
                case '~':
                    ++i; return operator(TokenKind.TILDE);

                case ':':
                    if (get_char(++i) == ':') {
                        ++i;
                        return operator(TokenKind.COLCOL);
                    }
                    return operator(TokenKind.COLON);

                case '*': case '!': case '^': case '%': case '=':
                    put_char_and_advance(get_char(i));
                    c = get_char(i);
                    if (c == '=')
                        put_char_and_advance(c);
                    return name_token();

                case '<': case '>':
                    old = get_char(i);
                    put_char_and_advance(old);
                    c = get_char(i);
                    if (c == '=') // >=
                        put_char_and_advance(c);
                    else if (old == '>' && is_whitespace(c))
                        return gt_token(true); // >
                    else if (c == old) {
                        c = get_char(i+1);
                        if (c == old && old == '>') {
                            c = get_char(i+2);
                            if (c == '=') { // >>>=
                                put_char_and_advance('>');
                                put_char_and_advance('>');
                                put_char_and_advance(c);
                            }
                            else
                                return gt_token(false); // >>> : only emit first
                        }
                        else if (c == '=') { // <<= or >>=
                            put_char_and_advance(old);
                            put_char_and_advance(c);
                        }
                        else if (old == '<') // <<
                            put_char_and_advance('<');
                        else // >> : only emit first
                            return gt_token(false);
                    }
                    return name_token();

                case '+': case '-': case '&': case '|':
                    old = get_char(i);
                    put_char_and_advance(old);
                    c = get_char(i);
                    if (c == '=' || c == old || old == '-' && c == '>')
                        put_char_and_advance(c);
                    return name_token();

                // comments and slashes

                case '/':
                    c = get_char(++i);
                    if (c == '/') {
                        do { c = get_char(++i); }
                        while (c != CR && c != LF && i < string.length);
                        add_comment(Token.CommentKind.LINE);
                        break;
                    }
                    else if (c == '*') {
                        c = get_char(++i);
                        Token.CommentKind kind = Token.CommentKind.BLOCK;
                        if (c == '*') {
                            c = get_char(i + 1);
                            if (c == '*')
                                kind = Token.CommentKind.JAVADOC;
                        }
                        while (i < string.length) {
                            if (c == '*') {
                                c = get_char(++i);
                                if (c == '/') break;
                            }
                            else
                                c = get_char(++i);
                        }
                        if (c != '/')
                            return error("unclosed comment");
                        ++i;
                        add_comment(kind);
                        break;
                    }
                    else if (c == '=') {
                        ++i;
                        return operator(TokenKind.SLASHEQ);
                    }
                    else
                        return operator(TokenKind.SLASH);

                // char literal

                case '\'':
                    c = get_char(++i);
                    if (c == '\'')
                        return error("empty character literal");
                    if (c == CR || c == LF)
                        return error("illegal end of line in character literal");
                    else {
                        int x = scan_lit_char();
                        if (x == -1)
                            return literal_error();
                        c = get_char(i);
                        if (c == '\'') {
                            ++i;
                            return token(TokenKind.CHARLITERAL, start, i, String.valueOf(Character.toChars(x)), 0, false);
                        } else
                            return error("unclosed char literal");
                    }

                // string literal

                case '\"':
                    int x;
                    c = get_char(++i);
                    while (c != '\"') {
                        x = scan_lit_char();
                        if (x == -1)
                            return literal_error();
                        put_char(x);
                        c = get_char(i);
                        if (c == CR || c == LF)
                            return error("unclosed string literal");
                    }
                    ++i;
                    return str_token();

                // weird identifier parts and illegal characters

                default:
                    c = get_char(i);
                    if (c >= '\u0080') { // not ascii
                        if (Character.isJavaIdentifierStart(c))
                            return scan_ident();
                    }
                    String arg = (32 < c && c < 127) // printable ascii char?
                        ? String.format("%s", c)
                        : String.format("\\u%04x", c);
                    ++i;
                    return error("illegal char: " + arg);
            }
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Skips past and emits an identifier token.
     *
     * <p>Assumes we are positioned over a valid start of identifier character.
     */
    private Token scan_ident()
    {
        do {
            int c = get_char(i);
            switch (c) {
                case 'A': case 'B': case 'C': case 'D': case 'E':
                case 'F': case 'G': case 'H': case 'I': case 'J':
                case 'K': case 'L': case 'M': case 'N': case 'O':
                case 'P': case 'Q': case 'R': case 'S': case 'T':
                case 'U': case 'V': case 'W': case 'X': case 'Y':
                case 'Z':
                case 'a': case 'b': case 'c': case 'd': case 'e':
                case 'f': case 'g': case 'h': case 'i': case 'j':
                case 'k': case 'l': case 'm': case 'n': case 'o':
                case 'p': case 'q': case 'r': case 's': case 't':
                case 'u': case 'v': case 'w': case 'x': case 'y':
                case 'z':
                case '$': case '_':
                case '0': case '1': case '2': case '3': case '4':
                case '5': case '6': case '7': case '8': case '9':
                    put_char_and_advance(c);
                    continue;
                case '\u0000': case '\u0001': case '\u0002': case '\u0003':
                case '\u0004': case '\u0005': case '\u0006': case '\u0007':
                case '\u0008': case '\u000E': case '\u000F': case '\u0010':
                case '\u0011': case '\u0012': case '\u0013': case '\u0014':
                case '\u0015': case '\u0016': case '\u0017':
                case '\u0018': case '\u0019': case '\u001B':
                case '\u007F': // control characters
                    ++i;
                    continue;
                case EOI:
                    if (i >= string.length - 1)
                        return name_token();
                    // otherwise treat as a control character
                    ++i;
                    continue;
                default:
                    if (c < '\u0080') // all ASCII range chars already handled above
                        return name_token();
                    else if (Character.isIdentifierIgnorable(c))
                        ++i;
                    else if (Character.isJavaIdentifierPart(c))
                        put_char_and_advance(c);
                    else
                        return name_token();
            }
        } while (true);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Skips past a number (integral or floating point) literal with the given radix, and emits a
     * token for it.
     *
     * <p>Since floating point literals may start with a 0, a radix of 8 will be treated as
     * effectively as radix of 10. However, the emitted token will have the proper radix field.
     * Octal literals must be checked for invalid digits later.
     *
     * <p>Assumes we are positionned at a position where we expect a number to start, but past
     * the radix indicator (0x or 0b) if present.
     */
    private Token scan_number (int radix)
    {
        int used_radix = radix == 8 ? 10 : radix;
        boolean seen_digit = false;
        int c = get_char(i);

        if (c == '_') {
            skip_illegal_underscores();
            c = get_char(i);
        }

        if (digit(c, used_radix) >= 0) {
            seen_digit = true;
            scan_digits(used_radix);
            c = get_char(i);
        }

        if (used_radix == 10) {
            if (c == '.')
                return scan_fraction_and_suffix();
            if (!seen_digit) // unused branch, future-proofing
                return error("invalid decimal number, expected a decimal digit");
            if (c == 'e' || c == 'E' || c == 'f' || c == 'F' || c == 'd' || c == 'D')
                return scan_fraction_and_suffix();
        }
        else if (used_radix == 16) {
            if (c == '.')
                return scan_hex_fraction_and_suffix(seen_digit);
            if (!seen_digit)
                return error("invalid hex number, expected an hex digit");
            if (c == 'p' || c == 'P')
                return scan_hex_exponent_and_suffix();
        }

        else if (used_radix == 2 && !seen_digit)
            return error("invalid binary number, expected a binary digit");

        if (c == 'l' || c == 'L') {
            ++i;
            return num_token(TokenKind.LONGLITERAL, radix);
        }

        return num_token(TokenKind.INTLITERAL, radix);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Advances the position past any series of underscore. If at least one underscores
     * is skipped, emits a warning.
     */
    private void skip_illegal_underscores()
    {
        int c = get_char(i);
        if (c == '_') {
            warn(i, "illegal underscore");
            while (c == '_')
                c = get_char(++i);
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Skips past a set of valid digits for the specified radix, and adds them to the {@code #buf}.
     * Also skips any intervening or trailing underscore (those are not added to the buffer). If
     * a trailing underscore is present, emits a warning.
     *
     * <p>Assumes we are positioned over a valid digit of the given radix.
     */
    private void scan_digits (int radix)
    {
        int c = get_char(i);
        int last;
        assert digit(c, radix) >= 0;
        do {
            if (c != '_')
                put_char(c);
            last = c;
            c = get_char(++i);
        }
        while (digit(c, radix) >= 0 || c == '_');
        if (last == '_')
            warn(i - 1, "illegal underscore");
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Skips the fractional part and suffix ('d'/'f') of a floating point literal.
     * Emits a token for the whole literal.
     *
     * <p>Assumes we are positioned after a valid decimal number, or over a dot.
     */
    private Token scan_fraction_and_suffix()
    {
        skip_illegal_underscores();
        int c = get_char(i);

        if (c == '.') {
            put_char_and_advance(c);
            c = get_char(i);
        }

        if ('0' <= c && c <= '9') {
            scan_digits(10);
            c = get_char(i);
        }

        if (c == 'e' || c == 'E')
        {
            put_char_and_advance(c);
            skip_illegal_underscores();
            c = get_char(i);
            if (c == '+' || c == '-')
                put_char_and_advance(c);
            skip_illegal_underscores();
            c = get_char(i);
            if (!('0' <= c && c <= '9'))
                return error("malformed floating point literal");
            scan_digits(10);
            c = get_char(i);
        }

        if (c == 'f' || c == 'F') {
            put_char_and_advance(c);
            return num_token(TokenKind.FLOATLITERAL, 10);
        }

        if (c == 'd' || c == 'D')
            put_char_and_advance(c);

        return num_token(TokenKind.DOUBLELITERAL, 10);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Skips past the fractional, exponent and suffix parts of an hexadecimal floating point
     * literal, and emits a token for the whole literal.
     *
     * <p>Assumes we are positioned over a dot, and that what precedes is indeed a valid
     * start for an hexadecimal floating point literal.
     *
     * @param seen_digit indicates whether there were any digits before the dot.
     */
    private Token scan_hex_fraction_and_suffix (boolean seen_digit)
    {
        int c = get_char(i);
        assert c == '.';
        put_char_and_advance(c);
        skip_illegal_underscores();
        c = get_char(i);
        if (digit(c, 16) >= 0) {
            seen_digit = true;
            scan_digits(16);
        }
        return !seen_digit
            ? error("invalid hex number")
            : scan_hex_exponent_and_suffix();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Skips the hex exponent and suffix ('d'/'f') parts of an hex floating point literal.
     * Emits a token for the whole literal.
     *
     * <p>Assumes we are positioned past a valid hex floating point prefix.
     */
    private Token scan_hex_exponent_and_suffix()
    {
        int c = get_char(i);

        if (c != 'p' && c != 'P')
            return error("malformed floating point literal, expected 'p'");

        put_char_and_advance(c);
        skip_illegal_underscores();
        c = get_char(i);

        if (c == '+' || c == '-') {
            put_char_and_advance(c);
            skip_illegal_underscores();
            c = get_char(i);
        }

        if (!('0' <= c && c <= '9'))
            return error("malformed floating point literal, expected a decimal digit");

        scan_digits(10);
        c = get_char(i);

        if (c == 'f' || c == 'F') {
            put_char_and_advance(c);
            return num_token(TokenKind.FLOATLITERAL, 16);
        }
        else if (c == 'd' || c == 'D')
            put_char_and_advance(c);

        return num_token(TokenKind.DOUBLELITERAL, 16);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Scans the next character literal, which may be an escape sequence. Returns the scanned char,
     * or -1 if either we have reached the end of the input while scanning, or an invalid escape is
     * found.
     *
     * <p>Leaves the position right after the last scanned character, or at the end of the input.
     *
     * <p>Note that Java is very permissive, it allows even non-printable characters inside
     * literals.
     */
    private int scan_lit_char ()
    {
        int c = get_char(i);

        if (c != '\\')
            return i < string.length
                ? get_char(i++)
                : -1;

        switch (c = get_char(++i))
        {
            case '0': case '1': case '2': case '3':
            case '4': case '5': case '6': case '7':
                int lead = c;
                int oct = digit(lead, 8);
                c = get_char(++i);
                if ('0' <= c && c <= '7') {
                    oct = oct * 8 + digit(c, 8);
                    c = get_char(++i);
                    if (lead <= '3' && '0' <= c && c <= '7') {
                        oct = oct * 8 + digit(c, 8);
                        ++i;
                    }
                }
                return oct;

            case 'u':
                int hex = 0;
                for (int j = 0; j < 4;) {
                    c = get_char(++i);
                    if (c == 'u') {
                        continue;
                    }
                    if ('0' <= c && c <= '9' || 'a' <= c && c <= 'f' || 'A' <= c && c <= 'F') {
                        hex = hex * 16 + digit(c, 16);
                        ++j;
                    } else {
                        return -1;
                    }
                }
                ++i;
                return hex;

            case 'b':  ++i; return '\b';
            case 't':  ++i; return '\t';
            case 'n':  ++i; return '\n';
            case 'f':  ++i; return '\f';
            case 'r':  ++i; return '\r';
            case '\'': ++i; return '\'';
            case '\"': ++i; return '\"';
            case '\\': ++i; return '\\';
            default:   ++i; return -1;
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Emits the proper error token when {@link #scan_lit_char()} fails.
     */
    private Token literal_error()
    {
        return i == string.length
            ? error("unclosed literal")
            : error("illegal escape in literal");
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Fully converts the input string into a series of token.
     */
    public Token[] lex()
    {
        ArrayList<Token> tokens = new ArrayList<>(string.length / 20);
        Token token;
        while ((token = next()) != null)
            tokens.add(token);
        return tokens.toArray(new Token[0]);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Adds a warning to the list of warnings with the given position.
     */
    private void warn (int pos, String msg)
    {
        warnings.add(new Warning(msg, pos));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns an error token with the given message.
     */
    private Token error (String msg)
    {
        return token(
            TokenKind.ERROR, start, i, msg + " (" + new String(string, start, i-start) + ")", 0, false);
    }

    // ---------------------------------------------------------------------------------------------

    public static final class Warning
    {
        public final String msg;

        public final int position;

        public Warning (String msg, int position) {
            this.msg = msg;
            this.position = position;
        }
    }

    // ---------------------------------------------------------------------------------------------
}