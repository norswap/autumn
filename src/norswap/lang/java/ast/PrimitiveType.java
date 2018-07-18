package norswap.lang.java.ast;

import java.util.List;

public class PrimitiveType implements TType
{
    public final List<TAnnotation> annotations;
    public final String name;

    public PrimitiveType (List<TAnnotation> annotations, String name) {
        this.annotations = annotations;
        this.name = name;
    }
}
