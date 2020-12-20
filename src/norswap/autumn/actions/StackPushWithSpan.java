package norswap.autumn.actions;

import norswap.autumn.Parse;
import norswap.autumn.positions.Span;

/**
 * An action that is supplied with the following items, related to the execution of the parser
 * that consumes the action:
 * <ul>
 *     <li>the {@link Parse} object
 *     <li>an array of items that have been pushed on the {@link Parse#stack value stack} by the
 *     parser</li>
 *     <li>a {@link Span} object representing the input matched by the parser</li>
 * </ul>
 *
 * <p>Additionally, this action must return a value which is automatically pushed on the value
 * stack.
 */
@FunctionalInterface
public interface StackPushWithSpan extends StackAction
{
    @Override default void apply (Parse parse, Object[] items, int pos0, int size0) {
        Span match = items != null ? new Span(pos0, parse.pos) : null;
        parse.stack.push(get(parse, items, match));
    }

    Object get (Parse parse, Object[] items, Span match);
}
