package norswap.autumn.actions;

import norswap.autumn.Parse;

/**
 * An action that is supplied only with an array of items that have been pushed on the value
 * stack ({@link Parse#stack}), typically those pushed there by the sub-parser(s) of the
 * action's consumer. This action must return a value which is automatically pushed on the value
 * stack.
 */
@FunctionalInterface public interface StackPush extends StackAction
{
    @Override default void apply (Parse parse, Object[] items, int pos0, int size0) {
        parse.stack.push(get(items));
    }

    Object get (Object[] items);
}
