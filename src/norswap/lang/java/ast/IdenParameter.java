package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class IdenParameter implements FormalParameter
{
    public abstract List<Modifier> modifiers();
    public abstract TType type();
    public abstract Identifier name();
    public abstract List<Dimension> dimensions();

    public static IdenParameter mk
        (List<Modifier> modifiers, TType type, Identifier name, List<Dimension> dimensions) {
        return new AutoValue_IdenParameter(modifiers, type, name, dimensions);
    }
}

