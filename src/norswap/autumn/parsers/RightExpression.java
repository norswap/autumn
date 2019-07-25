package norswap.autumn.parsers;

import norswap.autumn.Parse;
import norswap.autumn.Parser;
import norswap.autumn.ParserVisitor;
import norswap.autumn.SideEffect;
import norswap.autumn.StackAction;
import norswap.utils.ArrayListInt;
import norswap.utils.ArrayStack;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Matches a collection of right-associative infix and prefix expressions with the same precedence.
 *
 * <p>See the definition of the various fields for more informations.
 *
 * <p>The infix operators have priority over the prefix operators, and the operators of the same
 * kind prioritize in the order in which they are given. Note that priority here doesn't mean
 * "precedence" but rather "priority" in the sense of "prioritized choice" (like {@link Choice}).
 *
 * <p>The parser tries to match as many repetitions of {@code (left infix | prefix)} as possible
 * (where {@code infix} is a disjunction of all infix operators and {@code prefix} is a disjunction
 * of all prefix operators), followed by {@code right}. It then applies the step {@link StackAction}
 * corresponding to each matched operator, in reverse order (right-most operator first).
 *
 * <p>For each operator, the step action will act as though all subsequent operators and the right
 * operand were children of the operator (hence emulating true right-associative semantics). For
 * infix operators, the step action consider the match starts before the corresponding left operand.
 *
 * <p><b>Beware:</b> defining different parsers for the left and right operands that may nonetheless
 * call the same parser(s) may cause significant parse performance degradation. This parser is able
 * to optimize this case when the same parser is passed as both left and right operand, however.
 */
public final class RightExpression extends Parser
{
    // ---------------------------------------------------------------------------------------------

    /** Left operand, can be null if no infix operators are defined. */
    public final Parser left;

    // ---------------------------------------------------------------------------------------------

    /** Right operand. */
    public final Parser right;

    // ---------------------------------------------------------------------------------------------

    /** Infix operators. */
    public final Parser[] infixes;

    // ---------------------------------------------------------------------------------------------

    /** Stack actions associated with the corresponding infix operators in {@link #infixes}. */
    public final StackAction[] infix_steps;

    // ---------------------------------------------------------------------------------------------

    /** Prefix operators. */
    public final Parser[] prefixes;

    // ---------------------------------------------------------------------------------------------

    /** Stack actions associated with the corresponding prefix operators in {@link #infixes}. */
    public final StackAction[] prefix_steps;

    // ---------------------------------------------------------------------------------------------

    /** Whether a right operand can match on its own (false), or an operator is required (true). */
    public final boolean operator_required;

    // ---------------------------------------------------------------------------------------------

    public RightExpression (
        Parser left, Parser right,
        Parser[] infixes, StackAction[] infix_steps,
        Parser[] prefixes, StackAction[] prefix_steps,
        boolean operator_required)
    {
        assert right != null;
        assert left != null || infixes.length == 0;
        assert infixes.length == infix_steps.length;
        assert prefixes.length == prefix_steps.length;

        this.left = left;
        this.right = right;
        this.infixes = infixes;
        this.prefixes = prefixes;
        this.infix_steps = infix_steps;
        this.prefix_steps = prefix_steps;
        this.operator_required = operator_required;
    }

    // ---------------------------------------------------------------------------------------------

    @Override
    protected boolean doparse (Parse parse)
    {
        // Stores alternate pairs of position and stack size recorded
        // before parsing a left-hand side.
        ArrayListInt stack = new ArrayListInt();
        stack.push(parse.pos);
        stack.push(parse.stack.size());

        // Stores the step corresponding to a parsed left-hand side.
        ArrayStack<StackAction> steps = new ArrayStack<>();

        // Size of the log before parsing the next left-hand side.
        int log0 = parse.log.size();

        // Used to cache the result of parsing this.left when this.left == this.right.
        // Note the context will always be identical.
        int right_cached_pos = -1;
        List<SideEffect> right_cached_delta = null;

        outer: while (true)
        {
            if (left != null && left.parse(parse)) {
                for (int i = 0; i < infixes.length; ++i)
                    if (infixes[i].parse(parse)) {
                        stack.push(parse.pos);
                        stack.push(parse.stack.size());
                        steps.push(infix_steps[i]);
                        log0 = parse.log.size();
                        continue outer;
                    }

                if (left == right) {
                    right_cached_pos = parse.pos;
                    right_cached_delta = parse.log.delta(log0);
                }

                // rollback left operand
                parse.pos = stack.back(1);
                parse.log.rollback(log0);
            }

            for (int i = 0; i < prefixes.length; ++i)
                if (prefixes[i].parse(parse)) {
                    stack.push(parse.pos);
                    stack.push(parse.stack.size());
                    steps.push(prefix_steps[i]);
                    log0 = parse.log.size();
                    right_cached_pos = -1;
                    right_cached_delta = null;
                    continue outer;
                }

            break;
        }

        // Always pop the last entry (the last operand is not a left-hand-side).
        stack.pop(2);

        if (operator_required && stack.size() == 0)
            return false;

        if (right_cached_pos > 0) {
            parse.pos = right_cached_pos;
            parse.log.apply(right_cached_delta);
        }
        else if (!right.parse(parse))
            return false;

        while (stack.size() > 0) {
            int size0 = stack.pop();
            int pos0  = stack.pop();
            StackAction step = steps.pop();
            step.apply(parse, parse.stack.pop_from(size0), pos0, size0);
        }

        return true;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void accept (ParserVisitor visitor) {
        visitor.visit(this);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     *
     * <p>Order: left, right, infix operators, prefix operators
     */
    @Override public List<Parser> children()
    {
        return Collections.unmodifiableList(Stream.of(
                left != null ? Stream.of(left) : Stream.<Parser>empty(),
                Stream.of(right),
                Arrays.stream(infixes),
                Arrays.stream(prefixes))
            .flatMap(Function.identity())
            .collect(Collectors.toList()));
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toStringFull ()
    {
        return "RightExpression(" +
            "left=" + left +
            ", right=" + right +
            ", ops=" + Arrays.toString(infixes) +
            ", prefixes=" + Arrays.toString(prefixes) +
            ", operator_required=" + operator_required +
            ')';
    }

    // ---------------------------------------------------------------------------------------------
}
