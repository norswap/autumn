package norswap.lang.java.ast;

public final class Literal
{
    public final Object value;

    public Literal (Object value) {
        this.value = value;
    }

    @Override public String toString()
    {
        return value.toString();
    }
}
