package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class SemiStatement implements Statement
{
    public static SemiStatement mk() {
        return new AutoValue_SemiStatement();
    }
}
