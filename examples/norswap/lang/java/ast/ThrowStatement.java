package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ThrowStatement implements Statement
{
    public abstract Expression expression();

    public static ThrowStatement mk (Expression expression) {
        return new AutoValue_ThrowStatement(expression);
    }
}
