package visitor_composition;

public interface _VisitorMyParserB extends MyParserVisitor, _VisitorB
{
    @Override default void visit (MyParser Parser) {}
}
