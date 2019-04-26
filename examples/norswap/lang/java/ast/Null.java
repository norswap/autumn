package norswap.lang.java.ast;

/**
 * Singleton class representing the null literal, since {@code null} itself is not accepted
 * in certain collections.
 */
public final class Null
{
    public static final Null NULL = new Null();
    private Null() {}

    @Override public String toString() {
        return "NULL";
    }
}
