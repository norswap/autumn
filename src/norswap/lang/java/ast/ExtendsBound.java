package norswap.lang.java.ast;

public class ExtendsBound implements TypeBound
{
    public final TType type;

    public ExtendsBound (TType type) {
        this.type = type;
    }

    @Override public TType type () {
        return null;
    }
}
