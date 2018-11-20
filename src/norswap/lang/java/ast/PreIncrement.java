package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class PreIncrement
{
    public abstract Expression operand();

    public static PreIncrement mk (Expression operand) {
        return new AutoValue_PreIncrement(operand);
    }
}