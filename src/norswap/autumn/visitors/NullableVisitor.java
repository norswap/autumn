package norswap.autumn.visitors;

/**
 * Instantiable version of {@link _NullableVisitor}.
 */
public final class NullableVisitor implements _NullableVisitor
{
    // ---------------------------------------------------------------------------------------------

    private boolean result;

    // ---------------------------------------------------------------------------------------------

    @Override public boolean result () {
        return result;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void set_result (boolean value) {
        result = value;
    }

    // ---------------------------------------------------------------------------------------------
}
