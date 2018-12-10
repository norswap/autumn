package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class IfStatement implements Statement
{
    public abstract Expression cond();
    public abstract Statement if_true();
    public abstract @Nullable Statement if_false();

    public static IfStatement mk (Expression cond, Statement if_true, @Nullable Statement if_false) {
        return new AutoValue_IfStatement(cond, if_true, if_false);
    }
}
