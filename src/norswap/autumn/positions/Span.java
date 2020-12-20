package norswap.autumn.positions;

import norswap.autumn.UnicodeCharSequence;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

/**
 * Represents a segment of the input.
 */
public final class Span
{
    // ---------------------------------------------------------------------------------------------

    /**
     * Inclusive start index.
     */
    public final int start;

    /**
     * Exclusive end index.
     */
    public final int end;

    // ---------------------------------------------------------------------------------------------

    public Span (int start, int end) {
        if (start < 0)
            throw new IllegalArgumentException("Negative index: " + start);
        if (end < 0)
            throw new IllegalArgumentException("Negative length: " + start);
        if (start > end)
            throw new IllegalArgumentException(format("start (%d) > end (%d)", start, end));

        this.start = start;
        this.end = end;
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
     * Returns the end index, one past the last index included in the span, which is {@code index +
     * length}.
     */
    public int end() {
        return end;
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
        check_bounds(input.length());
        return input.substring(start, end);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the string spanned by this span in the given input, which is a list of
     * unicode codepoints.
     */
    public String get (int[] input) {
        check_bounds(input.length);
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
        check_bounds(input.length());
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
        check_bounds(input.length);
        return new UnicodeCharSequence(input, start, end);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the sublist spanned by this span in the given input.
     */
    public <T> List<T> get (List<T> input) {
        check_bounds(input.size());
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
        check_bounds(input.size());
        return input.subList(start, end);
    }

    // ---------------------------------------------------------------------------------------------

    private void check_bounds (int input_length) {
        if (end > input_length)
            throw new IllegalStateException(
                this + " spans beyond end of input (size: " + input_length + ")");
    }

    // ---------------------------------------------------------------------------------------------

    @Override public int hashCode() {
        return start * 47 + end;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public boolean equals (Object other) {
        if (!(other instanceof Span))
            return false;
        Span s = (Span) other;
        return start == s.start && end == s.end;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toString() {
        return "span(" + start + " to " + end + ")";
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a string represetation of this span in terms of line/column coordinates,
     * using the supplied line map.
     */
    public String toString (LineMap line_map) {
        return "span(" + line_map.position_from(start)
            + " to " +  line_map.position_from(end) + ")";
    }

    // ---------------------------------------------------------------------------------------------
}

