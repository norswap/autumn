package norswap.autumn.actions;

import norswap.autumn.Parse;
import norswap.autumn.ParseOptions;
import norswap.autumn.ParseState;
import norswap.autumn.SideEffect;
import norswap.autumn.parsers.Collect;
import norswap.autumn.positions.Span;
import java.util.Arrays;
import java.util.List;

import static norswap.utils.Util.cast;

/**
 * This object collects arguments to a {@link StackAction}. It can be used to access
 * many details regarding the current state of the parse, include the {@link Parse} object itself,
 * as well as items collected from the stack, which are available under {@link #$}.
 *
 * <p>In this documentation, when we say "child parser" we mean the child parser invoked by the
 * consumer of the stack action, during whose execution the consumer collects items from the {@link
 * Parse#stack value stack} (cf. {@link StackAction}).
 *
 * <p>This additionally provide some helper methods to peform some common actions, as well as
 * utilities to access the the stack items and automatically cast them to a target type ({@link
 * #$0()}, {@link #$1()}, etc).
 *
 * <p>This object is created by consumer of {@link StackAction}. See {@link StackAction the javadoc}
 * for a list of built-in consumers.
 *
 * <p>There are a few constraints on the arguments used to construct this class - refer to the
 * fields javadoc for the full list. In particular, {@link #$} should be null if the child parser
 * invocation that underlies the action failed. {@link #leadingWhitespaceStart} and {@link
 * #trailingWhitespaceStart} also have specific requirements.
 */
public final class ActionContext
{
    // ---------------------------------------------------------------------------------------------

    /**
     * Current {@link Parse}.
     */
    public final Parse parse;

    // ---------------------------------------------------------------------------------------------

    /**
     * Items retrieved from the {@link Parse#stack value stack} during the execution of the
     * child parser (+ potential lookback, see {@link Collect#lookback}.
     *
     * <p>Will be null if the child parser failed but the action is executed anyway (see {@link
     * Collect#actionOnFail}).
     */
    public final Object[] $;

    // ---------------------------------------------------------------------------------------------

    /**
     * Input position before the execution of the child parser.
     */
    public final int pos0;

    // ---------------------------------------------------------------------------------------------

    /**
     * {@link Parse#log} size before the execution of the underlying child parser.
     */
    public final int size0;

    // ---------------------------------------------------------------------------------------------

    /**
     * Start of the leading whitespace preceding {@link #pos0}. Always {@code <= pos0}, and {@code
     * == pos0} if no such whitespace exist, or the information is not provided ({@link
     * ParseOptions#trackWhitespace} disabled). Can be set even if the child parser failed,
     * however.
     */
    public final int leadingWhitespaceStart;

    // ---------------------------------------------------------------------------------------------

    /**
     * Start of the trailing whitespace matched while executing the child parser. Always {@code >=
     * parse.pos}, and {@code == parse.pos} if no such whitespace exist, the child parser failed, or
     * the information is not provided ({@link ParseOptions#trackWhitespace} disabled).
     */
    public final int trailingWhitespaceStart;

    // ---------------------------------------------------------------------------------------------

    public ActionContext (Parse parse, Object[] items, int pos0, int size0,
            int leadingWhitespaceStart, int trailingWhitespaceStart)
    {
        this.parse = parse;
        this.$ = items;
        this.pos0 = pos0;
        this.size0 = size0;
        this.leadingWhitespaceStart = leadingWhitespaceStart;
        this.trailingWhitespaceStart = trailingWhitespaceStart;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * True iff the child parser suceeded.
     *
     * <p>The child parser can fail, and the action be executed anyway, see {@link
     * Collect#actionOnFail}.
     */
    public boolean success() {
        return $ != null;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns {@link ActionContext#$}{@code [index]}, implicitly casted to the requested targget
     * member type, and also supports negative indices, where -1 designates the last item in the
     * array.
     */
    public <T> T get(int index)
    {
        if (index < -$.length || index > $.length)
            throw new ArrayIndexOutOfBoundsException("$.get(" + index + ")");
        if (index < 0)
            index = $.length + index;
        return cast($[index]);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a list version of {@link #$}, implicitly casted to the requested target member type.
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> $list() {
        return Arrays.asList((T[]) $);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a copy of {@link #$}, whose type matched the passed {@code witness} array.
     */
    @SuppressWarnings("SuspiciousSystemArraycopy")
    public <T> T[] $array(T[] witness) {
        T[] out = Arrays.copyOf(witness, $.length);
        System.arraycopy($, 0, out, 0, out.length);
        return out;
    }

    // ---------------------------------------------------------------------------------------------

    private Span span = null;

    /**
     * Returns a span spanning the input matched by the child parser, including whitespace
     * information if available (see {@link Span} for details on whitespace). Returns an empty child
     * parser (0-sized, and with no whitespace information) at the invocation position if the child
     * parser failed but the action is executed anyway.
     */
    public Span span() {
        return span != null
            ? span
            : (span = new Span(pos0, trailingWhitespaceStart, leadingWhitespaceStart, parse.pos));
    }

    // ---------------------------------------------------------------------------------------------

    private String str;

    /**
     * Returns the string matched by the child parser.
     *
     * <p>The return value cached in this object, so repeated calls are not wasteful.
     */
    public String str() {
        return str != null
            ? str
            : $ == null
                ? null
                : (str = span().get(parse.string));
    }

    // ---------------------------------------------------------------------------------------------

    private List<?> list;

    /**
     * Returns the list of tokens matched by the child parser.
     *
     * <p>The return value cached in this object, so repeated calls are not wasteful.
     */
    public List<?> list() {
        return list != null
            ? list
            : $ == null
                ? null
                : (list = span().get(parse.list));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Helper method to push values on the {@link Parse#stack value stack}, equivalent to {@code
     * this.parse.stack.push(item)}.
     */
    public void push (Object item) {
        parse.stack.push(item);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Helper method to push values on the {@link Parse#stack value stack}, equivalent to {@code
     * this.parse.stack.push(item)} with every value in the array, in array order.
     */
    public void pushAll (Object... items) {
        for (Object item: items)
            parse.stack.push(item);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Helper method to retrieve the data from a parse state, equivalent to {@code
     * state.data(this.parse)}.
     */
    public <T> T data (ParseState<T> state) {
        return state.data(parse);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Helper method to apply a side effect, equivalent to {@code this.parse.log.apply(sideEffect)}.
     */
    public void apply (SideEffect sideEffect) {
        parse.log.apply(sideEffect);
    }

    // ---------------------------------------------------------------------------------------------

    /** Returns the stack item at index 0, casted to the target type. */
    public <T> T $0() {
        return cast($[0]);
    }

    /** Returns the stack item at index 1, casted to the target type. */
    public <T> T $1() {
        return cast($[1]);
    }

    /** Returns the stack item at index 2, casted to the target type. */
    public <T> T $2() {
        return cast($[2]);
    }

    /** Returns the stack item at index 3, casted to the target type. */
    public <T> T $3() {
        return cast($[3]);
    }

    /** Returns the stack item at index 4, casted to the target type. */
    public <T> T $4() {
        return cast($[4]);
    }

    /** Returns the stack item at index 5, casted to the target type. */
    public <T> T $5() {
        return cast($[5]);
    }

    /** Returns the stack item at index 6, casted to the target type. */
    public <T> T $6() {
        return cast($[6]);
    }

    /** Returns the stack item at index 7, casted to the target type. */
    public <T> T $7() {
        return cast($[7]);
    }

    /** Returns the stack item at index 8, casted to the target type. */
    public <T> T $8() {
        return cast($[8]);
    }

    /** Returns the stack item at index 9, casted to the target type. */
    public <T> T $9() {
        return cast($[9]);
    }

    // ---------------------------------------------------------------------------------------------
}
