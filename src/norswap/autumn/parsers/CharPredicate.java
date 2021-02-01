package norswap.autumn.parsers;

import norswap.autumn.Grammar;
import norswap.autumn.Parse;
import norswap.autumn.Parser;
import norswap.autumn.ParserVisitor;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.IntPredicate;

import static norswap.autumn.util.ParserStringsUtil.escapeQuotedSection;

/**
 * Matches a single character that satisfies a predicate, within {@link Parse#string}.
 *
 * <p>Since predicates are functions and cannot be printed out meaningfully, the parser has
 * a {@link #name} property that will be used to print the parser, unless a {@link #rule()} name
 * has been set for the parser.
 *
 * <p>Build with {@link Grammar#cpred(IntPredicate)}, {@link Grammar#set(int...)}, {@link Grammar#set(String)},
 * {@link Grammar#range(int, int)}, as well a a few pre-defined parsers in {@link Grammar}. Assign a name
 * with {@link norswap.autumn.Grammar.rule#named(String)}.
 */
public final class CharPredicate extends Parser
{
    // ---------------------------------------------------------------------------------------------

    /**
     * The display name for this parser, if {@link #setRule(String)} hasn't been called.
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
        if (predicate.test(parse.charAt(parse.pos))) {
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

    @Override public String toStringFull() {
        return name;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Creates a new parser that matches any single character except the nul ('\0') character.
     */
    public static CharPredicate any()
    {
        return new CharPredicate("<any char>", it -> it != 0);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Creates a new parser that matches a single {@code c} character.
     */
    public static CharPredicate single (int c)
    {
        String chars = Character.isBmpCodePoint(c)
            ? "" + (char) c
            : "" + ((char) c >> 16) + ((char) c & 0x0000FFFF);

        String name = "[" + escapeQuotedSection(chars) + "]";
        return new CharPredicate(name, it -> it == c);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Creates a new parser that matches a single character in the [start-end] range.
     */
    public static CharPredicate range (int start, int end)
    {
        String str = escapeQuotedSection(start + "-" + end);
        return new CharPredicate("[" + str + "]", it ->
            start <= it && it <= end);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Creates a new parser that matches a single character contains in {@code chars}.
     */
    public static CharPredicate set (String chars)
    {
        return new CharPredicate("[" + escapeQuotedSection(chars) + "]", it ->
            chars.indexOf(it) >= 0); // indexOf also works with code points
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Creates a new parser that matches a single character contains in {@code chars}.
     */
    public static CharPredicate set (int... chars)
    {
    	String s = new String(chars, 0, chars.length);
    	Arrays.sort(chars);
        return new CharPredicate("[" + escapeQuotedSection(s) + "]", it ->
        Arrays.binarySearch(chars, it) >= 0);
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
    public static CharPredicate hexDigit()
    {
        return new CharPredicate("<hex digit>", it ->
            '0' <= it && it <= '9' || 'a' <= it && it <= 'f' || 'A' <= it && it <= 'F');
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Creates a new parser that matches a single octal digit.
     */
    public static CharPredicate octalDigit()
    {
        return new CharPredicate("<octal digit>", it ->
            '0' <= it && it <= '7');
    }

    // ---------------------------------------------------------------------------------------------
}
