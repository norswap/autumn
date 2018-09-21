package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ParenExpression implements Expression
{
    public abstract Expression expression();

    public static ParenExpression mk (Expression expression) {
        return new AutoValue_ParenExpression(expression);
    }
}
