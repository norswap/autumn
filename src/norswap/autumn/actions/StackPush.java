package norswap.autumn.actions;

import norswap.autumn.Parse;

/**
 * A {@link StackAction} sub-interface setup such that you have to implement a method that returns
 * a value. This method is run as part of the action, and the value it returns is pushed onto
 * the {@link Parse#stack value stack}.
 */
@FunctionalInterface
public interface StackPush extends StackAction
{
    @Override default boolean apply (ActionContext context) {
        context.push(get(context));
        return true;
    }

    Object get (ActionContext context);
}
