package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class TypeMethodReference implements Expression
{
    public abstract TType type();
    public abstract List<TType> type_args();
    public abstract Identifier name();

    public static TypeMethodReference mk (TType type, List<TType> type_args, Identifier name) {
        return new AutoValue_TypeMethodReference(type, type_args, name);
    }
}
