package lang.java;

import norswap.lang.java.Grammar;
import norswap.lang.java.GrammarTokens;
import norswap.lang.java.Lexer;
import norswap.lang.java.Token;
import norswap.lang.java.ast.Literal;
import java.util.Arrays;

public final class TestGrammarTokens extends TestGrammar {

    // ---------------------------------------------------------------------------------------------

    public TestGrammarTokens() {
        super(new GrammarTokens());
        Literal.test_convert_hook = TestGrammarTokens::convert_literal_value;
        lexer = string -> Arrays.asList(new Lexer(string).lex());
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Because {@link Grammar} and {@link GrammarTokens} use different literal values in the AST,
     * we need to convert them when testing so that they end up comparing equivalent.
     *
     * <p>We can hook the AST by setting {@link Literal#test_convert_hook} to this method.
     */
    public static Object convert_literal_value (Object value) {
        if (!(value instanceof Token))
            return value.toString();

        Token token = (Token) value;
        String radix_prefix = token.radix == 16 ? "0x" : "";
        switch (token.kind) {
            case DOUBLELITERAL:
                return Double.valueOf(radix_prefix + token.string).toString();
            case FLOATLITERAL:
                return Float.valueOf(radix_prefix + token.string).toString();
            case LONGLITERAL:
                return Long.valueOf(token.string, token.radix).toString();
            case INTLITERAL:
                return Integer.valueOf(token.string, token.radix).toString();
            default:
                return token.string;
        }
    }

    // ---------------------------------------------------------------------------------------------
}
