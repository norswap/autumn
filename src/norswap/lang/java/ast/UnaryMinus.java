package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class UnaryMinus
{
    public abstract Expression operand();

    public static UnaryMinus mk (Expression operand) {
        return new AutoValue_UnaryMinus(operand);
    }
}