package visitor_composition;

import norswap.autumn.Parser;
import norswap.autumn.ParserVisitor;
import norswap.autumn.parsers.*;

public interface _VisitorA extends ParserVisitor
{
    int some_storage();

    @Override default void visit (Parser parser) {}

    @Override default void visit (Around parser) {}

    @Override default void visit (CharPredicate parser) {}

    @Override default void visit (Choice parser) {}

    @Override default void visit (Collect parser) {}

    @Override default void visit (Empty parser) {}

    @Override default void visit (Fail parser) {}

    @Override default void visit (Forwarding parser) {}

    @Override default void visit (GuardedRecursion parser) {}

    @Override default void visit (LazyParser parser) {}

    @Override default void visit (LeftAssoc parser) {}

    @Override default void visit (LeftRecursive parser) {}

    @Override default void visit (Longest parser) {}

    @Override default void visit (Lookahead parser) {}

    @Override default void visit (Memo parser) {}

    @Override default void visit (Not parser) {}

    @Override default void visit (ObjectPredicate parser) {}

    @Override default void visit (Optional parser) {}

    @Override default void visit (Repeat parser) {}

    @Override default void visit (RightAssoc parser) {}

    @Override default void visit (Sequence parser) {}

    @Override default void visit (StringMatch parser) {}

    @Override default void visit (TokenChoice parser) {}

    @Override default void visit (TokenParser parser) {}
}
