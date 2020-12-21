package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;
import norswap.utils.data.wrappers.Pair;
import java.util.List;

@AutoValue
public abstract class NormalAnnotation implements TAnnotation
{

    public abstract List<Identifier> name();
    public abstract List<Pair<Identifier, AnnotationElement>> elements();

    public static NormalAnnotation mk
        (List<Identifier> name, List<Pair<Identifier, AnnotationElement>> elements) {
        return new AutoValue_NormalAnnotation(name, elements);
    }
}
