package norswap.autumn.parsers;

import norswap.autumn.Parse;
import norswap.autumn.Parser;
import norswap.autumn.ParserVisitor;
import norswap.autumn.actions.ActionContext;
import norswap.autumn.actions.StackAction;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Matches a collection of left-associative infix and suffix expressions with the same precedence.
 *
 * <p>See the definition of the various fields for more informations.
 *
 * <p>The infix operators have priority over the suffix operators, and the operators of the same
 * kind prioritize in the order in which they are given. Note that priority here doesn't mean
 * "precedence" but rather "priority" in the sense of "prioritized choice" (like {@link Choice}).
 *
 * <p>The parser tries to match {@code left}, followed by as many repetitions of {@code (infix right
 * | suffix)} as possible (where {@code infix} is a disjunction of all infix operators and {@code
 * suffix} is a disjunction of all suffix operators). As it goes, the parser applies the step {@link
 * StackAction} corresponding to each matched operator.
 *
 * <p>For each operator, the step action will act as though the match started at the position the
 * {@code LeftExpression} parser was invoked at (hence emulating true left-associative semantics).
 */
public final class LeftExpression extends Parser
{
    // ---------------------------------------------------------------------------------------------

    /* Left operand. */
    public final Parser left;

    // ---------------------------------------------------------------------------------------------

    /** Right operand, can be null if no infix operators are defined. */
    public final Parser right;

    // ---------------------------------------------------------------------------------------------

    /** Infix operators. */
    public final Parser[] infixes;

    // ---------------------------------------------------------------------------------------------

    /** Stack actions associated with the corresponding infix operators in {@link #infixes}. */
    public final StackAction[] infixSteps;

    // ---------------------------------------------------------------------------------------------

    /** Suffix operators. */
    public final Parser[] suffixes;

    // ---------------------------------------------------------------------------------------------

    /** Stack actions associated with the corresponding prefix operators in {@link #infixes}. */
    public final StackAction[] suffixSteps;

    // ---------------------------------------------------------------------------------------------

    /** Whether a left operand can match on its own (false), or an operator is required (true). */
    public final boolean operatorRequired;

    // ---------------------------------------------------------------------------------------------
    
    public LeftExpression (
        Parser left, Parser right,
        Parser[] infixes, StackAction[] infixSteps,
        Parser[] suffixes, StackAction[] suffixSteps,
        boolean operatorRequired)
    {
        assert left != null;
        assert right != null || infixes.length == 0;
        assert infixes.length == infixSteps.length;
        assert suffixes.length == suffixSteps.length;

        this.left = left;
        this.right = right;
        this.infixes = infixes;
        this.suffixes = suffixes;
        this.infixSteps = infixSteps;
        this.suffixSteps = suffixSteps;
        this.operatorRequired = operatorRequired;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public boolean doparse (Parse parse)
    {
        final int pos0  = parse.pos;
        final int stack0 = parse.stack.size();
        final int whitespace0 = parse.leadingWhitespaceStart();
        int count = 0;

        final Consumer<StackAction> applyStep = (step) ->
            step.apply(new ActionContext(
                parse, parse.stack.popFrom(stack0), pos0, stack0,
                whitespace0, parse.trailingWhitespaceStart(pos0)));
        
        if (!left.parse(parse))
            return false;
        
        outer: while (true)
        {
            int pos1 = parse.pos;
            int log1 = parse.log.size();
            StackAction step = null;

            for (int i = 0; i < infixes.length; ++i)
                if (infixes[i].parse(parse))
                    if (right.parse(parse)) {
                        ++count;
                        applyStep.accept(infixSteps[i]);
                        continue outer;
                    }
                    else {
                        parse.pos = pos1;
                        parse.log.rollback(log1);
                    }

            for (int i = 0; i < suffixes.length; ++i)
                if (suffixes[i].parse(parse)) {
                    ++ count;
                    applyStep.accept(suffixSteps[i]);
                    continue outer;
                }

            break;
        }

        return count > 0 || !operatorRequired;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void accept (ParserVisitor visitor) {
        visitor.visit(this);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     *
     * <p>Order: left, right, infix operators, suffix operators
     */
    @Override public List<Parser> children()
    {
        return Collections.unmodifiableList(Stream.of(
                Stream.of(left),
                right != null ? Stream.of(right) : Stream.<Parser>empty(),
                Arrays.stream(infixes),
                Arrays.stream(suffixes))
            .flatMap(Function.identity())
            .collect(Collectors.toList()));
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toStringFull()
    {
        return String.format(
            "LeftExpression(left=%s, right=%s, ops=%s, suffixes=%s, operatorRequired=%b",
            left, right, Arrays.toString(infixes), Arrays.toString(suffixes), operatorRequired);
    }

    // ---------------------------------------------------------------------------------------------
}
