package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class SwitchStatement implements Statement
{
    public abstract Expression expression();
    public abstract List<SwitchClause> clauses();

    public static SwitchStatement mk (Expression expression, List<SwitchClause> clauses)
    {
        return new AutoValue_SwitchStatement(expression, clauses);
    }
}
