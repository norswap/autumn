package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class AnnotationElementList implements AnnotationElement
{
    abstract public List<AnnotationElement> elements();

    public static AnnotationElementList mk (List<AnnotationElement> elements) {
        return new AutoValue_AnnotationElementList(elements);
    }
}
