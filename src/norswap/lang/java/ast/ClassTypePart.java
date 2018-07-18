package norswap.lang.java.ast;

import java.util.List;

public class ClassTypePart
{
    public final List<TAnnotation> annotations;
    public final String name;
    public final List<TType> type_arguments;

    public ClassTypePart (List<TAnnotation> annotations, String name, List<TType> type_arguments) {
        this.annotations = annotations;
        this.name = name;
        this.type_arguments = type_arguments;
    }
}
