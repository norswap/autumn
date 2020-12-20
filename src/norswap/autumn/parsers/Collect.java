package norswap.autumn.parsers;

import norswap.autumn.Parse;
import norswap.autumn.Parser;
import norswap.autumn.ParserVisitor;
import norswap.autumn.actions.StackAction;
import java.util.Collections;

/**
 * Matches its child and, if it succeeds (or {@link #action_on_fail} is true), collects all the
 * items it added to {@link Parse#stack} and passes them to a user-defined action, optionally along
 * with the input matched by the child.
 *
 * <p>Actions are specified by the interface {@link StackAction}.
 *
 * <p>The {@code pop} constructor parameter controls whether the collected items are popped from
 * the stack. The items are popped if and only if {@code reduce == true}.
 *
 * <p>The {@code action_on_fail} constructor parameter controls whether the action should succeed
 * even when the child parser fails. In that case, the collect parser always succeeds.
 *
 * <p>The {@code lookback} constructor parameter enables getting additional items from the stack
 * to be prepended to the collected items. See {@link #lookback} for more details.
 */
public final class Collect extends Parser
{
    // ---------------------------------------------------------------------------------------------

    /**
     * The display name for this parser, if {@link #set_rule(String)} hasn't been called.
     */
    public String name;

    // ---------------------------------------------------------------------------------------------

    public final Parser child;

    // ---------------------------------------------------------------------------------------------

    /**
     * The action to be applied using the items pushed on the stack during the execution
     * of the child parser.
     */
    public final StackAction action;

    // ---------------------------------------------------------------------------------------------

    /**
     * Indicates a number of items to get from the top of the stack and preprend to the start
     * of the items array. These items will be popped if {@link #pop} is true. This could
     * result in an exception being thrown if there are not enough items on the stack.
     */
    public final int lookback;

    // ---------------------------------------------------------------------------------------------

    /**
     * Indicates whether the stack items passed to the action should be popped from the stack
     * (true) or left there (false).
     */
    public final boolean pop;

    // ---------------------------------------------------------------------------------------------

    /**
     * Whether to apply the action and succeed whenever the child parser fails.
     */
    public final boolean action_on_fail;

    // ---------------------------------------------------------------------------------------------

    public Collect (String name, Parser child,
                    int lookback, boolean action_on_fail, boolean pop, StackAction action)
    {
        if (lookback < 0)
            throw new IllegalArgumentException("negative lookback");

        this.name = name;
        this.child = child;
        this.lookback = lookback;
        this.pop = pop;
        this.action_on_fail = action_on_fail;
        this.action = action;
    }

    // ---------------------------------------------------------------------------------------------

    private static final Object MARKER = new Object();

    // ---------------------------------------------------------------------------------------------

    @Override public boolean doparse (Parse parse)
    {
        int pos0 = parse.pos;
        int size0 = parse.stack.size();
        boolean result = child.parse(parse);

        if (!result && !action_on_fail)
            return false;

        Object[] items = result
            ? pop
                ? parse.stack.pop_from(size0 - lookback)
                : parse.stack.peek_from(size0 - lookback, Object[]::new)
            : null;

        action.apply(parse, items, pos0, size0);
        return true;
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
        return name + "(" + child + ")";
    }

    // ---------------------------------------------------------------------------------------------
}