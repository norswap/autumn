package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class PrimitiveType implements TType
{
    public abstract List<TAnnotation> annotations();
    public abstract BasicType name();

    public static PrimitiveType mk (List<TAnnotation> annotations, BasicType name) {
        return new AutoValue_PrimitiveType(annotations, name);
    }
}
