package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class SynchronizedStatement implements Statement
{
    public abstract Expression expression();
    public abstract Block body();

    public static SynchronizedStatement mk (Expression expression, Block body)
    {
        return new AutoValue_SynchronizedStatement(expression, body);
    }
}
