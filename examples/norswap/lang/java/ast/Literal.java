package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;
import java.util.function.Function;

@AutoValue
public abstract class Literal implements Expression
{
    public static Function<Object, Object> testConvertHook = null;

    public abstract Object value();

    public static Literal mk (Object value) {
        if (testConvertHook != null) value = testConvertHook.apply(value);
        return new AutoValue_Literal(value);
    }
}
