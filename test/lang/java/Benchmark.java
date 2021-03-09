package lang.java;

import norswap.autumn.Autumn;
import norswap.autumn.AutumnTestFixture;
import norswap.autumn.Grammar;
import norswap.autumn.ParseOptions;
import norswap.autumn.ParseResult;
import norswap.autumn.ParseMetrics;
import norswap.lang.java.JavaGrammar;
import norswap.lang.java.JavaGrammarTokens;
import norswap.lang.java.Lexer;
import norswap.lang.java.Token;
import norswap.utils.IO;
import norswap.utils.NFiles;
import norswap.utils.exceptions.Exceptions;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public final class Benchmark
    extends AutumnTestFixture // for diagnostics in case of failure!
{
    // ---------------------------------------------------------------------------------------------

    private static final boolean DO_TRACE = false;
    private static final boolean DO_RECORD = false;
    private static final boolean LOG_PERCENT = true;
    private static final int iterCount = 1;

    // ---------------------------------------------------------------------------------------------

    private final ParseMetrics parseMetrics = new ParseMetrics();
    private final String config;

    // ---------------------------------------------------------------------------------------------

    public Benchmark (String config) {
        this.config = config;
        if (config.equals("tokens"))
            this.lexer = string -> Arrays.asList(new Lexer(string).lex());
    }

    // ---------------------------------------------------------------------------------------------

    public void run (String corpusPath, Grammar grammar) throws IOException
    {
        final List<Path> paths = NFiles.glob("**/*.java", Paths.get(corpusPath));
        final int slices = 100;
        final int sliceSize = (paths.size() + slices - 1) / slices;

        this.rule = grammar.root(); // for success(input) call

        long time = 0L;
        int nextSlice = sliceSize;
        int percentage;
        int i = 0;

        long size = 0;

        // Perform well-formed check + name assignment only once!
        Autumn.parse(grammar, "class Test {}", ParseOptions.get());

        ParseOptions options = ParseOptions
            .wellFormednessCheck(false)
            .recordCallStack(DO_RECORD)
            .metrics(() -> parseMetrics)
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
                result = Autumn.parse(grammar, tokens, options);
            } else {
                result = Autumn.parse(grammar, input, options);
            }

            time += System.nanoTime() - t0;

            if (!result.fullMatch)
            {
                System.out.println(i + "/" + paths.size() + " -> " + path);
                try {
                    this.inputName = path.toString();
                    success(input);
                } catch (AssertionError e) {
                    System.out.println(e.getMessage());
                }
                break;
            }

            if (i >= nextSlice)
            {
                percentage = (int) (100.0 * i / paths.size());
                if (LOG_PERCENT)
                    System.out.println(percentage + "% (" + i + "/" + paths.size() + ")");
                nextSlice += sliceSize;
            }
        }

        System.out.println("Number of files: " + paths.size());
        System.out.println("Total size in bytes: " + String.format("%,d", size));
        System.out.println("Code parsed in: " + Duration.ofNanos(time));
        if (DO_TRACE) System.out.println(parseMetrics);
    }

    // ---------------------------------------------------------------------------------------------

    public static void main (String[] args) throws IOException
    {
        String config = args[0];
        String corpusPath = args[1];
        Grammar grammar =
            config.equals("normal")
                ? new JavaGrammar()
            : config.equals("tokens")
                ? new JavaGrammarTokens()
            : Exceptions.exprThrow(
                new IllegalArgumentException("unknown benchmark config: " + config));

        Benchmark benchmark = new Benchmark(config);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (DO_TRACE) System.out.println(benchmark.parseMetrics);
        }));

        // System.in.read(); // wait to attach VisualVM or some other tool
        for (int i = 0; i < iterCount; ++i)
            benchmark.run(corpusPath, grammar);
    }

    // ---------------------------------------------------------------------------------------------
}