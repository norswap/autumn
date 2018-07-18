package norswap.lang.java.ast;

import java.util.List;

public class ArrayType implements TType
{
    public final TType stem;
    public final List<Dimension> dimensions;

    public ArrayType (TType stem, List<Dimension> dimensions) {
        this.stem = stem;
        this.dimensions = dimensions;
    }
}
