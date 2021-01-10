import norswap.autumn.Parse;
import norswap.autumn.Parser;
import norswap.autumn.ParserVisitor;
import norswap.autumn.visitors.VisitorFirstParsers;
import norswap.autumn.visitors.VisitorNullable;
import norswap.autumn.visitors.VisitorNullableRepetition;
import norswap.utils.Strings;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This example showcases how a custom parser can extend existing visitors.
 *
 * <p>The logic of the parser itself is a copy paste of the Sequence parser.
 */
public final class CustomParser extends Parser
{
    // =============================================================================================
    // CUSTOM PARSER SPECIFIC

    static {
        ParserVisitor.extend(VisitorNullable.class, CustomParser.class,
            (parser, visitor) -> visitor.addIfAllNullable(parser, parser.children()));

        ParserVisitor.extend(VisitorFirstParsers.class, CustomParser.class,
            (parser, visitor) -> visitor.firstsAddSequence(parser.children()));

        ParserVisitor.extend(VisitorNullableRepetition.class, CustomParser.class,
            (parser, visitor) -> visitor.result = false);
    }

    // =============================================================================================
    // USUAL LOGIC

    private final Parser[] children;

    // ---------------------------------------------------------------------------------------------

    @Override public List<Parser> children() {
        return Collections.unmodifiableList(Arrays.asList(children));
    }

    // ---------------------------------------------------------------------------------------------

    public CustomParser (Parser... children) {
        this.children = children;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public boolean doparse (Parse parse)
    {
        for (Parser child: children)
            if (!child.parse(parse))
                return false;
        return true;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toStringFull()
    {
        StringBuilder b = new StringBuilder();
        b.append("sequence(");
        for (Parser child: children)
            b.append(child).append(", ");
        if (children.length > 0)
            Strings.pop(b, 2);
        b.append(")");
        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------
}
