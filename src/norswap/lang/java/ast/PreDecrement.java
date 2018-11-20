package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class PreDecrement
{
    public abstract Expression operand();

    public static PreDecrement mk (Expression operand) {
        return new AutoValue_PreDecrement(operand);
    }
}