package norswap.autumn.parsers;

import norswap.autumn.DSL.rule;
import norswap.autumn.Parse;
import norswap.autumn.ParseState;
import norswap.autumn.Parser;
import norswap.autumn.ParserVisitor;
import norswap.autumn.SideEffect;
import java.util.Collections;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Wraps a child parser, matching the same thing it does but memoizing its result.
 *
 * <p>The memoization strategy depends on the implementation of {@link Memoizer} supplied to the
 * constructor. Built-in memoizers implementation are {@link MemoTable} and TODO. Users can also
 * define their own.
 *
 * <p>In a "context-free" situation (no {@link ParseState} being used), the way to memoize is per
 * (parser, input position) pair.
 *
 * <p>A single memoizer can be shared between different {@code Memo} instances (and, potentially,
 * other custom parsers using them) — or one can assign each {@code Memo} instance its own memoizer
 * (in which case it becomes unecessary to check the parser).
 *
 * <p>But, if parse state is being used, and changes the result of the child parser (either
 * resulting in a different input match, or in different {@link SideEffect} being emitted), then
 * it is necessary to verify that the memoized result is still applicable to the current context!
 *
 * <p>This is achieved through the use of an optional (may be null) function passed to the
 * constructor ({@link #context_extractor}), which extracts the relevant part of the context so that
 * it may be stored in the {@link MemoEntry}. The hashcode of the returned object is used to augment
 * the hash of the entry. When retrieving a memoized entry, a freshly extracted context is compared
 * to the context stored in the entry to establish compatibility (therefore the {@link
 * #equals(Object)} implementation of the context object is reponsible to establish context
 * compatibility).
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

    public final Function<Parse, Object> context_extractor;

    // ---------------------------------------------------------------------------------------------

    public Memo (Parser child, Supplier<Memoizer> memoizer, Function<Parse, Object> context_extractor)
    {
        this.child = child;
        this.memoizer = new ParseState<>(this, memoizer);
        this.context_extractor = context_extractor;
    }

    // ---------------------------------------------------------------------------------------------

    @Override protected boolean doparse (Parse parse)
    {
        Object ctx = null;
        int h = 31 * child.hashCode() + parse.pos;
        if (context_extractor != null) {
            ctx = context_extractor.apply(parse);
            h = 31 * h + ctx.hashCode();
        }

        Memoizer memo = memoizer.data(parse);
        MemoEntry entry = memo.get(h, child, parse.pos, ctx);

        if (entry != null)
        {
            if (!entry.matched())
                return false;

            parse.pos = entry.end_position;
            parse.log.apply(entry.delta);
            return true;
        }

        int pos0 = parse.pos;
        int log0 = parse.log.size();

        if (child.parse(parse))
            entry = new MemoEntry(h, child, pos0, parse.pos, parse.log.delta(log0), ctx);
        else
            entry = MemoEntry.no_match(h, child, pos0, ctx);

        memo.memoize(entry);
        return entry.matched();
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void accept (ParserVisitor visitor) {
        visitor.visit(this);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public Iterable<Parser> children () {
        return Collections.singleton(child);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toStringFull () {
        return "memo(" + child + ")";
    }

    // ---------------------------------------------------------------------------------------------
}
