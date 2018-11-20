package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Negate
{
    public abstract Expression operand();

    public static Negate mk (Expression operand) {
        return new AutoValue_Negate(operand);
    }
}