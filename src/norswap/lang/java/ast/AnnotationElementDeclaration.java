package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class AnnotationElementDeclaration implements Declaration
{
    public abstract List<Modifier> modifiers();
    public abstract TType type();
    public abstract Identifier name();
    public abstract List<Dimension> dimensions();
    public abstract @Nullable AnnotationElement value();

    public static AnnotationElementDeclaration mk (
        List<Modifier> modifiers, TType type, Identifier name, List<Dimension> dimensions,
        @Nullable AnnotationElement value)
    {
        return new AutoValue_AnnotationElementDeclaration(modifiers, type, name, dimensions, value);
    }
}
