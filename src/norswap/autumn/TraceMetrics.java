package norswap.autumn;

import java.util.HashMap;
import java.util.Map;

/**
 * A set of per-parser performance metrics (see {@link ParserMetrics}), which are collected
 * when a parse is running in tracing mode (see {@link ParseOptions#TRACE}.
 *
 * <p>Currently just a wrapper around a {@code Map[Parser, ParserMetrics]}.
 */
public final class TraceMetrics
{
    // ---------------------------------------------------------------------------------------------

    public final Map<Parser, ParserMetrics> metrics = new HashMap<>();

    // ---------------------------------------------------------------------------------------------
}
