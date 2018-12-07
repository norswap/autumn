package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class BreakStatement implements Statement
{
    public abstract @Nullable Identifier label();

    public static BreakStatement mk (@Nullable Identifier label) {
        return new AutoValue_BreakStatement(label);
    }
}
