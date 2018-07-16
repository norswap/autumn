package norswap.lang.java.ast;

import java.util.List;

public class AnnotationElementList implements AnnotationElement
{
    public final List<AnnotationElement> elements;

    public AnnotationElementList (List<AnnotationElement> elements) {
        this.elements = elements;
    }
}
