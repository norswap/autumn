package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class ArrayType implements TType
{
    public abstract TType stem();
    public abstract List<Dimension> dimensions();

    public static ArrayType mk (TType stem, List<Dimension> dimensions) {
        return new AutoValue_ArrayType(stem, dimensions);
    }
}
