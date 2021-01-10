package norswap.autumn;

import static java.lang.String.format;

/**
 * An implementation of {@link CharSequence} backed by a segment of a {@code int[]} array of Unicode
 * codepoints.
 */
public final class UnicodeCharSequence implements CharSequence
{
    /**
     * Array of code points backing the sequence.
     */
    public final int[] codePoints;

    /**
     * Inclusive start of the segment backing this sequence.
     */
    public final int start;

    /**
     * Exclusive end of the segment backing this sequence.
     */
    public final int end;

    /**
     * Creates a char sequence backed by the whole code point array.
     */
    public UnicodeCharSequence (int[] codePoints) {
        this(codePoints, 0, codePoints.length);
    }

    /**
     * Creates a char sequence backed by the delimited segment (start inclusive, end exclusive)
     * of the code point array.
     */
    public UnicodeCharSequence (int[] codePoints, int start, int end) {
        if (start < 0 || end < 0 || end > codePoints.length || start > end)
            throw new IndexOutOfBoundsException(format(
                "Invalid bounds [%d, %d[ for array of size %d", start, end, codePoints.length));
        this.codePoints = codePoints;
        this.start = start;
        this.end = end;
    }


    @Override public int length() {
        return end - start;
    }

    /**
     * Returns the character at the given index if it belongs to the basic multilingual plane (BMP)
     * ({@link Character#isBmpCodePoint(int)}), otherwise returns the nul character (`'\0') which
     * is illegal in most input format. Use {@link #codePointAt(int)} to disambiguate.
     */
    @Override public char charAt (int index) {
        int codePoint = codePoints[start + index];
        return Character.isBmpCodePoint(codePoint) ? (char) codePoint : '\0';
    }

    /**
     * Returns the code point at the given index.
     */
    public int codePointAt (int index) {
        return codePoints[start + index];
    }

    @Override public CharSequence subSequence (int start, int end) {
        return new UnicodeCharSequence(codePoints, this.start + start, this.start + end);
    }
}
