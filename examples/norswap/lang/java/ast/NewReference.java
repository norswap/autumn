package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class NewReference implements Expression
{
    public abstract TType type();
    public abstract List<TType> type_args();

    public static NewReference mk (TType type, List<TType> type_args) {
        return new AutoValue_NewReference(type, type_args);
    }
}
