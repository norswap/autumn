package norswap.autumn.positions;

/**
 * Represents a file position as a line/column pair.
 */
public final class Position
{
    public final int line;
    public final int column;

    public Position (int line, int column) {
        this.line = line;
        this.column = column;
    }

    @Override public int hashCode () {
        return line * 31 + column;
    }

    @Override public boolean equals (Object other) {
        if (!(other instanceof Position))
            return false;
        Position p = (Position) other;
        return line == p.line && column == p.column;
    }

    @Override public String toString () {
        return line + ":" + column;
    }
}
