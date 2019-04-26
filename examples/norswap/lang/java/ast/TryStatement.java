package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class TryStatement implements Statement
{
    public abstract List<TryResource> resources();
    public abstract Block body();
    public abstract List<CatchClause> catch_clauses();
    public abstract @Nullable Block finally_block();

    public static TryStatement mk (
        List<TryResource> resources, Block body, List<CatchClause> catch_clauses,
        @Nullable Block finally_block)
    {
        return new AutoValue_TryStatement(resources, body, catch_clauses, finally_block);
    }
}
