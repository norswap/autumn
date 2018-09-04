package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class SuperBound implements TypeBound
{
    @Override public abstract TType type();

    public static SuperBound make (TType type) {
        return new AutoValue_SuperBound(type);
    }
}
