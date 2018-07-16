package norswap.lang.java.ast;

import java.util.List;

public class MarkerAnnotation implements Annotation
{
    public final List<String> name;

    public MarkerAnnotation (List<String> name) {
        this.name = name;
    }
}
