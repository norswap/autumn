package norswap.lang.java.ast;

import java.lang.annotation.Annotation;
import java.util.List;

public class TypeParam
{
    public final List<Annotation> annotations;
    public final String name;
    public final List<TType> bounds;

    public TypeParam (List<Annotation> annotations, String name, List<TType> bounds) {
        this.annotations = annotations;
        this.name = name;
        this.bounds = bounds;
    }
}
