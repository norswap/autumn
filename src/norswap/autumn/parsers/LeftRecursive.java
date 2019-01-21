package norswap.autumn.parsers;

import norswap.autumn.DSL;
import norswap.autumn.Parse;
import norswap.autumn.Parser;
import norswap.autumn.ParserVisitor;
import norswap.autumn.SideEffect;
import norswap.autumn.util.ArrayStack;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * A left-recursion capable parser. The child parser passed to this parser must left-recurse
 * only through a {@link LazyParser} reference to the {@link LeftRecursive} parser! The
 * method {@link DSL#left_recursive(Function)} can automate this setup.
 *
 * <p>In brief, here is how left-recursion handling works:
 * <ol>
 * <li>The child parser is run. All left-recursive calls (i.e. nested calls to the parser at the
 * same input position) will fail immediately.</li>
 *
 * <li>After successfully parsing the child, we record its result (final input position, side
 * effects), then re-invoke it; but this time left-recursive calls will incur the result of the
 * previous successful invocation of the child.</li>
 *
 * <li>This process repeats itself until either the child fails, or the result's input position
 * stops growing.</li>
 *
 * <li>The final result will thus be that of the largest successful child parser invocation.</li>
 * </ol>
 *
 * <p><b>Beware</b> that a parser that is both left- and right-recursion will always be parsed in a
 * left-associative manner. To produce right-associative parses, use one of the {@link DSL#right}
 * methods, or manually eliminate the left-recursion.</p>
 */
public final class LeftRecursive extends Parser
{
    // ---------------------------------------------------------------------------------------------

    public final Parser child;

    // ---------------------------------------------------------------------------------------------

    private final ArrayStack<Invocation> invocations = new ArrayStack<>();

    // ---------------------------------------------------------------------------------------------

    public LeftRecursive (Parser child) {
        this.child = child;
    }

    // ---------------------------------------------------------------------------------------------

    @Override protected boolean doparse (Parse parse)
    {
        int pos0 = parse.pos;
        int log0 = parse.log.size();

        Invocation invoc = invocations.snoop();

        if (invoc != null && invoc.pos0 == pos0)
        {
            if (invoc.delta == null) return false; // failed seed

            // seed match
            parse.pos = invoc.end_pos;
            parse.apply(invoc.delta);
            return true;
        }

        invoc = new Invocation(pos0, -1, null); // failed seed
        invocations.push(invoc);

        while (child.parse(parse) && parse.pos > invoc.end_pos)
        {
            invoc.end_pos = parse.pos;
            invoc.delta = parse.delta(log0);
            parse.pos = pos0;
            parse.rollback(log0);
        }

        invocations.pop();

        if (invoc.delta == null)
            return false;

        parse.pos = invoc.end_pos;
        parse.apply(invoc.delta);
        return true;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void accept (ParserVisitor visitor) {
        visitor.visit(this);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public Iterable<Parser> children () {
        return Collections.singletonList(child);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void set_rule (String name)
    {
        child.set_rule(name + "(leftrec child)");
        super.set_rule(name);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toStringFull ()
    {
        return child.rule() != null
            ? "left_recursive(" + child + ")"
            : rule() != null
                ? rule()
                : "anonymous left_recursive";
    }

    // ---------------------------------------------------------------------------------------------

    private final class Invocation
    {
        public final int pos0;
        public int end_pos;
        public List<SideEffect> delta;

        private Invocation (int pos0, int end_pos, List<SideEffect> delta)
        {
            this.pos0 = pos0;
            this.end_pos = end_pos;
            this.delta = delta;
        }
    }

    // ---------------------------------------------------------------------------------------------
}
