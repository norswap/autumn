package norswap.autumn.parsers;

import norswap.autumn.Grammar.rule;
import norswap.autumn.Parse;
import norswap.autumn.ParseState;
import norswap.autumn.Parser;
import norswap.autumn.ParserVisitor;
import norswap.autumn.SideEffect;
import norswap.autumn.memo.*;
import java.util.Collections;
import java.util.function.Function;

/**
 * Wraps a child parser, matching the same thing it does but memoizing its result.
 *
 * <p>The memoization strategy depends on the implementation of {@link Memoizer} supplied to the
 * constructor. Built-in memoizers implementation are {@link MemoTable} and {@link MemoCache}.
 * Users can also define their own.
 *
 * <p>The results of the child parser will be memoized based on the input position and an optional
 * context object, and potentially on the parser itself, depending on the supplied memoizer (this is
 * useful if you want to share a single memoizer between multiple {@code Memo} instances).
 *
 * <p>If parse state is being used, and changes the result of the child parser (either resulting in
 * a different input match, or in different {@link SideEffect} being emitted), then it is necessary
 * to verify that the memoized result is still applicable to the current context!
 *
 * <p>This is achieved through the use of an optional (may be null) function passed to the
 * constructor ({@link #contextExtractor}), which extracts the relevant part of the context.
 * Context objects are extracted each time the memo parser is called, and will be compared (using
 * its {@link Object#hashCode()} and {@link Object#equals(Object)} methods) to the context object
 * stored in a {@link MemoEntry} to determine if the entry is compatible with the current context.
 *
 * <p>If the function is null, no context comparisons are performed.
 *
 * <p>Build with {@link rule#memo(int)} or {@link rule#memo(int, Function)}.
 */
public final class Memo extends Parser
{
    // ---------------------------------------------------------------------------------------------

    public final Parser child;

    // ---------------------------------------------------------------------------------------------

    public final ParseState<Memoizer> memoizer;

    // ---------------------------------------------------------------------------------------------

    public final Function<Parse, Object> contextExtractor;

    // ---------------------------------------------------------------------------------------------

    public Memo (
        Parser child, ParseState<Memoizer> memoizer, Function<Parse, Object> contextExtractor)
    {
        this.child = child;
        this.memoizer = memoizer;
        this.contextExtractor = contextExtractor;
    }

    // ---------------------------------------------------------------------------------------------

    @Override protected boolean doparse (Parse parse)
    {
        Object ctx = contextExtractor != null ? contextExtractor.apply(parse) : null;
        Memoizer memo = memoizer.data(parse);
        MemoEntry entry = memo.get(child, parse.pos, ctx);

        if (entry != null)
        {
            if (!entry.succeeded())
                return false;

            parse.pos = entry.endPosition;
            parse.log.apply(entry.delta);
            return true;
        }

        int pos0 = parse.pos;
        int log0 = parse.log.size();

        entry = new MemoEntry(
            child.parse(parse), child, pos0, parse.pos, parse.log.delta(log0), ctx);

        memo.memoize(entry);
        return entry.succeeded();
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void accept (ParserVisitor visitor) {
        visitor.visit(this);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public Iterable<Parser> children() {
        return Collections.singleton(child);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toStringFull() {
        return "memo(" + child + ")";
    }

    // ---------------------------------------------------------------------------------------------
}
