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
 * Matches a collection of right-associative infix and prefix expressions with the same precedence.
 *
 * <p>See the definition of the various fields for more informations.
 *
 * <p>The prefix operators have priority over the infix operators, and the operators of the same
 * kind prioritize in the order in which they are given. Note that priority here doesn't mean
 * "precedence" but rather "priority" in the sense of "prioritized choice" (like {@link Choice}).
 *
 * <p>The parser tries to match as many repetitions of {@code prefix / (left infix)} as possible
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
    public final StackAction[] infixSteps;

    // ---------------------------------------------------------------------------------------------

    /** Prefix operators. */
    public final Parser[] prefixes;

    // ---------------------------------------------------------------------------------------------

    /** Stack actions associated with the corresponding prefix operators in {@link #infixes}. */
    public final StackAction[] prefixSteps;

    // ---------------------------------------------------------------------------------------------

    /** Whether a right operand can match on its own (false), or an operator is required (true). */
    public final boolean operatorRequired;

    // ---------------------------------------------------------------------------------------------

    public RightExpression (
        Parser left, Parser right,
        Parser[] infixes, StackAction[] infixSteps,
        Parser[] prefixes, StackAction[] prefixSteps,
        boolean operatorRequired)
    {
        assert right != null;
        assert left != null || infixes.length == 0;
        assert infixes.length == infixSteps.length;
        assert prefixes.length == prefixSteps.length;

        this.left = left;
        this.right = right;
        this.infixes = infixes;
        this.prefixes = prefixes;
        this.infixSteps = infixSteps;
        this.prefixSteps = prefixSteps;
        this.operatorRequired = operatorRequired;
    }

    // ---------------------------------------------------------------------------------------------

    @Override
    protected boolean doparse (Parse parse)
    {
        final int pos0 = parse.pos;
        final int size0 = parse.stack.size();
        final int whitespace0 = parse.leadingWhitespaceStart();
        final int log0 = parse.log.size();

        Consumer<StackAction> applyStep = step ->
            step.apply(new ActionContext(
                parse, parse.stack.popFrom(size0), pos0, size0,
                whitespace0, parse.trailingWhitespaceStart(pos0)));

        for (int i = 0; i < prefixes.length; ++i) {
            if (prefixes[i].parse(parse)) {
                boolean oldRecursive = parse.rightRecursive;
                parse.rightRecursive = true;
                if (doparse(parse)) {
                    parse.rightRecursive = oldRecursive;
                    applyStep.accept(prefixSteps[i]);
                    return true;
                } else {
                    parse.rightRecursive = oldRecursive;
                    parse.pos = pos0;
                    parse.log.rollback(log0);
                }
            }
        }

        if (left != null && left.parse(parse)) {
            for (int i = 0; i < infixes.length; ++i) {
                int pos1 = parse.pos;
                int log1 = parse.log.size();
                if (infixes[i].parse(parse)) {
                    boolean oldRecursive = parse.rightRecursive;
                    parse.rightRecursive = true;
                    if (doparse(parse)) {
                        parse.rightRecursive = oldRecursive;
                        applyStep.accept(infixSteps[i]);
                        return true;
                    } else {
                        parse.rightRecursive = oldRecursive;
                        parse.pos = pos1;
                        parse.log.rollback(log1);
                    }
                }
            }

            if (left == right)
                return !operatorRequired || parse.rightRecursive;
        }

        return (!operatorRequired || parse.rightRecursive) && right.parse(parse);
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

    @Override public String toStringFull()
    {
        return String.format(
            "RightExpression(left=%s, right=%s, ops=%s, prefixes=%s, operatorRequired=%b",
            left, right, Arrays.toString(infixes), Arrays.toString(prefixes), operatorRequired);
    }

    // ---------------------------------------------------------------------------------------------
}
