package norswap.lang.java.ast;

import norswap.utils.Pair;
import java.util.List;

public class NormalAnnotation implements TAnnotation
{
    public final List<String> name;
    public final List<Pair<String, AnnotationElement>> elements;

    public NormalAnnotation (List<String> name, List<Pair<String, AnnotationElement>> elements) {
        this.name = name;
        this.elements = elements;
    }
}
