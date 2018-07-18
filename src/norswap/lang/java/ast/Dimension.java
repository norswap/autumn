package norswap.lang.java.ast;

import java.util.List;

public class Dimension
{
    public final List<TAnnotation> annotations;

    public Dimension (List<TAnnotation> annotations) {
        this.annotations = annotations;
    }
}
