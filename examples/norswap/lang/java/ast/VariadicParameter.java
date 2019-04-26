package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class VariadicParameter implements FormalParameter
{
    public abstract List<Modifier> modifiers();
    public abstract TType type();
    public abstract List<TAnnotation> array_annotations();
    public abstract Identifier name();

    public static VariadicParameter mk (
        List<Modifier> modifiers, TType type, List<TAnnotation> array_annotations, Identifier name)
    {
        return new AutoValue_VariadicParameter(modifiers, type, array_annotations, name);
    }
}
