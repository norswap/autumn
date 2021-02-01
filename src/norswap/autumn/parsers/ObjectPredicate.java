package norswap.autumn.parsers;

import norswap.autumn.Parse;
import norswap.autumn.Parser;
import norswap.autumn.ParserVisitor;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Matches a single object that satisfies a predicate, within {@link Parse#list}.
 *
 * <p>Since predicates are functions and cannot be printed out meaningfully, the parser has
 * a {@link #name} property that will be used to print the parser, unless a {@link #rule()} name
 * has been set for the parser.
 *
 * <p>Build with {@link norswap.autumn.Grammar#opred(Predicate)} and name with {@link
 * norswap.autumn.Grammar.rule#named(String)}.
 */
public final class ObjectPredicate extends Parser
{
    // ---------------------------------------------------------------------------------------------

    /**
     * The display name for this parser, if {@link #setRule(String)} hasn't been called.
     */
    public String name;

    // ---------------------------------------------------------------------------------------------

    public final Predicate<Object> predicate;

    // ---------------------------------------------------------------------------------------------

    /**
     * Creates a new parser that matches a single object that satisfies {@code predicate}.
     * {@code name} is used as display name for this parser.
     */
    public ObjectPredicate (String name, Predicate<Object> predicate)
    {
        this.name = name;
        this.predicate = predicate;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public boolean doparse (Parse parse)
    {
        assert parse.list != null;
        if (predicate.test(parse.objectAt(parse.pos))) {
            ++ parse.pos;
            return true;
        }
        return false;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void accept (ParserVisitor visitor) {
        visitor.visit(this);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public Iterable<Parser> children() {
        return Collections.emptyList();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Creates a new parser that matches a single object with class {@code klass}.
     */
    public static ObjectPredicate instance (Class<?> klass)
    {
        return new ObjectPredicate("<is? " + klass.getSimpleName() + ">", klass::isInstance);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Creates a new parser that matches any single non-null object.
     */
    public static ObjectPredicate any()
    {
        return new ObjectPredicate("<any object>", Objects::nonNull);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toStringFull() {
        return name;
    }

    // ---------------------------------------------------------------------------------------------
}
