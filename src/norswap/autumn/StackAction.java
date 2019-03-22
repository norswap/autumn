package norswap.autumn;

import norswap.autumn.parsers.LeftAssoc;
import norswap.autumn.parsers.RightAssoc;
import java.util.List;

/**
 * An interface for specifying actions on the value stack ({@link Parse#stack}).
 *
 * <p>The basic assumption is that stack actions are passed to some parsers. These parsers want to
 * run a child parser and do something with the items it pushed on the stack. (They can run
 * <i>multiple</i> child parsers, but we'll always refer to "the" child parser for simplicity's
 * sake).
 *
 * <p>Autumn itself supplies three consumers of stack actions: the {@link
 * norswap.autumn.parsers.Collect}, {@link LeftAssoc} and {@link RightAssoc} parsers.
 *
 * <p>It's important that any state change done by these actions be performed through {@link
 * Log#apply(SideEffect)} (or another such {@link Log} method, or a method that already performs
 * change through them, such as some {@link SideEffectingArrayStack} methods).
 *
 * <p>The parsers that consume this interface will call {@link #apply(Parse, Object[], int, int)}.
 * However, this method typically calls another one, depending on the sub-interface being used.
 *
 * <p>We provide six sub-interfaces: {@link Collect}, {@link CollectWithString}, {@link
 * CollectWithList}, {@link Push}, {@link PushWithString}, {@link PushWithList}. See their
 * respective documentation for more information.
 *
 * <p>These sub-interfaces are what we use in the {@link DSL} builder, for numerous methods of the
 * {@link DSL.rule} class (those starting with {@code collect} and {@code push}, and a couple more
 * besides.
 *
 * <p>You could also provide provide your own implementations of this class without going through
 * one of these sub-interfaces.
 */
public interface StackAction
{
    // ---------------------------------------------------------------------------------------------

    /**
     * The main method that consumer of stack actions will always call.
     *
     * @param items collected items from the stack, or null if the child parser failed.
     * @param pos0 the input position at which the child parser matched.
     * @param size0 the size of the stack before the child parser was called.
     */
    void apply (Parse parse, Object[] items, int pos0, int size0);

    // ---------------------------------------------------------------------------------------------

    /**
     * An action that is supplied with the {@link Parse} object as well as an array of items that
     * have been pushed on the value stack ({@link Parse#stack}), typically those pushed there by
     * the sub-parser(s) of the action's consumer.
     */
    @FunctionalInterface
    interface Collect extends StackAction
    {
        @Override default void apply (Parse parse, Object[] items, int pos0, int size0) {
            apply(parse, items);
        }

        /**
         * @param items collected items from the stack, or null if the child parser failed.
         */
        void apply (Parse parse, Object[] items);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * An action that is supplied with the {@link Parse} object as well as an array of items that
     * have been pushed on the value stack ({@link Parse#stack}), and part of the {@link
     * Parse#string} input.
     *
     * <p>Typically the items are those pushed by the sub-parser(s) of the action's consumer, and
     * the string is the input it matched.
     */
    @FunctionalInterface
    interface CollectWithString extends StackAction
    {
        @Override default void apply (Parse parse, Object[] items, int pos0, int size0)
        {
            assert parse.string != null;
            apply(parse, items != null ? parse.string.substring(pos0, parse.pos) : null, items);
        }

        /**
         * @param match part of {@link Parse#string} matched by the child parser.
         * @param items collected items from the stack, or null if the child parser failed.
         */
        void apply (Parse parse, String match, Object[] items);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * An action that is supplied with the {@link Parse} object as well as an array of items that
     * have been pushed on the value stack ({@link Parse#stack}), and part of the {@link Parse#list}
     * input.
     *
     * <p>Typically the items are those pushed by the sub-parser(s) of the action's consumer, and
     * the list is the input it matched.
     */
    @FunctionalInterface
    interface CollectWithList extends StackAction
    {
        @Override default void apply (Parse parse, Object[] items, int pos0, int size0)
        {
            assert parse.list != null;
            apply(parse, items != null ? parse.list.subList(pos0, parse.pos) : null, items);
        }

        /**
         * @param match part of {@link Parse#list} matched by the child parser.
         * @param items collected items from the stack, or null if the child parser failed.
         */
        void apply (Parse parse, List<?> match, Object[] items);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * An action that is supplied with the {@link Parse} object as well as an array of items that
     * have been pushed on the value stack ({@link Parse#stack}), typically those pushed there by
     * the sub-parser(s) of the action's consumer. This action must return a value which is
     * automatically pushed on the value stack.
     */
    @FunctionalInterface
    interface Push extends StackAction
    {
        @Override default void apply (Parse parse, Object[] items, int pos0, int size0) {
            parse.stack.push(get(parse, items));
        }

        Object get (Parse parse, Object[] items);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * An action that is supplied with the {@link Parse} object as well as an array of items that
     * have been pushed on the value stack ({@link Parse#stack}), and part of the {@link
     * Parse#string} input. This action must return a value which is automatically pushed on the
     * value stack.
     *
     * <p>Typically the items are those pushed by the sub-parser(s) of the action's consumer, and
     * the string is the input it matched.
     */
    @FunctionalInterface
    interface PushWithString extends StackAction
    {
        @Override default void apply (Parse parse, Object[] items, int pos0, int size0) {
            parse.stack.push(get(parse, items != null ? parse.string.substring(pos0, parse.pos) : null, items));
        }

        Object get (Parse parse, String match, Object[] items);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * An action that is supplied with the {@link Parse} object as well as an array of items that
     * have been pushed on the value stack ({@link Parse#stack}), and part of the {@link Parse#list}
     * input. This action must return a value which is automatically pushed on the value stack.
     *
     * <p>Typically the items are those pushed by the sub-parser(s) of the action's consumer, and
     * the string is the input it matched.
     */
    @FunctionalInterface
    interface PushWithList extends StackAction
    {
        @Override default void apply (Parse parse, Object[] items, int pos0, int size0) {
            parse.stack.push(get(parse, items != null ? parse.list.subList(pos0, parse.pos) : null, items));
        }

        Object get (Parse parse, List<?> match, Object[] items);
    }

    // ---------------------------------------------------------------------------------------------
}