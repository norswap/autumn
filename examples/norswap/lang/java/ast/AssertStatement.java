package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class AssertStatement implements Statement
{
    public abstract Expression expression();
    public abstract @Nullable Expression message();

    public static AssertStatement mk (Expression expression, @Nullable Expression message)
    {
        return new AutoValue_AssertStatement(expression, message);
    }
}
