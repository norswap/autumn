package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class CatchClause implements Statement
{
    public abstract List<Modifier> modifiers();
    public abstract List<TType> types();
    public abstract VarDeclaratorID id();
    public abstract Block body();

    public static CatchClause mk (
        List<Modifier> modifiers, List<TType> types, VarDeclaratorID id, Block body)
    {
        return new AutoValue_CatchClause(modifiers, types, id, body);
    }
}
