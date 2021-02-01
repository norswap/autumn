package lang.java;

import norswap.lang.java.JavaGrammarTokens;
import norswap.lang.java.Lexer;
import norswap.lang.java.Token;
import norswap.lang.java.ast.Literal;
import java.util.Arrays;

public final class TestGrammarTokens extends TestGrammar {

    // ---------------------------------------------------------------------------------------------

    public TestGrammarTokens() {
        super(new JavaGrammarTokens());
        Literal.testConvertHook = TestGrammarTokens::convertLiteralValue;
        lexer = string -> Arrays.asList(new Lexer(string).lex());
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Because {@link JavaGrammar} and {@link JavaGrammarTokens} use different literal values in the AST,
     * we need to convert them when testing so that they end up comparing equivalent.
     *
     * <p>We can hook the AST by setting {@link Literal#testConvertHook} to this method.
     */
    public static Object convertLiteralValue (Object value) {
        if (!(value instanceof Token))
            return value.toString();

        Token token = (Token) value;
        String radixPrefix = token.radix == 16 ? "0x" : "";
        switch (token.kind) {
            case DOUBLELITERAL:
                return Double.valueOf(radixPrefix + token.string).toString();
            case FLOATLITERAL:
                return Float.valueOf(radixPrefix + token.string).toString();
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
