package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class EnhancedForStatement implements Statement
{
    public abstract List<Modifier> modifiers();
    public abstract TType type();
    public abstract VarDeclaratorID id();
    public abstract Expression iterable();
    public abstract Statement body();

    public static EnhancedForStatement mk (
        List<Modifier> modifiers, TType type, VarDeclaratorID id, Expression iterable,
        Statement body)
    {
        return new AutoValue_EnhancedForStatement(modifiers, type, id, iterable, body);
    }
}
