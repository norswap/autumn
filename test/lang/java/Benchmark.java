package lang.java;

import norswap.autumn.Autumn;
import norswap.autumn.ParseOptions;
import norswap.autumn.ParseResult;
import norswap.autumn.Parser;
import norswap.autumn.ParserMetrics;
import norswap.autumn.TestFixture;
import norswap.autumn.ParseMetrics;
import norswap.lang.java.Grammar;
import norswap.utils.IO;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class Benchmark extends TestFixture
{
    // ---------------------------------------------------------------------------------------------

    private static final boolean DO_TRACE = false;
    private static final boolean DO_RECORD = false;

    // ---------------------------------------------------------------------------------------------

    private final ParseMetrics parse_metrics = new ParseMetrics();

    // ---------------------------------------------------------------------------------------------

    public void run (String corpus_path) throws IOException
    {
        final Grammar grammar = new Grammar();
        this.rule = grammar.root;
        final List<Path> paths = IO.glob("**/*.java", Paths.get(corpus_path));
        final int slices = 100;
        final int slice_size = (paths.size() + slices - 1) / slices;

        long time = 0L;
        int next_slice = slice_size;
        int percentage;
        int i = 0;

        long size = 0;

        ParseOptions.ParseOptionsBuilder builder = ParseOptions.builder();
        if (DO_TRACE)  builder.metrics(parse_metrics);
        if (DO_RECORD) builder.record_call_stack();
        ParseOptions options = builder.get();

        for (Path path: paths)
        {
            ++i;
            // System.out.println(i + " / " + path);
            String input = IO.slurp(""+ path);
            size += path.toFile().length();
            long t0 = System.nanoTime();
            ParseResult result = Autumn.parse(rule.get(), input, options);

            time += System.nanoTime() - t0;

            if (!result.full_match)
            {
                System.out.println(i + "/" + paths.size() + " -> " + path);
                try {
                    success(input);
                } catch (AssertionError e) {
                    System.out.println(e.getMessage());
                }
                break;
            }

            if (i >= next_slice)
            {
                percentage = (int) (100.0 * i / paths.size());
                System.out.println(percentage + "% (" + i + "/" + paths.size() + ")");
                next_slice += slice_size;
            }
        }

        System.out.println("Number of files: " + paths.size());
        System.out.println("Total size in bytes: " + String.format("%,d", size));
        System.out.println("Code parsed in: " + Duration.ofNanos(time));
        if (DO_TRACE) pretty_print_trace();
    }

    // ---------------------------------------------------------------------------------------------

    public void pretty_print_trace()
    {
        parse_metrics.metrics.entrySet().stream()
            .sorted(Comparator.comparingLong(
                (Map.Entry<Parser, ParserMetrics> it) -> it.getValue().self_time).reversed())
            .forEach(it -> {
                ParserMetrics v = it.getValue();
                System.out.println(it.getKey() + ": " + Duration.ofNanos(v.self_time)
                    + " / " + Duration.ofNanos(v.total_time)
                    + " / " + String.format("%,d", v.invocations));
            });
    }

    // ---------------------------------------------------------------------------------------------

    public static void main (String[] args) throws IOException
    {
        Benchmark benchmark = new Benchmark();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (DO_TRACE) benchmark.pretty_print_trace();
        }));

        String os = System.getProperty("os.name");

        String corpus_path =
            args.length > 0
                ? args[0]
                // Spring 4.3.2
                : os.equals("Mac OS X")
                    ? "/Users/nilaurent/Documents/bench"
                    : "D:/bench";

        // Some results:
        // Windows: 40s                 (Spring 4.3.2) (35M)
        // Windows: 1m14s   (trace)     (Spring 4.3.2) (35M)
        // Windows: 1m43s   (trace)     (Spring 4.3.2) (35M)
        // OSX: 26s                     (Spring 4.3.2) (35M)
        // OSX: 24.5s       (graalvm)   (Spring 4.3.2) (35M)
        // OSX: 47s         (record)    (Spring 4.3.2) (35M)
        // OSX: 1m57s       (trace)     (Spring 4.3.2) (35M)

        benchmark.run(corpus_path);
    }

    // ---------------------------------------------------------------------------------------------
}
