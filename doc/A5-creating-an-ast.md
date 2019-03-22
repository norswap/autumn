# A5. Creating an Abstract Syntax Tree (AST)

```java
package lang.json;

import norswap.autumn.Autumn;
import norswap.autumn.DSL;
import norswap.autumn.ParseOptions;
import norswap.autumn.ParseResult;
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
        seq(character('-').opt(), integer, fractional.opt(), exponent.opt())
        .push_with_string((p,xs,str) -> Double.parseDouble(str))
        .word();

    public rule string_char = choice(
        seq(set('"', '\\').not(), range('\u0000', '\u001F').not(), any),
        seq(character('\\'), set("\\/bfnrt")),
        seq(str("\\u"), hex_digit, hex_digit, hex_digit, hex_digit));

    public rule string =
        seq(character('"'), string_char.at_least(0), character('"'))
            .push_with_string((p,xs,str) -> str.substring(1, str.length() - 1))
            .word();

    public rule value = lazy(() -> choice(
        string,
        number,
        this.object,
        this.array,
        word("true")  .as_val(true),
        word("false") .as_val(false),
        word("null")  .as_val(null)));

    public rule pair =
        seq(string, ":", value)
        .push((p,xs) -> xs);

    public rule object =
        seq("{", pair.sep(0, ","), "}")
        .push((p,xs) ->
            Arrays.stream((Object[][]) xs).collect(Collectors.toMap(x -> $(x[0]), x -> x[1])));

    public rule array =
        seq("[", value.sep(0, ","), "]")
        .as_list(Object.class);

    public rule root = seq(ws, value);

    { make_rule_names(); }

    public ParseResult parse (String input) {
        return Autumn.parse(root, input, ParseOptions.get());
    }
}
```