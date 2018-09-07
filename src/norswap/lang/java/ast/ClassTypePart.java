package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class ClassTypePart
{
    public abstract List<TAnnotation> annotations();
    public abstract Identifier name();
    public abstract List<TType> type_arguments();

    public static ClassTypePart make
            (List<TAnnotation> annotations, Identifier name, List<TType> type_arguments) {
        return new AutoValue_ClassTypePart(annotations, name, type_arguments);
    }
}
