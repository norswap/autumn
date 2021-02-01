package norswap.autumn.positions;

/**
 * Utilities to help implement things in {@link LineMap}, since it is an interface and cannot
 * have private methods.
 */
class LineMapUtils
{
    // ---------------------------------------------------------------------------------------------

    private final static boolean AUTUMN_USE_CHAR_COLUMN;

    // ---------------------------------------------------------------------------------------------

    static {
        boolean useColumn;
        try {
            useColumn = System.getenv("AUTUMN_USE_CHAR_COLUMN") != null;
        } catch (SecurityException e) {
            // no permission to read env vars, just return the default
            useColumn = false;
        }
        AUTUMN_USE_CHAR_COLUMN = useColumn;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a position suitable to be displayed, which might be the straightforward
     * position obtained through the line map, or a position whose column has been fixed
     * to be expressed in terms of characters rather than width, if the environment variable
     * {@code AUTUMN_USE_CHAR_COLUMN} is set.
     */
    static Position positionForDisplay (LineMap map, int offset)
    {
        Position position = map.positionFrom(offset);

        if (!AUTUMN_USE_CHAR_COLUMN)
            return position;

        int lineOffset = map.offsetFor(position.line);
        int charColumn = offset - lineOffset;
        return new Position(position.line, charColumn);
    }

    // ---------------------------------------------------------------------------------------------
}
