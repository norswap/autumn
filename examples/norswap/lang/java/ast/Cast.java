package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class Cast implements Expression
{
    public abstract List<TType> types();
    public abstract Expression operand();

    public static Cast mk (List<TType> types, Expression operand) {
        return new AutoValue_Cast(types, operand);
    }
}