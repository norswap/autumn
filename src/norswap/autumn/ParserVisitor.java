package norswap.autumn;

import norswap.autumn.parsers.*;

/**
 * A visitor interface for the built-in implementations of {@link Parser}.
 */
public interface ParserVisitor
{
    // ---------------------------------------------------------------------------------------------

    /**
     * Catch-all rule in case someone doesn't want to implement a visitor interface for a
     * custom parser.
     *
     * TODO further reference
     */
    void visit (Parser parser);

    // ---------------------------------------------------------------------------------------------

    void visit (Around parser);
    void visit (CharPredicate parser);
    void visit (Choice parser);
    void visit (Collect parser);
    void visit (LazyParser parser);
    void visit (LeftAssoc parser);
    void visit (Longest parser);
    void visit (Lookahead parser);
    void visit (Not parser);
    void visit (ObjectPredicate parser);
    void visit (Optional parser);
    void visit (Repeat parser);
    void visit (Sequence parser);
    void visit (StringMatch parser);
    void visit (TokenParser parser);

    // ---------------------------------------------------------------------------------------------
}
