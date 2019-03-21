package norswap.autumn.parsers;

import norswap.autumn.DSL;
import norswap.autumn.Parse;
import norswap.autumn.Parser;
import norswap.autumn.ParserVisitor;
import java.util.Collections;
import java.util.function.IntPredicate;

import static norswap.autumn.util.ParserStringsUtil.escape_quoted_section;

/**
 * Matches a single character that satisfies a predicate, within {@link Parse#string}.
 *
 * <p>Since predicates are functions and cannot be printed out meaningfully, the parser has
 * a {@link #name} property that will be used to print the parser, unless a {@link #rule()} name
 * has been set for the parser.
 *
 * <p>Build with {@link DSL#cpred(IntPredicate)}, {@link DSL#set(char...)}, {@link DSL#set(String)},
 * {@link DSL#range(char, char)}, as well a a few pre-defined parsers in {@link DSL}
 */
public final class CharPredicate extends Parser
{
    // ---------------------------------------------------------------------------------------------

    /**
     * The display name for this parser, if {@link #set_rule(String)} hasn't been called.
     */
    public String name;

    // ---------------------------------------------------------------------------------------------

    public final IntPredicate predicate;

    // ---------------------------------------------------------------------------------------------

    /**
     * Creates a new parser that matches a single character that satisfies {@code predicate}.
     * {@code name} is used as display name for this parser.
     */
    public CharPredicate (String name, IntPredicate predicate)
    {
        this.name = name;
        this.predicate = predicate;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public boolean doparse (Parse parse)
    {
        assert parse.string != null;
        if (predicate.test(parse.char_at(parse.pos))) {
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

    @Override public String toStringFull()
    {
        return name;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Creates a new parser that matches any single character except the nul ('\0') character.
     */
    public static CharPredicate any ()
    {
        return new CharPredicate("<any char>", it -> it != 0);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Creates a new parser that matches a single {@code c} character.
     */
    public static CharPredicate single (char c)
    {
        return new CharPredicate("[" + escape_quoted_section("" + c) + "]", it -> it == c);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Creates a new parser that matches a single character in the [start-end] range.
     */
    public static CharPredicate range (char start, char end)
    {
        String str = escape_quoted_section(start + "-" + end);
        return new CharPredicate("[" + str + "]", it ->
            start <= it && it <= end);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Creates a new parser that matches a single character contains in {@code chars}.
     */
    public static CharPredicate set (String chars)
    {
        return new CharPredicate("[" + escape_quoted_section(chars) + "]", it ->
            chars.indexOf(it) >= 0);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Creates a new parser that matches a single character contains in {@code chars}.
     */
    public static CharPredicate set (char... chars)
    {
        return set(new String(chars));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Creates a new parser that matches a single ASCII alphabetic character.
     */
    public static CharPredicate alpha()
    {
        return new CharPredicate("<alpha>", it ->
            'a' <= it && it <= 'z' || 'A' <= it && it <= 'Z');
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Creates a new parser that matches a single ASCII alpha-numeric character.
     */
    public static CharPredicate alphanum()
    {
        return new CharPredicate("<alpha>", it ->
            'a' <= it && it <= 'z' || 'A' <= it && it <= 'Z' || '0' <= it && it <= '9');
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Creates a new parser that matches a single decimal digit.
     */
    public static CharPredicate digit()
    {
        return new CharPredicate("<digit>", it ->
            '0' <= it && it <= '9');
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Creates a new parser that matches a single hexadecimal digit (for letters, both the
     * lowercase and uppercase forms are allowed).
     */
    public static CharPredicate hex_digit()
    {
        return new CharPredicate("<hex digit>", it ->
            '0' <= it && it <= '9' || 'a' <= it && it <= 'f' || 'A' <= it && it <= 'F');
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Creates a new parser that matches a single octal digit.
     */
    public static CharPredicate octal_digit()
    {
        return new CharPredicate("<octal digit>", it ->
            '0' <= it && it <= '7');
    }

    // ---------------------------------------------------------------------------------------------
}
