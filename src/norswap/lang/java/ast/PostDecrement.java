package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class PostDecrement
{
    public abstract Expression operand();

    public static PostDecrement mk (Expression operand) {
        return new AutoValue_PostDecrement(operand);
    }
}