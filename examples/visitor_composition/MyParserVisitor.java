package visitor_composition;

import norswap.autumn.ParserVisitor;

public interface MyParserVisitor extends ParserVisitor
{
    void visit (MyParser Parser);
}
