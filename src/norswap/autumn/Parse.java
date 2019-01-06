package norswap.autumn;

import norswap.autumn.parsers.Not;
import norswap.autumn.parsers.Sequence;
import norswap.utils.Slot;
import norswap.utils.ArrayListLong;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * Used as a stand-in for null values in {@link #stack}.
     * @see #stack
     */
    public static final Object NULL = new Object();

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
     */
    public final boolean record_call_stack;

    // ---------------------------------------------------------------------------------------------

    /**
     * Indicate whether the parse traces its execution. This records performance metrics for each
     * parser (see {@link ParserMetrics}) into {@link #trace_metrics}. Enabling this flag does slow
     * down the execution considerably (around x2 in our initial tests).
     */
    public final boolean trace;

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
     * <p>This stack should only be mutated through a {@link SideEffect}. The helper methods {@link
     * #push} , {@link #pop()}, etc... does this for you automatically.
     *
     * <p>Since {@code null} value are unsupported for deques, {@link #push}, {@link #pop()}, {@link
     * #peek()}, etc... automatically translate from/to null to/from the special {@link #NULL}
     * object.
     *
     * <p>The two big legitimate use cases for accessing this is (a) taking the size of the stack
     * and (b) checking the results of the parse after it is complete.
     */
    public final Deque<?> stack = new ArrayDeque<>();

    // ---------------------------------------------------------------------------------------------

    /**
     * Use this map to store custom parsing state. If state changes must be undone when
     * backtracking (as is usual), these states should usually be modified exclusively through a
     * {@link SideEffect}.
     *
     * <p>Use {@link ParseState} to transparently access this map and cache its values for
     * increase performance.
     */
    public final Map<Object, Object> states = new HashMap<>();

    // ---------------------------------------------------------------------------------------------

    /**
     * The current parser invocation stack.
     * Only filled in if {@link #record_call_stack} is true.
     */
    ArrayDeque<ParserCallFrame> call_stack;

    // ---------------------------------------------------------------------------------------------

    /**
     * The stack of parser invocations that lead to the furthest error.
     * Only filled in if {@link #record_call_stack} is true.
     */
    ArrayDeque<ParserCallFrame> error_call_stack;

    // ---------------------------------------------------------------------------------------------

    final ArrayListLong trace_timings;

    // ---------------------------------------------------------------------------------------------

    /**
     * Maps parser names to a set of parser metrics.
     *
     * <p>You can set this before starting the parse, in order to collect metrics accross
     * multiple parses. Note that this will only work properly if each parse completes without
     * throwing an exception (as that would break recursion tracking).
     */
    public Map<Parser, ParserMetrics> trace_metrics;

    // ---------------------------------------------------------------------------------------------

    private Parse (String string, List<?> list, boolean record_call_stack, boolean trace)
    {
        this.string = string;
        this.list   = list;
        this.record_call_stack = record_call_stack;
        this.trace = trace;
        call_stack = record_call_stack ? new ArrayDeque<>() : null;
        trace_timings = trace ? new ArrayListLong(256) : null;
        trace_metrics = trace ? new HashMap<>() : null;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Create a parse over a string input, without call stack recording.
     */
    public Parse (String string)
    {
        this(string, null, false, false);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Create a parse over a list of objects, without call stack recording.
     */
    public Parse (List<?> list)
    {
        this(null, list, false, false);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Create a parse over a string input, with call stack recording depending on {@code
     * record_call_stack}.
     */
    public Parse (String string, boolean record_call_stack, boolean trace)
    {
        this(string, null, record_call_stack, trace);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Create a parse over a list of objects, with call stack recording depending on {@code
     * record_call_stack}.
     */
    public Parse (List<?> list, boolean record_call_stack, boolean trace)
    {
        this(null, list, record_call_stack, trace);
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
     * Returns an immutable copy of the current stack of parser invocations, whose iteration
     * order goes from the top to the bottom of the stack (last called parser to first called
     * parser).
     *
     * @throws Error if {@link #record_call_stack} is false.
     */
    public Collection<ParserCallFrame> call_stack()
    {
        if (!record_call_stack)
            throw new Error("Trying to access the call stack, even though it wasn't recorded!");

        return Collections.unmodifiableCollection(call_stack.clone());
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * cf. {@link #call_stack()} but returns the actual call stack datastructure, that is both
     * modifiable, and usually keeps getting modified during the parse. Will return null if {@link
     * #record_call_stack} is false.
     *
     * <p>Don't use this unless you really now what you're doing! No base parsers use this.
     */
    public ArrayDeque<ParserCallFrame> call_stack_mutable()
    {
        return call_stack;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * cf. {@link #call_stack()}, but a setter.
     *
     * <p>Don't use this unless you really now what you're doing! No base parsers use this.
     */
    public void set_call_stack (ArrayDeque<ParserCallFrame> call_stack)
    {
        this.call_stack = call_stack;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the stack of parser invocations that lead to the furthest error (at position {@link
     * #error}), or null if there were no parse errors. The stack is returned as an immutable
     * collection whose iteration order goes from the top to the bottom of the stack (last called
     * parser to first called parser).
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
     * cf. {@link #error_call_stack()} but returns the actual (mutable) error call stack
     * datastructure. Will return null if {@link #record_call_stack} is false.
     *
     * <p>Don't use this unless you really now what you're doing! Among base parsers,
     * only {@link Not} uses this.
     */
    public ArrayDeque<ParserCallFrame> error_call_stack_mutable()
    {
        return error_call_stack;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * cf. {@link #error_call_stack}, but a setter.
     *
     * <p>Don't use this unless you really now what you're doing! Among base parsers,
     * only {@link Not} uses this.
     */
    public void set_error_call_stack (ArrayDeque<ParserCallFrame> error_call_stack)
    {
        this.error_call_stack = error_call_stack;
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

    private Object convert (Object o)
    {
        if (o == null) return NULL;
        if (o == NULL) return null;
        return o;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Push an item onto the AST {@link #stack} through a {@link SideEffect}.
     */
    @SuppressWarnings("unchecked")
    public void push (Object item)
    {
        apply(() -> ((Deque<Object>) stack).push(convert(item)), stack::pop);
    }

    // ---------------------------------------------------------------------------------------------

    private void check_stack_size (int size)
    {
        if (size < 0 || stack.size() < size)
            throw new IllegalArgumentException(
                "amount (" + size + ") too large for stack of size: " + stack.size());
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the first item at the top of the AST {@link #stack}, or throws an exception if
     * the stack is empty.
     */
    public Object peek()
    {
        check_stack_size(1);
        return convert(stack.element());
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Pops the first item at the top of the AST {@link #stack}, returns it, and registers
     * a corresponding {@link SideEffect}.
     */
    @SuppressWarnings("unchecked")
    public Object pop()
    {
        Slot<Object> slot = new Slot<>();

        apply(
            () -> {
                check_stack_size(1);
                slot.x = convert(stack.pop());
            },
            () -> ((Deque<Object>) stack).push(convert(slot.x)));

        return slot.x;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns an array containing the {@code amount} items at the top of the AST {@link #stack}, in
     * increasing index order (the top of the stack will be the last element of the array).
     */
    public Object[] peek (int amount)
    {
        check_stack_size(amount);
        Object[] args = new Object[amount];
        int i = 1;
        for (Object it: stack)
            if (i <= amount) args[amount - i++] = convert(it);
            else break;
        return args;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Just like {@link #peek}, for as many items are there are between {@code index} and the top
     * of the stack (both inclusive). (The item at the bottom of the stack has index 0.)
     */
    public Object[] peek_from (int index)
    {
       return peek(stack.size() - index);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Pops the {@code amount} items at the top of the AST {@link #stack}, and returns them as an
     * array, in increasing index order (the top of the stack will be the last element of the
     * array).
     *
     * <p>A corresponding {@link SideEffect} is also registered.
     */
    @SuppressWarnings("unchecked")
    public Object[] pop (int amount)
    {
        Slot<Object[]> slot = new Slot<>();

        apply(
            () -> {
                check_stack_size(amount);
                Object[] args = new Object[amount];
                for (int i = 1; i <= amount; ++i)
                    args[amount - i] = convert(stack.pop());
                slot.x = args;
            },
            () -> {
                for (Object o: slot.x)
                    ((Deque<Object>)stack).push(convert(o));
            });

        return slot.x;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Just like {@link #peek}, for as many items are there are between {@code index} and the top
     * of the stack (both inclusive). (The item at the bottom of the stack has index 0.)
     *
     * <p>The registered side-effect will remember the amount to pop, not the specific index
     * passed to the function, which is generally the desired semantics.
     */
    public Object[] pop_from (int index)
    {
        return pop(stack.size() - index);
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
     * Applies a list of side-effects in order. Usually the list was obtained by a previous call to
     * {@link #delta}.
     */
    public void apply (List<SideEffect> delta)
    {
        delta.forEach(this::apply);
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
