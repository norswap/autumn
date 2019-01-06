package norswap.autumn;

import java.time.Duration;

/**
 * A set of performance metrics linked to a parser, produced in tracing mode.
 *
 * @see Parse#trace
 */
public final class ParserMetrics
{
    // ---------------------------------------------------------------------------------------------

    public final Parser parser;

    // ---------------------------------------------------------------------------------------------

    /**
     * Cumulative "self" execution time for the parser (excluding the execution time of its
     * children).
     */
    public long self_time = 0;

    // ---------------------------------------------------------------------------------------------

    /**
     * Cumulative "total" execution time for the parser (including the execution time of its
     * children).
     *
     * <p>Note that parser that recurse are not double-counted: only the top parser contributes
     * to the total time.
     */
    public long total_time = 0;

    // ---------------------------------------------------------------------------------------------

    /**
     * Running counter of the number of in-progress invocations (so the parser is recursing
     * when > 1).
     */
    int recursive_invocations = 0;

    // ---------------------------------------------------------------------------------------------

    /**
     * Total number of invocations of the parser.
     */
    public int invocations = 0;

    // ---------------------------------------------------------------------------------------------

    public ParserMetrics (Parser parser) {
        this.parser = parser;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toString () {
        return "TraceMetrics{" +
            "parser: " + parser +
            ", self: "  + Duration.ofNanos(self_time) +
            ", total: " + Duration.ofNanos(total_time) +
            ", invocs:" + String.format("%,d", invocations) +
            '}';
    }

    // ---------------------------------------------------------------------------------------------
}
