package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class UnaryPlus
{
    public abstract Expression operand();

    public static UnaryPlus mk (Expression operand) {
        return new AutoValue_UnaryPlus(operand);
    }
}