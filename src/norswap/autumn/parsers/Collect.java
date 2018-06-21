package norswap.autumn.parsers;

import norswap.autumn.Parse;
import norswap.autumn.Parser;
import norswap.autumn.ParserVisitor;
import java.util.Collections;
import java.util.List;

/**
 * Matches its child and, if it succeeds, collects all the items it added to {@link Parse#stack} and
 * passes them to a user-defined action, optionally along with the input matched by the child.
 *
 * <p>There are three kinds of actions the user can define: {@link SimpleAction}, {@link
 * ListAction}, {@link StringAction}.
 *
 * <p>The {@code reduce} constructor parameter controls whether the collected items are popped from
 * the stack. The items are popped if and only if {@code reduce == true}.
 */
public final class Collect extends Parser
{
    // ---------------------------------------------------------------------------------------------

    /**
     * The display name for this parser.
     */
    public final String name;

    // ---------------------------------------------------------------------------------------------

    public final Parser child;

    // ---------------------------------------------------------------------------------------------

    /**
     * Indicates whether the stack items passed to the action should be popped from the stack
     * (true) or left there (false).
     */
    public final boolean reduce;

    // ---------------------------------------------------------------------------------------------

    /**
     * The action to be applied using the items pushed on the stack during the execution
     * of the child parser.
     */
    public final Action action;

    // ---------------------------------------------------------------------------------------------

    private Collect (String name, Parser child, boolean reduce, Action action)
    {
        this.name = name;
        this.child = child;
        this.reduce = reduce;
        this.action = action;
    }

    // ---------------------------------------------------------------------------------------------

    public Collect (String name, Parser child, boolean reduce, SimpleAction action) {
        this(name, child, reduce, (Action) action);
    }

    // ---------------------------------------------------------------------------------------------

    public Collect (String name, Parser child, boolean reduce, ListAction action) {
        this(name, child, reduce, (Action) action);
    }

    // ---------------------------------------------------------------------------------------------

    public Collect (String name, Parser child, boolean reduce, StringAction action) {
        this(name, child, reduce, (Action) action);
    }

    // ---------------------------------------------------------------------------------------------

    private static final Object MARKER = new Object();

    // ---------------------------------------------------------------------------------------------

    @Override public boolean doparse (Parse parse)
    {
        int pos0 = parse.pos;
        int size0 = parse.stack.size();
        if (!child.parse(parse))
            return false;
        action.apply(parse, reduce, pos0, size0);
        return true;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void accept (ParserVisitor visitor) {
        visitor.visit(this);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public Iterable<Parser> children() {
        return Collections.singletonList(child);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toStringFull()
    {
        return name;
    }

    // ---------------------------------------------------------------------------------------------

    private static Object[] get_from (Parse parse, boolean reduce, int index) {
        return reduce
            ? parse.pop_from(index)
            : parse.look_from(index);
    }

    // ---------------------------------------------------------------------------------------------

    private interface Action
    {
        void apply (Parse parser, boolean reduce, int pos0, int size0);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * An action that can consult the parse and collected stack items.
     */
    @FunctionalInterface public interface SimpleAction extends Action
    {
        @Override default void apply (Parse parse, boolean reduce, int pos0, int size0) {
            apply(parse, get_from(parse, reduce, size0));
        }

        void apply (Parse parse, Object[] items);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * An action that can consult the parse, the matched part of {@link Parse#list} and
     * the collected stack items.
     */
    @FunctionalInterface public interface ListAction extends Action
    {
        @Override default void apply (Parse parse, boolean reduce, int pos0, int size0) {
            assert parse.list != null;
            apply(parse, parse.list.subList(pos0, parse.pos), get_from(parse, reduce, size0));
        }

        void apply (Parse parse, List<?> match, Object[] items);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * An action that can consult the parse, the matched part of {@link Parse#string} and
     * the collected stack items.
     */
    @FunctionalInterface public interface StringAction extends Action
    {
        @Override default void apply (Parse parse, boolean reduce, int pos0, int size0) {
            assert parse.string != null;
            apply(parse, parse.string.substring(pos0, parse.pos), get_from(parse, reduce, size0));
        }

        void apply (Parse parse, String match, Object[] items);
    }

    // ---------------------------------------------------------------------------------------------
}