package lang.json;

import norswap.autumn.DSL;

/**
 * https://www.json.org/
 *
 * Integer      ::= 0 | [0-9]+
 * Fractional   ::= '.' [0-9]+
 * Exponent     ::= [eE] [+-]? Integer
 * Number       ::= '-'? Integer, Fractional? Exponent?
 * HexDigit     ::= [0-9] | [a-f] | [A-F]
 * StringChar   ::= !["\] ![\u0000-\u001F] . | \ [\/bfnrt] | "\u" HexDigit HexDigit HexDigit HexDigit
 * String       ::= '"' StringChar* '"'
 * Value        ::= String | Number | Object | Array | "true" | "false" | "null"
 * Pair         ::= String ':' Value
 * Object       ::= '{' (Pair (',' Pair)*)? '}'
 * Array        ::= '[' (Value (',' Value)*)? ']'
 *
 * Whitespace allowed after all brackets, commas, colon and values.
 */
public final class JSON extends DSL
{
    { ws = usual_whitespace; }

    public rule integer = choice(
        character('0'),
        digit.at_least(1));

    public rule fractional =
        seq(character('.'), digit.at_least(1));

    public rule exponent =
        seq(set("eE"), set("+-").opt(), integer);

    public rule number =
        seq(character('-').opt(), integer, fractional.opt(), exponent.opt()).word();

    public rule string_char = choice(
        seq(set('"', '\\').not(), range('\u0000', '\u001F').not(), any),
        seq(character('\\'), set("\\/bfnrt")),
        seq(str("\\u"), hex_digit, hex_digit, hex_digit, hex_digit));

    public rule string =
        seq(character('"'), string_char.at_least(0), character('"')).word();

    public rule value = lazy(() -> choice(
        string,
        number,
        this.object,
        this.array,
        word("true"),
        word("false"),
        word("null")));

    public rule pair =
        seq(string, word(":"), value);

    public rule object =
        seq(word("{"), pair.sep(0, word(",")), word("}"));

    public rule array =
        seq(word("["), value.sep(0, word(",")), word("]"));

    public rule root = seq(ws, value);

    { make_rule_names(); }
}
