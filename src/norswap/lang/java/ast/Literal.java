package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Literal implements Expression
{
    public abstract Object value();

    public static Literal make (Object value) {
        return new AutoValue_Literal(value);
    }
}
