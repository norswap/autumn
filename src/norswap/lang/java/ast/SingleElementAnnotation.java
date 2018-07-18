package norswap.lang.java.ast;

import java.util.List;

public class SingleElementAnnotation implements TAnnotation
{
    public final List<String> name;
    public final AnnotationElement elem;

    public SingleElementAnnotation (List<String> name, AnnotationElement elem) {
        this.name = name;
        this.elem = elem;
    }
}
