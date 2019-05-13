package triplet;

import norswap.autumn.DSL;
import norswap.autumn.Parse;
import norswap.autumn.ParseState;
import norswap.autumn.Parser;
import norswap.autumn.ParserVisitor;
import norswap.utils.Slot;
import java.util.Collections;

/**
 * This parser matches its child parsersas many time as possible, then saves the number of matched
 * repetitions in a parse state with key {@code CountingRepeat.class}, so that a {@link
 * CountedRepeat} may use it to match another parser for the same number of repetitions.
 */
public final class CountingRepeat extends Parser
{
    public final Parser child;
    public final ParseState<Slot<Integer>> repetitions
        = new ParseState<>(CountingRepeat.class, Slot::new);

    public CountingRepeat (Parser child) {
        this.child = child;
    }

    public static DSL.rule with (DSL.rule child) {
        return child.dsl().rule(new CountingRepeat(child.get()));
    }

    @Override protected boolean doparse (Parse parse)
    {
        int repetitions0 = 0;

        while (child.parse(parse))
            repetitions0++;

        if (repetitions0 == 0) return false;
        final int repetitions1 = repetitions0;

        parse.log.apply(() -> {
            Slot<Integer> slot = repetitions.data(parse);
            Integer old = slot.x;
            slot.x = repetitions1;
            return () -> slot.x = old;
        });

        return true;
    }

    @Override public void accept (ParserVisitor visitor) {
        ((ParserVisitorTriplet) visitor).visit(this);
    }

    @Override public Iterable<Parser> children () {
        return Collections.singleton(child);
    }

    @Override public String toStringFull () {
        return "counting_repeat(" + child + ")";
    }
}

/* *************************************************************************************************
// Alternative implementation as an AbstractForwarding (for string matches only)

public final class Count extends AbstractForwarding
{
    public final ParseState<Slot<Integer>> repetitions
        = new ParseState<>(CountingRepeat.class, Slot::new);

    public CountingRepeat (String string) {
        super("counting_repeat", str(string).at_least(1).collect_with_string(
            (p, xs, repeated_string) -> p.log.apply(() -> {
                Slot<Integer> slot = repetitions.data(p);
                Integer old = slot.x;
                slot.x = repeated_string.length() / string.length();
                return () -> slot.x = old;
            })));

        if (string.isEmpty())
            throw new IllegalArgumentException("Repeated string can't be empty.");
    }
}

************************************************************************************************* */