package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class BoundMethodReference implements Expression
{
    public abstract Expression receiver();
    public abstract TType type();
    public abstract List<TType> type_args();
    public abstract Identifier name();

    public static BoundMethodReference mk
        (Expression receiver, TType type, List<TType> type_args, Identifier name)
    {
        return new AutoValue_BoundMethodReference(receiver, type, type_args, name);
    }
}
