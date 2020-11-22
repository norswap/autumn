package lang.java;

import norswap.autumn.Autumn;
import norswap.autumn.DSL;
import norswap.autumn.ParseOptions;
import norswap.autumn.ParseResult;
import norswap.autumn.Parser;
import norswap.autumn.ParserMetrics;
import norswap.autumn.TestFixture;
import norswap.autumn.ParseMetrics;
import norswap.lang.java.Grammar;
import norswap.lang.java.GrammarFast;
import norswap.lang.java.GrammarTokens;
import norswap.lang.java.Lexer;
import norswap.lang.java.Token;
import norswap.utils.IO;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class Benchmark
    extends TestFixture // for diagnostics in case of failure!
{
    // ---------------------------------------------------------------------------------------------

    private static final boolean DO_TRACE = false;
    private static final boolean DO_RECORD = false;
    private static final boolean LOG_PERCENT = true;
    private static final int iter_count = 1;

    // ---------------------------------------------------------------------------------------------

    private final ParseMetrics parse_metrics = new ParseMetrics();
    private final String config;

    // ---------------------------------------------------------------------------------------------

    public Benchmark (String config) {
        this.config = config;
    }

    // ---------------------------------------------------------------------------------------------

    public void run (String corpus_path, DSL.rule root) throws IOException
    {
        final List<Path> paths = IO.glob("**/*.java", Paths.get(corpus_path));
        final int slices = 100;
        final int slice_size = (paths.size() + slices - 1) / slices;

        this.rule = root; // for success(input) call

        long time = 0L;
        int next_slice = slice_size;
        int percentage;
        int i = 0;

        long size = 0;

        // Perform well-formed check only once!
        Autumn.parse(root, "class Test {}", ParseOptions.get());

        ParseOptions options = ParseOptions
            .well_formedness_check(false)
            .record_call_stack(DO_RECORD)
            .metrics(() -> parse_metrics)
            .trace(DO_TRACE)
            .get();

        for (Path path: paths)
        {
            ++i;
            // System.out.println(i + " / " + path);
            long t0 = System.nanoTime();
            String input = IO.slurp(""+ path);
            size += path.toFile().length();
            ParseResult result;

            if (config.equals("tokens")) {
                Lexer lexer = new Lexer(input);
                List<Token> tokens = Arrays.asList(lexer.lex());
                 result = Autumn.parse(root, tokens, options);
            } else {
                result = Autumn.parse(root, input, options);
            }

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
                if (LOG_PERCENT)
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
        String config = args[0];
        String corpus_path = args[1];
        DSL.rule root =
            config.equals("normal")
                ? new Grammar().root
            : config.equals("fast")
                ? new GrammarFast().root
            : config.equals("tokens")
                ? new GrammarTokens().root
                : null;

        if (root == null)
            // TODO make a throwing function in norswap-utils and inline this in the condition above
            throw new IllegalArgumentException("unknown benchmark config: " + config);

        Benchmark benchmark = new Benchmark(config);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (DO_TRACE) benchmark.pretty_print_trace();
        }));

        // NOTE(norswap): In November 2020, this run in Xs over the source of Spring 5.1.8 on my
        // 2019 2.6Ghz MacBook Pro (to give an order of magnitude).
        // where X:
        // = 18.5s using Grammar
        // = 12.5s using GrammarFast

        // System.in.read(); // wait to attach VisualVM or some other tool
        for (int i = 0; i < iter_count; ++i)
            benchmark.run(corpus_path, root);
    }

    // ---------------------------------------------------------------------------------------------
}