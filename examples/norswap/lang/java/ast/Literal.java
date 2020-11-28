package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;
import java.util.function.Function;

@AutoValue
public abstract class Literal implements Expression
{
    public static Function<Object, Object> test_convert_hook = null;

    public abstract Object value();

    public static Literal mk (Object value) {
        if (test_convert_hook != null) value = test_convert_hook.apply(value);
        return new AutoValue_Literal(value);
    }
}
