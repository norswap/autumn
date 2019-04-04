package norswap.autumn.visitors;

import norswap.autumn.Parser;
import java.util.HashSet;
import java.util.Set;

/**
 * Instantiable version of {@link _VisitorNullable}.
 */
public class VisitorNullable implements _VisitorNullable
{
    // ---------------------------------------------------------------------------------------------

    private boolean result;

    // ---------------------------------------------------------------------------------------------

    private Set<Parser> stack = new HashSet<>();

    // ---------------------------------------------------------------------------------------------

    @Override public boolean result () {
        return result;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void set_result (boolean value) {
        result = value;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public Set<Parser> stack () {
        return stack;
    }

    // ---------------------------------------------------------------------------------------------
}
