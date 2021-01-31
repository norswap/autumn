package norswap.autumn.positions;

/**
 * Represents a string position as a line/column pair, where columns are expressed in character
 * width.
 * <p>
 * Each character has a width of one, except tabs which jumps to the nearest multiple of
 * the tab width, as configured in {@link LineMapString} and {@link LineMapTokens}, which are
 * typically used to create these objects.
 */
public final class Position
{
    public final int line;
    public final int column;

    public Position (int line, int column) {
        this.line = line;
        this.column = column;
    }

    @Override public int hashCode() {
        return line * 31 + column;
    }

    @Override public boolean equals (Object other) {
        if (!(other instanceof Position))
            return false;
        Position p = (Position) other;
        return line == p.line && column == p.column;
    }

    @Override public String toString() {
        return line + ":" + column;
    }
}
