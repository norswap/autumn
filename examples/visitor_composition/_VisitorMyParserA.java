package visitor_composition;

public interface _VisitorMyParserA extends MyParserVisitor, _VisitorA
{
    @Override default void visit (MyParser Parser) {}
}
