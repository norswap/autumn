package norswap.lang.java.ast;

import java.util.Objects;

public class Literal implements Expression
{
    public final Object value;

    public Literal (Object value) {
        this.value = value;
    }

    @Override public String toString() {
        return value != null ? value.toString() : "null";
    }

    @Override public int hashCode() {
        return value.hashCode();
    }

    @Override public boolean equals (Object obj) {
        return obj instanceof Literal
            && Objects.equals(value, ((Literal) obj).value);
    }
}
