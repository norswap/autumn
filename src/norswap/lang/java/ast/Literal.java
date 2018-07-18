package norswap.lang.java.ast;

public class Literal
{
    public final Object value;

    public Literal (Object value) {
        this.value = value;
    }

    @Override public String toString() {
        return value != null ? value.toString() : "null";
    }
}
