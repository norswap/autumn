package norswap.lang.java.ast;

import java.util.List;

public class Wildcard implements TType
{
    public final List<TAnnotation> annotations;
    public final TypeBound bound;

    public Wildcard (List<TAnnotation> annotations, TypeBound bound) {
        this.annotations = annotations;
        this.bound = bound;
    }
}
