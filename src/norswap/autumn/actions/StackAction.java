package norswap.autumn.actions;

import norswap.autumn.Log;
import norswap.autumn.Parse;
import norswap.autumn.SideEffect;
import norswap.autumn.SideEffectingArrayStack;
import norswap.autumn.parsers.Collect;
import norswap.autumn.parsers.LeftExpression;
import norswap.autumn.parsers.RightExpression;

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
 * <p>In practice, you will most often instantiate this interface by using one of the three
 * functional interfaces that inherit it: {@link StackConsumer} (plain action), {@link StackPush}
 * (returns a value which is automatically pushed onto the value stack), and {@link StackPredicate}
 * (control whether the consumer should fail).
 *
 * <h2>Stack Action Consumers</h2>
 *
 * <p>Autumn itself supplies a few of stack actions: the {@link Collect}, {@link LeftExpression} and
 * {@link RightExpression} parsers.
 *
 * <p>To execute the action, consumers must construct an {@link ActionContext} object. Refer
 * to its javadoc for more details.
 *
 * <p>Stack action consumer <b>must</b> call the action's {@link #apply(ActionContext)} method,
 * not {@code action} or another sub-interface method.</p>
 *
 * <h2>Parser Success Control</h2>
 *
 * <p>{@link #apply(ActionContext)} returns a boolean. If it is false, it is intended to signify
 * to the action consumer (a parser) that it should fail.
 *
 * <p>This return value exists for the benefit of the {@link StackPredicate} sub-interface.
 * Other sub-interfaces always return {@code true}.
 */
public interface StackAction
{
    /**
     * The method called by stack action consumers to trigger the action. The returned
     * boolean controls the success of the consummer (a parser). See {@link StackAction} for
     * more information.
     */
    boolean apply (ActionContext context);
}