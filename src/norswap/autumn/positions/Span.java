package norswap.autumn.positions;

import norswap.autumn.Grammar;
import norswap.autumn.ParseOptions;
import norswap.autumn.UnicodeCharSequence;
import norswap.autumn.parsers.StringMatch;
import norswap.autumn.parsers.TrailingWhitespace;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

/**
 * Represents a "span" (aka "chunk", "segment", "section") of some parsing input sequence.
 *
 * <p>The span is bounded by its {@link #start} (inclusive) and {@link #end} (exlusive) fields,
 * which are offsets into the sequence.
 *
 * <p>Additionally, the span can also have associated leading and trailing whitespace <b>outside</b>
 * of the {@code start-end} range, represented by the {@link #whitespaceStart} and {@link
 * #whitespaceEnd} fields (see field javadoc for technicalities).
 *
 * <h2>Details on Whitespace Handling</h2>
 *
 * <p>The usual whitespace model in Autumn associates trailing whitespace with grammar rules
 * (usually on the lexical (token) level), so that matching the rule also matches any trailing
 * whitespace. This is achieved through the use of the {@link TrailingWhitespace} and {@link
 * StringMatch} parsers (refer to their respective Javadoc to learn how they can be build using
 * {@link Grammar}).
 *
 * <p>Without any special processing, this would lead to spans spanning the matched rule + any
 * trailing whitespace, without any way to distinguish "real content" from whitespace, and without
 * any information about leading whitespace. When {@link ParseOptions#trackWhitespace} is disabled,
 * this is exactly what happens.
 *
 * <p>However, when the option is enabled (the default), the parsers cited above do track the
 * whitespace, allowing us to build spans that differentiate "real content" from trailing whitespace,
 * and that can reference leading whitespace.
 */
public final class Span
{
    // ---------------------------------------------------------------------------------------------

    /**
     * Inclusive start offset.
     */
    public final int start;

    /**
     * Exclusive end offset.
     */
    public final int end;

    /**
     * Start of the section of whitespace preceding the span, if any and if the information was
     * provided. Always {@code <= start}, and {@code == start} when there is no leading whitespace
     * or the information is not provided.
     */
    public final int whitespaceStart;

    /**
     * End of the section of whitespace following the span, if any and if the information was
     * provided. Always {@code >= end}, and {@code == end} when there is no trailing whitespace or
     * the information is not provided.
     */
    public final int whitespaceEnd;

    // ---------------------------------------------------------------------------------------------

    /**
     * Build a span without providing whitespace information.
     */
    public Span (int start, int end) {
        this(start, end, start, end);
    }

    // ---------------------------------------------------------------------------------------------

    public Span (int start, int end, int whitespaceStart, int whitespaceEnd) {
        if (start < 0)
            throw new IllegalArgumentException("Negative start: " + start);
        if (end < 0)
            throw new IllegalArgumentException("Negative end: " + start);
        if (whitespaceStart < 0)
            throw new IllegalArgumentException("Negative whitespaceStart: " + start);
        if (whitespaceEnd < 0)
            throw new IllegalArgumentException("Negative whitespaceEnd: " + start);
        if (start > end)
            throw new IllegalArgumentException(format("start (%d) > end (%d)", start, end));
        if (whitespaceStart > start)
            throw new IllegalArgumentException(
                format("whitespaceStart (%d) > start (%d)", whitespaceStart, start));
        if (end > whitespaceEnd)
            throw new IllegalArgumentException(
                format("end (%d) > whitespaceEnd (%d)", end, whitespaceEnd));

        this.start = start;
        this.end = end;
        this.whitespaceStart = whitespaceStart;
        this.whitespaceEnd = whitespaceEnd;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the size of the span, which is always {@code >= 0}.
     */
    public int size() {
        return end - start;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Return the size of the section of whitespace preceding the span, or 0 if there is none
     * or the information wasn't provided.
     */
    public int leadingWhitespaceSize() {
        return start - whitespaceStart;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Return the size of the section of whitespace following the span, or 0 if there is none
     * or the information wasn't provided.
     */
    public int trailingWhitespaceSize() {
        return whitespaceEnd - end;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the end offset, one past the last offset included in the span, which is {@code start +
     * size()}.
     */
    public int end() {
        return end;
    }

    // ---------------------------------------------------------------------------------------------

    private void checkBounds (int pos, int inputLength) {
        if (pos > inputLength)
            throw new IllegalStateException(
                this + " spans beyond end of input (size: " + inputLength + ")");
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the string spanned by this span in the given input, which is a string <b>that does
     * not contain any {@link Character#isSurrogate(char) surrogate character}</b> (said otherwise,
     * all the Unicode code points contained in the string are in the {@link
     * Character#isBmpCodePoint Basic Multilingual Plane} and can thus be represented as a single
     * {@code char}. Otherwise use {@link #get(int[])}.
     */
    public String get (String input) {
        checkBounds(end, input.length());
        return input.substring(start, end);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the string spanned by this span in the given input, which is a list of
     * unicode codepoints.
     */
    public String get (int[] input) {
        checkBounds(end, input.length);
        return new String(input, start, end - start);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the string spanned by this span in the given input, which is a string <b>that does
     * not contain any {@link Character#isSurrogate(char) surrogate character}</b> (said otherwise,
     * all the Unicode code points contained in the string are in the {@link
     * Character#isBmpCodePoint Basic Multilingual Plane} and can thus be represented as a single
     * {@code char}. Otherwise use {@link #getSubsequence(int[])}.
     *
     * <p>It is returned as a {@link CharSequence} backed by the passed string, which will
     * be retained in memory as long as the char sequence is reachable.
     */
    public CharSequence getSubsequence (String input) {
        checkBounds(end, input.length());
        return CharBuffer.wrap(input).subSequence(start, end);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the substring spanned by this span in the given input, which is a list of
     * unicode codepoints.
     *
     * <p>It is returned as a {@link CharSequence} backed by the passed string, which will
     * be retained in memory as long as the char sequence is reachable.
     */
    public CharSequence getSubsequence (int[] input) {
        checkBounds(end, input.length);
        return new UnicodeCharSequence(input, start, end);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the sublist spanned by this span in the given input.
     */
    public <T> List<T> get (List<T> input) {
        checkBounds(end, input.size());
       return new ArrayList<>(input.subList(start, end));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the sublist spanned by this span in the given input.
     *
     * <p>It is returned as a list backed by the passed list, which will be retained in memory as
     * long as the returned list is reachable.
     */
    public <T> List<T> getSubsequence (List<T> input) {
        checkBounds(end, input.size());
        return input.subList(start, end);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a span spanning the leading whitespace associated with the current span.
     *
     * <p>The returned span does not itself have any leading or trailing whitespace (that would be
     * meaningless). In other words it is defined by its own {@link #start} - {@link #end} range.
     */
    public Span getLeadingWhitespace() {
        return new Span(whitespaceStart, start);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a span spanning the trailing whitespace associated with the current span.
     *
     * <p>The returned span does not itself have any leading or trailing whitespace (that would be
     * meaningless). In other words it is defined by its own {@link #start} - {@link #end} range.
     */
    public Span getTrailingWhitespace() {
        return new Span(end, whitespaceEnd);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public int hashCode() {
        int h = start;
        h = h * 31 + end;
        h = h * 31 + whitespaceStart;
        h = h * 31 + whitespaceEnd;
        return h;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public boolean equals (Object other) {
        if (!(other instanceof Span))
            return false;
        Span s = (Span) other;
        return start == s.start && end == s.end
            && whitespaceStart == s.whitespaceStart && whitespaceEnd == s.whitespaceEnd;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toString()
    {
        return start == whitespaceStart && end == whitespaceEnd
            ? "span(" + start + " to " + end + ")"
            : format("span((%s-)%s to %s(-%s)", whitespaceStart, start, end, whitespaceEnd);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a string represetation of this span in terms of line/column coordinates,
     * using the supplied line map.
     */
    public String toString (LineMap lineMap)
    {
        return start == whitespaceStart && end == whitespaceEnd
            ? format("span(%s to %s)",
                lineMap.positionFrom(start), lineMap.positionFrom(end))
            : format("span((%s-)%s to %s(-%s)",
                lineMap.positionFrom(whitespaceStart), lineMap.positionFrom(start),
                lineMap.positionFrom(end), lineMap.positionFrom(whitespaceEnd));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a string representation of the start position of this span, using hte given line map.
     *
     * <p>Unlike other string methods in this class, this one will include the input name
     * (usually a file name) from the line map.
     *
     * <p>This method is a convenience wrapper, if you want the same kind of representation for
     * the other offset held in the span, simply use {@link LineMap#stringWithName(int)}.
     */
    public String startString (LineMap map) {
        return map.stringWithName(start);
    }

    // ---------------------------------------------------------------------------------------------
}

