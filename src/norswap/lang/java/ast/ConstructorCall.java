package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class ConstructorCall implements Expression
{
    public abstract List<TType> type_args();
    public abstract TType type();
    public abstract List<Expression> args();
    public abstract @Nullable List<Declaration> body();

    public static ConstructorCall mk
        (List<TType> type_args, TType type, List<Expression> args, @Nullable List<Declaration> body)
    {
        return new AutoValue_ConstructorCall(type_args, type, args, body);
    }
}
