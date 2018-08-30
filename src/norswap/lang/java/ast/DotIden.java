package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class DotIden
{
    public abstract Expression operand();
    public abstract Identifier id();

    public static DotIden make (Expression operand, Identifier id) {
        return new AutoValue_DotIden(operand, id);
    }
}
