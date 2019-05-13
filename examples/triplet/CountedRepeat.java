package triplet;

import norswap.autumn.DSL;
import norswap.autumn.Parse;
import norswap.autumn.ParseState;
import norswap.autumn.Parser;
import norswap.autumn.ParserVisitor;
import norswap.utils.Slot;
import java.util.Collections;

/**
 * This parser matches its child parser as many times as the number of repetition stored in
 * a parse state with key {@code CountingRepeat.class} (not {@code CounterRepeat}!).
 *
 * <p>This parse state must have been written by a {@link CountingRepeat} parser beforehand or
 * an exception will ensue!
 */
final class CountedRepeat extends Parser
{
    public final Parser child;
    public final ParseState<Slot<Integer>> repetitions
        = new ParseState<>(CountingRepeat.class, Slot::new); // CountING!

    public CountedRepeat (Parser child) {
        this.child = child;
    }

    public static DSL.rule with (DSL.rule child) {
        return child.dsl().rule(new CountedRepeat(child.get()));
    }

    @Override protected boolean doparse (Parse parse)
    {
        Slot<Integer> slot = repetitions.data(parse);

        if (slot.x == null)
            throw new IllegalStateException(
                "Invoking a CountedRepeat without a preceding CountingRepeat invocation.");

        for (int i = 0; i < slot.x; ++i)
            if (!child.parse(parse))
                return false;
        return true;
    }

    @Override public void accept (ParserVisitor visitor) {
        ((ParserVisitorTriplet) visitor).visit(this);
    }

    @Override public Iterable<Parser> children () {
        return Collections.singleton(child);
    }

    @Override public String toStringFull () {
        return "counted_repeat(" + child + ")";
    }
}