package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class DotThis
{
    public abstract Expression operand();

    public static DotThis mk (Expression operand) {
        return new AutoValue_DotThis(operand);
    }
}
