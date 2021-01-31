package norswap.autumn.parsers;

import norswap.autumn.Grammar;
import norswap.autumn.Parse;
import norswap.autumn.Parser;
import norswap.autumn.ParserVisitor;
import java.util.Collections;
import java.util.function.Supplier;

/**
 * Returns the same result as the parser returned by a supplier function.
 * The supplier function will be called <b>at most once</b>, when this parser is first called
 * or walked through.
 *
 * <p>The goal of this parser is to enable recursive and forward parser references when those are
 * stored as fields. However beware that field names need to be fully qualified (e.g. {@code
 * this.myparser} or {@code MyClass.myparser} or you will get an "Illegal self/forward-refrence"
 * error at compile-time.
 *
 * <p>Valid example: {@code Parser x = new LazyParser(() -> this.y); Parser y = new StringMatch("xxx");}
 *
 * <p>Beware that it is unsafe to print this parser whenever its supplier is unable to provide
 * a correct parser yet (typically because the referenced parser hasn't been initialized yet).
 *
 * <p>Build with {@link Grammar#lazy(Supplier)}.
 */
public final class LazyParser extends Parser
{
    // ---------------------------------------------------------------------------------------------

    public final Supplier<Parser> supplier;

    // ---------------------------------------------------------------------------------------------

    private Parser parser;

    // ---------------------------------------------------------------------------------------------

    public Parser child() {
        if (parser == null)
            parser = supplier.get();

        return parser;
    }

    // ---------------------------------------------------------------------------------------------

    public LazyParser (Supplier<Parser> supplier)
    {
        this.supplier = supplier;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public boolean doparse (Parse parse)
    {
        return child().parse(parse);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void accept (ParserVisitor visitor) {
        visitor.visit(this);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public Iterable<Parser> children() {
        return Collections.singleton(child());
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toStringFull() {
        return "lazy(" + child() + ")";
    }

    // ---------------------------------------------------------------------------------------------
}