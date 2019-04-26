package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class SwitchClause implements Statement
{
    public abstract SwitchLabel label();
    public abstract List<Statement> statements();

    public static SwitchClause mk (SwitchLabel label, List<Statement> statements) {
        return new AutoValue_SwitchClause(label, statements);
    }
}
