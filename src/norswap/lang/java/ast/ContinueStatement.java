package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ContinueStatement implements Statement
{
    public abstract @Nullable Identifier label();

    public static ContinueStatement mk (@Nullable Identifier label) {
        return new AutoValue_ContinueStatement(label);
    }
}
