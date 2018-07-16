package norswap.autumn;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * The context associated with "a parse" (running a parsing expression on some input).
 *
 * <p>A parse is created by giving it an input: either a String ({@link #string}) or a
 * list ({@link #list}).
 */
public final class Parse
{
    // ---------------------------------------------------------------------------------------------

    /**
     * Position within the input.
     */
    public int pos = 0;

    // ---------------------------------------------------------------------------------------------

    /**
     * Position of the furthest encountered error, initially -1.
     */
    public int error = -1;

    // ---------------------------------------------------------------------------------------------

    /**
     * One of the two forms of input the parse may have.
     */
    public final String string;

    // ---------------------------------------------------------------------------------------------

    /**
     * One of the two forms of input the parse may have.
     */
    public final List<?> list;

    // ---------------------------------------------------------------------------------------------

    /**
     * The list of side-effects that have been applied during this parse. New side-effects
     * are appended at the end.
     *
     * <p>Usually, this is only modified through the {@link #apply} methods. Parsers automatically
     * undo side-effects when fail through {@link #rollback}. A list of recently applied
     * side-effects can be acquired through {@link #delta}.
     */
    public final List<SideEffect> log = new ArrayList<>();

    // ---------------------------------------------------------------------------------------------

    /**
     * A stack that can be used to build ASTs.
     *
     * <p>Unless modifications are never exposed outside of the parser making the modification, this
     * stack should only be mutated through a {@link SideEffect}. The helper method {@link #push}
     * does this for you.
     */
    public final Deque<?> stack = new ArrayDeque<>();

    // ---------------------------------------------------------------------------------------------

    /**
     * Create a parse over a string input.
     */
    public Parse (String string)
    {
        this.string = string;
        this.list   = null;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Create a parse over a list of objects.
     */
    public Parse (List<?> list)
    {
        this.string = null;
        this.list   = list;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the character from {@link #string} at the given index,
     * or 0 if {@code index == string.length}.
     */
    public char char_at (int index)
    {
        assert string != null;
        return index != string.length()
            ? string.charAt(index)
            : 0;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the object from {@link #list} at the given index,
     * or null if {@code index == list.size()}.
     */
    public Object object_at (int index)
    {
        assert list != null;
        return index != list.size()
            ? list.get(index)
            : null;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Push an item onto the AST {@link #stack} through a {@link SideEffect}.
     */
    @SuppressWarnings("unchecked")
    public void push (Object item)
    {
        apply(() -> ((Deque<Object>) stack).push(item), stack::pop);
    }

    // ---------------------------------------------------------------------------------------------

    private void check_stack_index (int index)
    {
        if (index < 0 || stack.size() < index)
            throw new IllegalArgumentException(
                "illegal index " + index + " for stack of size " + stack.size());
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Pops the first item at the top of the AST {@link #stack} and returns it.
     */
    public Object pop()
    {
        check_stack_index(1);
        return stack.pop();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Pops items from the AST {@link #stack}, whose index {@code i} are such that {@code index <= i
     * < stack.size} (the item at the bottom of the stack has index 0).
     *
     * <p> Returns the popped items as an array, in increasing index order (the top of the stack
     * will be the last element of the array).
     */
    public Object[] pop_from (int index)
    {
        check_stack_index(index);
        int len = stack.size() - index;
        Object[] args = new Object[len];
        for (int i = 1; i <= len; ++i)
            args[len - i] = stack.pop();
        return args;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns an array of items from the AST {@link #stack}, whose index {@code i} are such that
     * {@code index <= i < stack.size} (the item at the bottom of the stack has index 0), in
     * increasing index order (the top of the stack will be the last element of the array).
     */
    public Object[] look_from (int index)
    {
        check_stack_index(index);
        int len = stack.size() - index;
        Object[] args = new Object[len];
        int i = 1;
        for (Object it: stack)
            if (i <= len) args[len - i++] = it;
            else break;
        return args;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Applies the given side-effect and adds it to the log of applied side effects.
     */
    public void apply (SideEffect effect)
    {
        log.add(effect);
        effect.apply.run();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Creates a new side-effect from the given apply and undo actions, then applies it and
     * adds it to the log of applied side effects.
     */
    public void apply (Runnable apply, Runnable undo)
    {
        apply(new SideEffect(apply, undo));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Rollback logged side-effects in reverse order of application until the log size is {@code
     * log_target_size}.
     */
    public void rollback (int log_target_size)
    {
        for (int i = log.size(); i > log_target_size; --i)
        {
            SideEffect effect = log.remove(i - 1);
            effect.undo.run();
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a list of logged side-effects whose index {@code i} are such that {@code
     * log_start_index <= i < log.size()}, in increasing index order.
     */
    public List<SideEffect> delta (int log_start_index)
    {
        return new ArrayList<>(log.subList(log_start_index, log.size()));
    }

    // ---------------------------------------------------------------------------------------------
}
