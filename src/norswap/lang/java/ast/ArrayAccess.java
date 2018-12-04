package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ArrayAccess implements Expression
{
    public abstract Expression operand();
    public abstract Expression index();

    public static ArrayAccess mk (Expression operand, Expression index) {
        return new AutoValue_ArrayAccess(operand, index);
    }
}
