package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class MethodCall implements Expression
{
    public abstract @Nullable Expression receiver();
    public abstract List<TType> type_args();
    public abstract Identifier name();
    public abstract List<Expression> args();

    public static MethodCall mk
        (@Nullable Expression receiver, List<TType> type_args, Identifier name,
         List<Expression> args)
    {
        return new AutoValue_MethodCall(receiver, type_args, name, args);
    }
}
