package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class BasicFor implements Statement
{
    public abstract List<Statement> init();
    public abstract @Nullable Expression cond();
    public abstract List<Statement> iter();
    public abstract Statement body();

    public static BasicFor mk (
        List<Statement> init, @Nullable Expression cond, List<Statement> iter, Statement body)
    {
        return new AutoValue_BasicFor(init, cond, iter, body);
    }
}
