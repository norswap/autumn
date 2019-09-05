package norswap.autumn;

import norswap.autumn.parsers.LeftFold;
import norswap.autumn.parsers.RightFold;
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
 * norswap.autumn.parsers.Collect}, {@link LeftFold} and {@link RightFold} parsers.
 *
 * <p>It's important that any state change done by these actions be performed through {@link
 * Log#apply(SideEffect)} (or another such {@link Log} method, or a method that already performs
 * change through them, such as some {@link SideEffectingArrayStack} methods).
 *
 * <p>The parsers that consume this interface will call {@link #apply(Parse, Object[], int, int)}.
 * However, this method typically calls another one, depending on the sub-interface being used.
 *
 * <p>We provide seven sub-interfaces: {@link ActionWithParse}, {@link ActionWithString}, {@link
 * ActionWithList}, {@link Push}, {@link PushWithParse}, {@link PushWithString}, {@link
 * PushWithList}. See their respective documentation for more information.
 *
 * <p>These sub-interfaces are what we use in the {@link DSL} builder, for numerous methods of the
 * {@link DSL.rule} class (those starting with {@code collect} and {@code push}, and a couple more
 * besides).
 *
 * <p>Note that all {@code Push*} sub-interfaces extend {@link Push}. Many methods in {@link
 * DSL} accept a {@link Push}, and if you want to use a lambda that represents another {@code
 * Push*} sub-interface, you should use the methods {@link DSL#with_parse}, {@link DSL#with_string}
 * or {@link DSL#with_list} to hint the compiler about which type to use.
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
    interface ActionWithParse extends StackAction
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
    interface ActionWithString extends StackAction
    {
        @Override default void apply (Parse parse, Object[] items, int pos0, int size0)
        {
            assert parse.string != null;
            apply(parse, items, items != null ? new String(parse.string, pos0, parse.pos - pos0) : null);
        }

        /**
         * @param items collected items from the stack, or null if the child parser failed.
         * @param match part of {@link Parse#string} matched by the child parser.
         */
        void apply (Parse parse, Object[] items, String match);
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
    interface ActionWithList extends StackAction
    {
        @Override default void apply (Parse parse, Object[] items, int pos0, int size0)
        {
            assert parse.list != null;
            apply(parse, items, items != null ? parse.list.subList(pos0, parse.pos) : null);
        }

        /**
         * @param items collected items from the stack, or null if the child parser failed.
         * @param match part of {@link Parse#list} matched by the child parser.
         */
        void apply (Parse parse, Object[] items, List<?> match);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * An action that is supplied only with an array of items that have been pushed on the value
     * stack ({@link Parse#stack}), typically those pushed there by the sub-parser(s) of the
     * action's consumer. This action must return a value which is automatically pushed on the value
     * stack.
     */
    @FunctionalInterface
    interface Push extends StackAction
    {
        @Override default void apply (Parse parse, Object[] items, int pos0, int size0) {
            parse.stack.push(get(items));
        }

        Object get (Object[] items);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * An action that is supplied with the {@link Parse} object as well as an array of items that
     * have been pushed on the value stack ({@link Parse#stack}), typically those pushed there by
     * the sub-parser(s) of the action's consumer. This action must return a value which is
     * automatically pushed on the value stack.
     */
    @FunctionalInterface
    interface PushWithParse extends Push
    {
        @Override default void apply (Parse parse, Object[] items, int pos0, int size0) {
            parse.stack.push(get(parse, items));
        }

        @Override default Object get (Object[] items) {
            throw new Error("You called a StackAction with another method than #apply!");
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
    interface PushWithString extends Push
    {
        @Override default void apply (Parse parse, Object[] items, int pos0, int size0)
        {
            String match = items != null ? new String(parse.string, pos0, parse.pos - pos0) : null;
            parse.stack.push(get(parse, items, match));
        }

        @Override default Object get (Object[] items) {
            throw new Error("You called a StackAction with another method than #apply!");
        }

        Object get (Parse parse, Object[] items, String match);
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
    interface PushWithList extends Push
    {
        @Override default void apply (Parse parse, Object[] items, int pos0, int size0)
        {
            List<?> match = items != null ? parse.list.subList(pos0, parse.pos) : null;
            parse.stack.push(get(parse, items, match));
        }

        @Override default Object get (Object[] items) {
            throw new Error("You called a StackAction with another method than #apply!");
        }

        Object get (Parse parse, Object[] items, List<?> match);
    }

    // ---------------------------------------------------------------------------------------------
}