package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class DoWhileStatement implements Statement
{
    public abstract Statement body();
    public abstract Expression cond();

    public static DoWhileStatement mk (Statement body, Expression cond) {
        return new AutoValue_DoWhileStatement(body, cond);
    }
}
