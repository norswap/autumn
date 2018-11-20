package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class PostIncrement
{
    public abstract Expression operand();

    public static PostIncrement mk (Expression operand) {
        return new AutoValue_PostIncrement(operand);
    }
}