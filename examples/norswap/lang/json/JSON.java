package norswap.lang.json;

import norswap.autumn.Autumn;
import norswap.autumn.Grammar;
import norswap.autumn.ParseOptions;
import norswap.autumn.ParseResult;
import norswap.autumn.positions.LineMapString;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * https://www.json.org/
 *
 * Integer      ::= 0 | [0-9]+
 * Fractional   ::= '.' [0-9]+
 * Exponent     ::= [eE] [+-]? Integer
 * Number       ::= '-'? Integer, Fractional? Exponent?
 * HexDigit     ::= [0-9] | [a-f] | [A-F]
 * StringChar   ::= !["\] ![\u0000-\u001F] . | \ [\/bfnrt] | "\\u" HexDigit HexDigit HexDigit HexDigit
 * String       ::= '"' StringChar* '"'
 * Value        ::= String | Number | Object | Array | "true" | "false" | "null"
 * Pair         ::= String ':' Value
 * Object       ::= '{' (Pair (',' Pair)*)? '}'
 * Array        ::= '[' (Value (',' Value)*)? ']'
 *
 * Whitespace allowed after all brackets, commas, colon and values.
 */
public final class JSON extends Grammar
{
    // Lexical

    { ws = usual_whitespace; }

    public rule integer =
        choice('0', digit.at_least(1));

    public rule fractional =
        seq('.', digit.at_least(1));

    public rule exponent =
        seq(set("eE"), set("+-").opt(), integer);

    public rule number =
        seq(opt('-'), integer, fractional.opt(), exponent.opt())
        .push($ -> Double.parseDouble($.str()))
        .word();

    public rule string_char = choice(
        seq(set('"', '\\').not(), range('\u0000', '\u001F').not(), any),
        seq('\\', set("\\/bfnrt")),
        seq(str("\\u"), hex_digit, hex_digit, hex_digit, hex_digit));

    public rule string_content =
        string_char.at_least(0)
        .push($ -> $.str());

    public rule string =
        seq('"',string_content , '"')
        .word();

    public rule LBRACE   = word("{");
    public rule RBRACE   = word("}");
    public rule LBRACKET = word("[");
    public rule RBRACKET = word("]");
    public rule LPAREN   = word("(");
    public rule RPAREN   = word(")");
    public rule COLON    = word(":");
    public rule COMMA    = word(",");

    // Syntactic

    public rule value = lazy(() -> choice(
        string,
        number,
        this.object,
        this.array,
        word("true")  .as_val(true),
        word("false") .as_val(false),
        word("null")  .as_val(null)));

    public rule pair =
        seq(string, COLON, value)
        .push($ -> $.$);

    public rule object =
        seq(LBRACE, pair.sep(0, COMMA), RBRACE)
        .push($ -> Arrays.stream($.$)
            .map(x -> (Object[]) x)
            .collect(Collectors.toMap(x -> (String) x[0], x -> x[1])));

    public rule array =
        seq(LBRACKET, value.sep(0, COMMA), RBRACKET)
        .as_list(Object.class);

    public rule root = seq(ws, value);

    @Override public rule root() {
        return root;
    }

    public void parse (String inputName, String input) {
        ParseResult result = Autumn.parse(root, input, ParseOptions.get());
        if (result.fullMatch) {
            System.out.println(result.toString());
        } else {
            // debugging
            System.out.println(result.toString(new LineMapString(inputName, input), false));
            // for users
            System.out.println(result.userErrorString(new LineMapString(inputName, input)));
        }
    }

    public static void main (String[] args) {
        // failing parse example
        new JSON().parse("<test>", "{ \"test\" : // }");
    }
}
