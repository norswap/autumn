package norswap.autumn;

import java.util.HashMap;
import java.util.HashSet;

import static norswap.utils.Util.cast;

/**
 * This class represents a set of options that can be passed to one of the {@link Parse#run}
 * methods.
 *
 * <p>Instantiate this class by calling {@link #parse_options(ParseOption...)} with a set of {@link
 * ParseOption} obtained from its static members and methods.
 *
 * <p>Options are either binary flags ({@link #TRACE}, {@link #RECORD_CALL_STACK}) or a tag
 * associated with a value ({@link #METRICS}).
 */
public final class ParseOptions
{
    // ---------------------------------------------------------------------------------------------

    /**
     * The default parse options, which is an empty set of options.
     */
    public static final ParseOptions DEFAULT_PARSE_OPTIONS = parse_options();

    // ---------------------------------------------------------------------------------------------

    /**
     * A parse option. It's impossible for the user to instantiate this type.
     */
    public static class ParseOption {
        public final String name;
        private ParseOption (String name) {
            this.name = name;
        }
        @Override public String toString() { return name; }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * A parse option representing a flag.
     */
    public static class ParseOptionFlag extends ParseOption {
        private ParseOptionFlag (String name) { super(name); }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * A tag identifying a valid {@link ValuedParseOption}
     */
    public static class ParseOptionTag {
        public final String name;
        private ParseOptionTag (String name) {
            this.name = name;
        }
        @Override public String toString() { return name; }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * A parse option comprised of a tag and an associated value.
     */
    public static class ValuedParseOption extends ParseOption
    {
        public final ParseOptionTag tag;
        public final Object value;

        private ValuedParseOption (ParseOptionTag tag, Object value) {
            super(tag.name);
            this.tag = tag;
            this.value = value;
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Indicate whether the parse traces its execution. This records performance metrics for each
     * parser (see {@link ParserMetrics}) into {@link Parse#trace_metrics}. Enabling this flag does
     * slow down the execution considerably (around x2 in our initial tests).
     */
    public static final ParseOptionFlag TRACE = new ParseOptionFlag("TRACE");

    // ---------------------------------------------------------------------------------------------

    /**
     * Indicates wether the parse records the stack of parser invocations in {@link
     * Parse#call_stack}.
     */
    public static final ParseOptionFlag RECORD_CALL_STACK = new ParseOptionFlag("RECORD_CALL_STACK");

    // ---------------------------------------------------------------------------------------------

    /**
     * Tag for an option valued by trace metrics. See {@link #METRICS(TraceMetrics)}.
     */
    public static final ParseOptionTag METRICS = new ParseOptionTag("METRICS");

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a new parse option with tag {@link #METRICS} and the given value.
     *
     * <p>Implies {@link #TRACE}.
     */
    public static ParseOption METRICS (TraceMetrics trace_metrics) {
        return new ValuedParseOption(METRICS, trace_metrics);
    }

    // ---------------------------------------------------------------------------------------------

    private static ParseOptionTag IF_TAG = new ParseOptionTag("IF_TAG");

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a parse option that will include {@code option} in the set only if {@code cond} is
     * true.
     */
    public static ParseOption IF (boolean cond, ParseOption option){
        return new ValuedParseOption(IF_TAG, option);
    }

    // ---------------------------------------------------------------------------------------------

    private final HashSet<ParseOption> flags = new HashSet<>();

    // ---------------------------------------------------------------------------------------------

    private final HashMap<ParseOptionTag, Object> values = new HashMap<>();

    // ---------------------------------------------------------------------------------------------

    /**
     * Creates a new set of parse options from those supplied.
     */
    public static ParseOptions parse_options(ParseOption... options) {
        return new ParseOptions(options);
    }

    // ---------------------------------------------------------------------------------------------

    private ParseOptions (ParseOption... options)
    {
        for (ParseOption opt: options) {
            if (opt instanceof ValuedParseOption) {
                ValuedParseOption vopt = cast(opt);
                if (vopt.tag == IF_TAG)
                    opt = cast(vopt.value);
            }
            if (opt instanceof ValuedParseOption) {
                ValuedParseOption vopt = cast(opt);

                if (vopt.value == null)
                    throw new IllegalArgumentException(
                        "Parse option for tag [" + vopt.tag + "] is null.");

                if (values.put(vopt.tag, vopt.value) != null)
                    throw new IllegalArgumentException(
                        "Duplicate valued option for tag: " + vopt.tag);
            } else if (!flags.add(opt)) {
                throw new IllegalArgumentException("Duplicate flag option: " + opt);
            }
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Indicates whether the given flag was supplied to this object.
     */
    public boolean has (ParseOption flag) {
        return flags.contains(flag);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Indicates whether a valued option with the given tag was supplied to this object.
     */
    public boolean has (ParseOptionTag tag) {
        return values.get(tag) != null;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the value associated with the given tag, or null if no such tag was supplied to
     * this object.
     *
     * <p>This method auto-casts its return value to the desired return type.
     */
    public <T> T value (ParseOptionTag tag) {
        return cast(values.get(tag));
    }

    // ---------------------------------------------------------------------------------------------
}
