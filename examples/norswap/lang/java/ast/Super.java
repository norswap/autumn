package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Super implements Expression
{
    public static Super mk() {
        return new AutoValue_Super();
    }
}
