package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class PrimitiveType implements TType
{
    public abstract List<TAnnotation> annotations();
    public abstract String name();

    public static PrimitiveType make (List<TAnnotation> annotations, String name) {
        return new AutoValue_PrimitiveType(annotations, name);
    }
}
