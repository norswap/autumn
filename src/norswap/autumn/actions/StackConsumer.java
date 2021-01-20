package norswap.autumn.actions;

/**
 * A {@link StackAction} sub-interface used to specify actions on the {@link ActionContext} without
 * returning any value.
 */
@FunctionalInterface
public interface StackConsumer extends StackAction
{
    @Override default boolean apply (ActionContext context) {
        action(context);
        return true;
    }

    void action (ActionContext context);
}
