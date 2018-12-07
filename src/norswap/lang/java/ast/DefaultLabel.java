package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class DefaultLabel implements SwitchLabel
{
    public static DefaultLabel mk() {
        return new AutoValue_DefaultLabel();
    }
}
