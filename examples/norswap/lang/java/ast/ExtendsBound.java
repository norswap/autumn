package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ExtendsBound implements TypeBound
{
    @Override public abstract TType type();

    public static ExtendsBound mk (TType type) {
        return new AutoValue_ExtendsBound(type);
    }
}
