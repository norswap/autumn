package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class DotSuper
{
    public abstract Expression operand();

    public static DotSuper mk (Expression operand) {
        return new AutoValue_DotSuper(operand);
    }
}
