package norswap.autumn.parsers;

import norswap.autumn.Parse;
import norswap.autumn.Parser;
import java.util.function.BiConsumer;

/**
 * Matches a left-associative binary expression. See {@link #LeftAssoc}.
 */
public final class LeftAssoc extends Parser
{
    // ---------------------------------------------------------------------------------------------

    public final Parser left;

    // ---------------------------------------------------------------------------------------------

    public final Parser right;

    // ---------------------------------------------------------------------------------------------

    public final Parser operator;

    // ---------------------------------------------------------------------------------------------

    public final boolean operator_required;

    // ---------------------------------------------------------------------------------------------

    public final BiConsumer<Parse, Object[]> consumer;

    // ---------------------------------------------------------------------------------------------

    /**
     * Matches a left-associative binary expression (specified by {@code left}, {@code right} and
     * {@code operator}).
     *
     * @param operator_required specifies whether at least one operator should
     * be present or if a left-hand side alone is admissible.
     *
     * @param consumer is applied immediately after a right-hand side has been matched, enabling
     * left-associative tree building.
     */
    public LeftAssoc (Parser left, Parser operator, Parser right,
                      boolean operator_required, BiConsumer<Parse, Object[]> consumer)
    {
        this.left = left;
        this.operator = operator;
        this.right = right;
        this.operator_required = operator_required;
        this.consumer = consumer;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public boolean doparse (Parse parse)
    {
        int size0 = parse.stack.size();

        if (!left.parse(parse))
            return false;

        if (operator_required)
            if (!(operator.parse(parse) && right.parse(parse)))
                return false;
            else
                consumer.accept(parse, parse.pop_from(size0));

        while (true)
            if (!(operator.parse(parse) && right.parse(parse)))
                break;
            else
                consumer.accept(parse, parse.pop_from(size0));

        return true;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toStringFull()
    {
        StringBuilder b = new StringBuilder();
        b.append("left_assoc(");
        b.append(left)      .append(", ");
        b.append(operator)  .append(", ");
        b.append(right);
        if (operator_required)
            b.append(", operator_required");
        b.append(")");
        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------
}
