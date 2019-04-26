package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ReturnStatement implements Statement
{
    public abstract @Nullable Expression expression();

    public static ReturnStatement mk (@Nullable Expression expression) {
        return new AutoValue_ReturnStatement(expression);
    }
}
