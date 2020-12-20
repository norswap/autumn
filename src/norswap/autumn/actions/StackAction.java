package norswap.autumn.actions;

import norswap.autumn.Log;
import norswap.autumn.Parse;
import norswap.autumn.SideEffect;
import norswap.autumn.SideEffectingArrayStack;
import norswap.autumn.parsers.Collect;
import norswap.autumn.parsers.LeftExpression;
import norswap.autumn.parsers.LeftFold;
import norswap.autumn.parsers.RightExpression;
import norswap.autumn.parsers.RightFold;

/**
 * An interface for specifying actions on the value stack ({@link Parse#stack}).
 *
 * <p>The basic assumption is that stack actions are passed to some parsers. These parsers want to
 * run a child parser and do something with the items it pushed on the stack. (They can run
 * <i>multiple</i> child parsers, but we'll always refer to "the" child parser for simplicity's
 * sake).
 *
 * <p>It's important that any state change done by these actions be performed through {@link
 * Log#apply(SideEffect)} (or another such {@link Log} method, or a method that already performs
 * change through them, such as some {@link SideEffectingArrayStack} methods).
 *
 * <h2>Stack Action Consumers</h2>
 *
 * <p>Autumn itself supplies a few of stack actions: the {@link Collect}, {@link LeftFold}
 * {@link RightFold}, {@link LeftExpression} and {@link RightExpression} parsers.
 *
 * <p>To execute the action, consumers must construct an {@link ActionContext} object. Refer
 * to its javadoc for more details.
 *
 * @see StackPush for a StackAction that automatically pushes a value onto the value stack.
 */
@FunctionalInterface
public interface StackAction
{
    /**
     * The method called by stack action consumers to trigger the action.
     */
    void apply (ActionContext context);
}