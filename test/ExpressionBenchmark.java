import norswap.autumn.Autumn;
import norswap.autumn.Grammar;
import norswap.autumn.ParseOptions;
import norswap.autumn.ParseResult;
import norswap.utils.Strings;
import java.time.Duration;

/**
 * Demonstration of the inefficiency of writing an "expression tower" (including many types of
 * binary operators with different precedences) using right-recursion, versus using {@link
 * Grammar#left_expression()} and {@link Grammar#right_expression()}.
 */
public final class ExpressionBenchmark
{
    public final static class BadRightExpression extends Grammar
    {
        rule number = range('0', '9').at_least(1);
        rule ground = lazy(() -> choice(seq('(', this.expr, ')'), number));

        rule product = lazy(() -> choice(
            seq(ground, '*', this.product),
            seq(ground, '/', this.product),
            ground));

        rule sum = lazy(() -> choice(
            seq(product, '+', this.sum),
            seq(product, '-', this.sum),
            product));

        rule shift = lazy(() -> choice(
            seq(sum, "<<", this.shift),
            seq(sum, ">>", this.shift),
            seq(sum, "<<<", this.shift),
            sum));

        rule cmp = lazy(() -> choice(
            seq(shift, "==", this.cmp),
            seq(shift, "!=", this.cmp),
            seq(shift, ">=", this.cmp),
            seq(shift, "<=", this.cmp),
            seq(shift, '>', this.cmp),
            seq(shift, '<', this.cmp),
            shift));

        rule expr = seq(cmp);

        @Override public rule root () {
            return expr;
        }
    }

    public final static class GoodRightExpression extends Grammar
    {
        rule number = range('0', '9').at_least(1);
        rule ground = lazy(() -> choice(seq('(', this.expr, ')'), number));

        rule product = right_expression()
            .operand(ground)
            .infix('*')
            .infix('/');

        rule sum = right_expression()
            .operand(product)
            .infix('+')
            .infix('-');

        rule shift = right_expression()
            .operand(sum)
            .infix("<<")
            .infix(">>")
            .infix("<<<");

        rule cmp = right_expression()
            .operand(shift)
            .infix("==")
            .infix("!=")
            .infix(">=")
            .infix("<=")
            .infix('>')
            .infix('<');

        rule expr = seq(cmp);

        @Override public rule root () {
            return expr;
        }
    }

    public static void main (String[] args)
    {
        // any more repetition and the bad grammar causes a stack overflow
        String input = Strings.repeat("(42)+", 3000) + "(42)";

        ParseResult result;
        long time;

        Grammar bad = new BadRightExpression();
        Grammar good = new GoodRightExpression();
        ParseOptions options = ParseOptions.wellFormednessCheck(false).get();

        time = System.nanoTime();
        result = Autumn.parse(bad, input, options);
        if (!result.fullMatch)
            System.out.println("bad failed");
        time = System.nanoTime() - time;
        System.out.println("Bad parsed in: " + Duration.ofNanos(time));

        time = System.nanoTime();
        result = Autumn.parse(good, input, options);
        if (!result.fullMatch)
            System.out.println("good failed");
        time = System.nanoTime() - time;
        System.out.println("Good parsed in: " + Duration.ofNanos(time));

        // result: bad = 4.14s // good = 0.017s
    }
}
