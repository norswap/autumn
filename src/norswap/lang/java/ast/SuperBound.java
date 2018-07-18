package norswap.lang.java.ast;

public class SuperBound implements TypeBound
{
    public final TType type;

    public SuperBound (TType type) {
        this.type = type;
    }

    @Override public TType type () {
        return null;
    }
}
