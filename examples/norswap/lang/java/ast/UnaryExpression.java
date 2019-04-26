package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class UnaryExpression implements Expression
{
    public abstract UnaryOperator operator();
    public abstract Expression operand();

    public static UnaryExpression mk (UnaryOperator operator, Expression operand) {
        return new AutoValue_UnaryExpression(operator, operand);
    }
}
