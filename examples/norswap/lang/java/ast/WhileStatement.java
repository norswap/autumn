package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class WhileStatement implements Statement
{
    public abstract Expression cond();
    public abstract Statement body();

    public static WhileStatement mk (Expression cond, Statement body) {
        return new AutoValue_WhileStatement(cond, body);
    }
}
