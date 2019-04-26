package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Identifier implements Expression
{
    public abstract String name();

    public static Identifier mk (String name) {
        return new AutoValue_Identifier(name);
    }
}
