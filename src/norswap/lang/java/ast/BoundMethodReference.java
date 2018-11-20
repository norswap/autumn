package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class BoundMethodReference implements Expression
{
    public abstract Expression receiver();
    public abstract List<TType> type_args();
    public abstract Identifier name();

    public static BoundMethodReference mk
        (Expression receiver, List<TType> type_args, Identifier name)
    {
        return new AutoValue_BoundMethodReference(receiver, type_args, name);
    }
}
