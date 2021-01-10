package norswap.autumn;

import java.time.Duration;

/**
 * A set of performance metrics linked to a parser, produced in tracing mode ({@link
 * ParseOptions#trace}).
 *
 * <p>Multiple {@link ParserMetrics} are aggregated within a single {@link ParseMetrics}.
 *
 * <p>Field are public for convenience but should not be written.
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
    public long selfTime = 0;

    // ---------------------------------------------------------------------------------------------

    /**
     * Cumulative "total" execution time for the parser (including the execution time of its
     * children).
     *
     * <p>Note that parser that recurse are not double-counted: only the top parser contributes
     * to the total time.
     */
    public long totalTime = 0;

    // ---------------------------------------------------------------------------------------------

    /**
     * Running counter of the number of in-progress invocations (so the parser is recursing
     * when > 1).
     */
    int recursiveInvocations = 0;

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

    @Override public String toString() {
        return "ParserMetrics{" +
            "parser: " + parser +
            ", self: "  + Duration.ofNanos(selfTime) +
            ", total: " + Duration.ofNanos(totalTime) +
            ", invocs:" + String.format("%,d", invocations) +
            '}';
    }

    // ---------------------------------------------------------------------------------------------
}
