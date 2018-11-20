package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Complement
{
    public abstract Expression operand();

    public static Complement mk (Expression operand) {
        return new AutoValue_Complement(operand);
    }
}