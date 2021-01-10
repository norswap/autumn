package norswap.autumn;

import norswap.autumn.util.ArrayStack;
import norswap.utils.data.wrappers.Slot;
import java.util.ArrayList;
import java.util.function.IntFunction;


/**
 * A stack in which <b>some</b> mutating operations produce <i>side-effecting</i> results, namely:
 *
 * <ul>
 *     <li>{@link #push(Object)}</li>
 *     <li>{@link #pop()}</li>
 *     <li>{@link #pop(int)}</li>
 *     <li>{@link #popFrom(int)}</li>
 * </ul>
 *
 * <p>The stack should only be mutated through these operations, or it won't be safe
 * to use during a parser!
 *
 * <p>A <i>side-effecting</i> operation is one where a {@link SideEffect.Applied} is pushed onto {@link
 * Parse#log} to represent a state mutation, enabling it to be undone in case of parser
 * backtracking.
 *
 * <p>Norswap's note: in the long run it would be good if we overrode every single mutating method
 * of {@link ArrayStack} and {@link ArrayList} and made them side-effecting. For now, it will have
 * to wait.
 */
public final class SideEffectingArrayStack extends ArrayStack<Object>
{
    // ---------------------------------------------------------------------------------------------

    protected final Log log;

    // ---------------------------------------------------------------------------------------------

    public SideEffectingArrayStack (Log log) {
        this.log = log;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Side-effecting version of {@link ArrayStack#push(Object)}.
     */
    @Override public void push (Object item)
    {
        log.apply(() -> {
            super.push(item);
            return super::pop;
        });
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Side-effecting version of {@link ArrayStack#pop()}.
     */
    @Override public Object pop()
    {
        Object out = super.peek();
        log.apply(() -> {
            Object x = super.pop();
            return () -> super.push(x);
        });
        return out;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Side-effecting version of {@link ArrayStack#pop(int, IntFunction)}.
     */
    public Object[] pop (int amount)
    {
        Slot<Object[]> slot = new Slot<>();
        log.apply(() -> {
            Object[] x = super.pop(amount, Object[]::new);
            slot.x = x; // useless after first application
            return () -> super.push(x);
        });
        return slot.x;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Side-effecting ersion of {@link ArrayStack#popFrom(int, IntFunction)}.
     *
     * <p>The registered side-effect will remember the amount to pop, not the specific index
     * passed to the function, which is generally the desired semantics.
     */
    public Object[] popFrom (int index)
    {
        return pop(size() - index);
    }

    // ---------------------------------------------------------------------------------------------
}
