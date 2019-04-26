package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;
import java.lang.annotation.Annotation;
import java.util.List;

@AutoValue
public abstract class TypeParameter
{
    public abstract List<Annotation> annotations();
    public abstract Identifier name();
    public abstract List<TType> bounds();

    public static TypeParameter mk (List<Annotation> annotations, Identifier name, List<TType> bounds) {
        return new AutoValue_TypeParameter(annotations, name, bounds);
    }
}
