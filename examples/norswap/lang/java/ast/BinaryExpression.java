package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class BinaryExpression implements Expression
{
    public abstract BinaryOperator operator();
    public abstract Expression left();
    public abstract Expression right();

    public static BinaryExpression mk (BinaryOperator operator, Expression left, Expression right) {
        return new AutoValue_BinaryExpression(operator, left, right);
    }
}
