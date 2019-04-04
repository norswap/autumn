package norswap.autumn.visitors;

import norswap.autumn.Parser;
import java.util.HashSet;
import java.util.Set;

/**
 * Instantiable version of {@link _VisitorFirstParsers}.
 */
public class VisitorFirstParsers implements _VisitorFirstParsers
{
    // ---------------------------------------------------------------------------------------------

    private final _VisitorNullable nullable_visitor;

    // ---------------------------------------------------------------------------------------------

    private Set<Parser> parsers = new HashSet<>();

    // ---------------------------------------------------------------------------------------------

    /**
     * Creates a new instance with the given nullable visitor.
     *
     * <p>Use this if you must support custom parsers.
     */
    public VisitorFirstParsers (_VisitorNullable nullable_visitor) {
        this.nullable_visitor = nullable_visitor;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Creates a new well-formedness checker using a nullable visitors for built-in parsers only.
     *
     * <p>Use the other constructor if you must support custom parsers.
     */
    public VisitorFirstParsers () {
        this(new VisitorNullable());
    }

    // ---------------------------------------------------------------------------------------------

    @Override public _VisitorNullable nullable_visitor () {
        return nullable_visitor;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public Set<Parser> firsts () {
        return parsers;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void renew_firsts () {
        parsers = new HashSet<>();
    }

    // ---------------------------------------------------------------------------------------------
}
