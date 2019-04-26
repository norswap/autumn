package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class DotNew implements Expression
{
    public abstract Expression operand();
    public abstract ConstructorCall constructor();

    public static DotNew mk (Expression operand, ConstructorCall constructor) {
        return new AutoValue_DotNew(operand, constructor);
    }
}
