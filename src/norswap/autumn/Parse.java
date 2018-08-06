package norswap.autumn;

import norswap.autumn.parsers.Sequence;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/**
 * The context associated with "a parse" (running a parsing expression on some input).
 *
 * <p>A parse is created by giving it an input: either a String ({@link #string}) or a
 * list ({@link #list}).
 *
 * <p>Another setting you can select is whether to record a *call stack* - the stack of all
 * parser whose invocation is pending. The stack can be inspected by parsers through {@link
 * #call_stack()}.
 *
 * <p>When call stack recording is enabled, the parse also records the *error call stack*, which
 * is a snapshot of the call stack taken at the furthest error location ({@link #error}).
 *
 * <p>Parse objects are not meant to be reused (fed to multiple top-level parsers). Technically, it
 * might be possible feed the object to multiple parsers successively in order to match a sequence
 * of items. There is however not point in doing so, as you can create a {@link Sequence} parser
 * instead.
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
     * Indicates wether the parse records the stack of parser invocations in {@link #call_stack}.
     *
     * Set this before parsing in order to record the stack of parser invocations ({@link
     * #error_call_stack}) leading to the furthest error (at position {@link #error}).
     *
     * <p>Do not modify this setting after the parse has started.
     */
    public final boolean record_call_stack;

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
     * The current parser invocation stack.
     * Only filled in if {@link #record_call_stack} is true.
     */
    final ArrayDeque<ParserCallFrame> call_stack;

    // ---------------------------------------------------------------------------------------------

    /**
     * The stack of parser invocations that lead to the furthest error.
     * Only filled in if {@link #record_call_stack} is true.
     */
    ArrayDeque<ParserCallFrame> error_call_stack;

    // ---------------------------------------------------------------------------------------------

    private Parse (String string, List<?> list, boolean record_call_stack)
    {
        this.string = string;
        this.list   = list;
        this.record_call_stack = record_call_stack;
        call_stack = record_call_stack ? new ArrayDeque<>() : null;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Create a parse over a string input, without call stack recording.
     */
    public static Parse of (String string)
    {
        return new Parse(string, null, false);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Create a parse over a list of objects, without call stack recording.
     */
    public static Parse of (List<?> list)
    {
        return new Parse(null, list, false);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Create a parse over a string input, with call stack recording depending on {@code
     * record_call_stack}.
     */
    public static Parse of (String string, boolean record_call_stack)
    {
        return new Parse(string, null, record_call_stack);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Create a parse over a list of objects, with call stack recording depending on {@code
     * record_call_stack}.
     */
    public static Parse of (List<?> list, boolean record_call_stack)
    {
        return new Parse(null, list, record_call_stack);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * A generic method returning the size of the input that abstracts over whether this parse
     * is over a string or a list.
     */
    public int input_length()
    {
        return string != null
            ? string.length()
            : list.size();
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
     * Returns the current stack of parser invocations as an umodifiable collection whose iteration
     * order goes from the top to the bottom of the stack (last called parser to first called
     * parser).
     *
     * <p>The collection is unmodifiable but not immutable. If a snapshot is required, a copy
     * should be made, e.g. with {@link ArrayList#ArrayList(Collection)}.
     *
     * @throws Error if {@link #record_call_stack} is false.
     */
    public Collection<ParserCallFrame> call_stack()
    {
        if (!record_call_stack)
            throw new Error("Trying to access the call stack, even though it wasn't recorded!");

        return Collections.unmodifiableCollection(call_stack);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the stack of parser invocations that lead to the furthest error (at position {@link
     * #error}), or null if there were no parse errors. The stack is returned as an unmodifiable
     * collection whose iteration order goes from the top to the bottom of the stack (last called
     * parser to first called parser).
     *
     * <p>The collection is unmodifiable, but will only be immutable if the parse is complete,
     * and the parse object is not reused.
     *
     * @throws Error if {@link #record_call_stack} is false.
     */
    public Collection<ParserCallFrame> error_call_stack ()
    {
        if (!record_call_stack)
            throw new Error("Trying to access the error call stack, even though it wasn't recorded!");

        return Collections.unmodifiableCollection(error_call_stack);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Indicates whether the parse matched the whole input.
     */
    public boolean full_match()
    {
       return string != null
           ? string.length() == pos
           : list.size() == pos;
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
