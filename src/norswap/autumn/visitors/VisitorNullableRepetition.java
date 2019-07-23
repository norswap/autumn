package norswap.autumn.visitors;

/**
 * Instantiable version of {@link _VisitorNullableRepetition}.
 */
public class VisitorNullableRepetition implements _VisitorNullableRepetition
{
    // ---------------------------------------------------------------------------------------------

    private final _VisitorNullable nullable_visitor;

    // ---------------------------------------------------------------------------------------------

    private boolean result;

    // ---------------------------------------------------------------------------------------------

    /**
     * Creates a new instance with the given nullable visitor.
     *
     * <p>Use this if you must support custom parsers.
     */
    public VisitorNullableRepetition (_VisitorNullable nullable_visitor) {
        this.nullable_visitor = nullable_visitor;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Creates a new well-formedness checker using a nullable visitors for built-in parsers only.
     *
     * <p>Use the other constructor if you must support custom parsers.
     */
    public VisitorNullableRepetition () {
        this( new VisitorNullable());
    }

    // ---------------------------------------------------------------------------------------------

    @Override public _VisitorNullable nullable_visitor() {
        return nullable_visitor;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public boolean result () {
        return result;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public boolean set_result (boolean value) {
        result = value;
        return value;
    }

    // ---------------------------------------------------------------------------------------------
}
