package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class TernaryExpression implements Expression
{
    public abstract Expression cond();
    public abstract Expression if_true();
    public abstract Expression if_false();

    public static TernaryExpression mk (Expression cond, Expression if_true, Expression if_false) {
        return new AutoValue_TernaryExpression(cond, if_true, if_false);
    }
}
