package norswap.autumn;

import java.time.Duration;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * A set of per-parser performance metrics ({@link ParserMetrics}), which are collected
 * when a parse is running in tracing mode ({@link ParseOptions#trace}).
 *
 * <p>Currently just a wrapper around a {@code Map[Parser, ParserMetrics]}, along with a useful
 * {@code toString()} method that sorts the parser by self time.
 */
public final class ParseMetrics
{
    // ---------------------------------------------------------------------------------------------

    public final Map<Parser, ParserMetrics> metrics = new HashMap<>();

    // ---------------------------------------------------------------------------------------------

    @Override public String toString()
    {
        StringBuilder b = new StringBuilder(String.format("%40s | %-16s | %-16s | %s\n",
            "PARSER", "SELF TIME", "TOTAL TIME", "INVOCATIONS"));

        metrics.entrySet().stream()
            .sorted(Comparator.comparingLong(
                (Map.Entry<Parser, ParserMetrics> it) -> it.getValue().selfTime).reversed())
            .forEach(it -> {
                ParserMetrics v = it.getValue();
                b.append(String.format("%40s | %-16s | %-16s | %,d\n",
                    it.getKey(),
                    Duration.ofNanos(v.selfTime),
                    Duration.ofNanos(v.totalTime),
                    v.invocations));
            });

        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------
}
