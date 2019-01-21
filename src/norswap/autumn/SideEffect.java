package norswap.autumn;

/**
 * A side effect is a function that modifies some state and returns a function that will undo
 * this modification if called.
 *
 * <p>In general, you should never call side-effects yourself (just pass them to {@link Log}).
 *
 * <p>The functional method is {@link #__apply()}, but {@link Log} will call {@link #apply()}, in
 * order to store both the side-effect and its undo function. Storing the side-effect is notably
 * needed for {@link Log#delta(int)}.
 *
 * <p>The reason why a side effect must return an undo function upon application (instead of the
 * undo function being supplied once and for all) is that a specific application of the side effect
 * may need to save some data for the undo function to access. Typically this will be achieved
 * through lambda capture. For instance, {@link SideEffectingArrayStack#pop()} uses:
 *
 * <pre>
 * {@code
 * log.apply(() -> {
 *     Object x = super.pop();
 *     return () -> super.push(x);
 * });
 * }
 * </pre>
 */
@FunctionalInterface
public interface SideEffect
{
    // ---------------------------------------------------------------------------------------------

    /**
     * Modifies some state and returns a function that will undo this modification if called.
     */
    Runnable __apply();

    // ---------------------------------------------------------------------------------------------

    /**
     * Calls {@link #__apply()} and creates an {@link Applied} from the result.
     */
    default Applied apply()
    {
        Runnable undo = __apply();
        return new Applied(this, undo);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * A pair comprising a {@link SideEffect} that was called, and the undo function it returned.
     */
    final class Applied
    {
        public final SideEffect effect;
        public final Runnable undo;

        private Applied (SideEffect effect, Runnable undo) {
            this.effect = effect;
            this.undo = undo;
        }
    }

    // ---------------------------------------------------------------------------------------------
}
