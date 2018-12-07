package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class TryResource implements Statement
{
    public abstract List<Modifier> modifiers();
    public abstract TType type();
    public abstract VarDeclaratorID id();
    public abstract Expression value();

    public static TryResource mk (
        List<Modifier> modifiers, TType type, VarDeclaratorID id, Expression value)
    {
        return new AutoValue_TryResource(modifiers, type, id, value);
    }
}
