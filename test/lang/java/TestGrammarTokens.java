package lang.java;

import norswap.lang.java.GrammarTokens;
import norswap.lang.java.Lexer;
import java.util.Arrays;

public final class TestGrammarTokens extends TestGrammar {
    public TestGrammarTokens() {
        super(new GrammarTokens());
        lexer = string -> Arrays.asList(new Lexer(string).lex());
    }
}
