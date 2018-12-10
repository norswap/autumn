package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class InstanceOf implements Expression
{
    public abstract Expression operand();
    public abstract TType type();

    public static InstanceOf mk (Expression operand, TType type) {
        return new AutoValue_InstanceOf(operand, type);
    }
}