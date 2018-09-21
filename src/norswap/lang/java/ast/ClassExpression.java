package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ClassExpression implements Expression
{
    public abstract TType type();

    public static ClassExpression mk (TType type) {
        return new AutoValue_ClassExpression(type);
    }
}
