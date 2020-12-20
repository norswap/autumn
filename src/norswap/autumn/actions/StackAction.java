package norswap.autumn.actions;

import norswap.autumn.DSL;
import norswap.autumn.Log;
import norswap.autumn.Parse;
import norswap.autumn.SideEffect;
import norswap.autumn.SideEffectingArrayStack;
import norswap.autumn.parsers.Collect;
import norswap.autumn.parsers.LeftExpression;
import norswap.autumn.parsers.LeftFold;
import norswap.autumn.parsers.RightExpression;
import norswap.autumn.parsers.RightFold;
import norswap.autumn.positions.Span;

/**
 * An interface for specifying actions on the value stack ({@link Parse#stack}).
 *
 * <p>The basic assumption is that stack actions are passed to some parsers. These parsers want to
 * run a child parser and do something with the items it pushed on the stack. (They can run
 * <i>multiple</i> child parsers, but we'll always refer to "the" child parser for simplicity's
 * sake).
 *
 * <h2>Stack Action Consumers</h2>
 *
 * <p>Autumn itself supplies a few of stack actions: the {@link Collect}, {@link LeftFold}
 * {@link RightFold}, {@link LeftExpression} and {@link RightExpression} parsers.
 *
 * <p>The consumers of stack actions may opt to call the action even if some underlying parser
 * failed. In that case, it should pass {@code null} for the {@code items} parameter in {@link
 * #apply}.
 *
 * <p>It's important that any state change done by these actions be performed through {@link
 * Log#apply(SideEffect)} (or another such {@link Log} method, or a method that already performs
 * change through them, such as some {@link SideEffectingArrayStack} methods).
 *
 * <h2>Sub-Interfaces</h2>
 *
 * <p>The parsers that consume this interface will call {@link #apply(Parse, Object[], int, int)}.
 * However, this method typically calls another one, depending on the sub-interface being used.
 *
 * <p>We provide a few sub-interfaces to {@code StackAction} in the {@link norswap.autumn.actions}
 * package. These allow to get not only objects collected from the stack object, but also the
 * {@link Parse} object, a {@link Span} representing the matched input, and more.
 *
 * <p>These sub-interfaces are what we use in the {@link DSL} builder, for numerous methods of the
 * {@link DSL.rule} class (those starting with {@code collect} and {@code push}, and a couple more
 * besides).
 *
 * <p>The {@link StackPush} and {@link StackPushWithSpan} sub-interfaces require the implementation
 * of a method that returns a value, which they will push back onto the stack.
 *
 * <p>You could also provide provide your own implementations of this class without going through
 * one of these sub-interfaces.
 */
@FunctionalInterface
public interface StackAction
{
    /**
     * The main method that consumer of stack actions will always call.
     *
     * @param items collected items from the stack, or null if the child parser failed.
     * @param pos0 the input position at which the child parser matched.
     * @param size0 the size of the stack before the child parser was called.
     */
    void apply (Parse parse, Object[] items, int pos0, int size0);
}