package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class This implements Expression
{
    public static This mk() {
        return new AutoValue_This();
    }
}
