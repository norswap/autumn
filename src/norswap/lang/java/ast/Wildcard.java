package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class Wildcard implements TType
{
    public abstract List<TAnnotation> annotations();
    public abstract @Nullable TypeBound bound();

    public static Wildcard make(List<TAnnotation> annotations, @Nullable TypeBound bound) {
        return new AutoValue_Wildcard(annotations, bound);
    }
}
