package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class SingleElementAnnotation implements TAnnotation
{
    abstract public List<Identifier> name();
    abstract public AnnotationElement elem();

    public static SingleElementAnnotation mk (List<Identifier> name, AnnotationElement elem) {
        return new AutoValue_SingleElementAnnotation(name, elem);
    }
}
