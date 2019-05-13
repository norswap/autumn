package triplet;

import norswap.autumn.ParserVisitor;
import norswap.autumn.visitors.*;

interface ParserVisitorTriplet extends ParserVisitor
{
    void visit (CountingRepeat parser);
    void visit (CountedRepeat parser);

    interface _VisitorNullableTriplet
        extends _VisitorNullable, ParserVisitorTriplet
    {
        @Override default void visit (CountingRepeat parser) { set_result(false); }
        @Override default void visit (CountedRepeat parser)  { visit(parser.child); }
    }

    interface _VisitorFirstParsersTriplet
        extends _VisitorFirstParsers, ParserVisitorTriplet
    {
        @Override default _VisitorNullable nullable_visitor () {
            return new VisitorNullableTriplet();
        }

        @Override default void visit (CountingRepeat parser) { firsts().add(parser.child); }
        @Override default void visit (CountedRepeat parser)  { firsts().add(parser.child); }
    }

    interface _VisitorNullableRepetitionTriplet
        extends _VisitorNullableRepetition, ParserVisitorTriplet
    {
        @Override default void visit (CountingRepeat parser) { set_result(nullable(parser.child)); }
        @Override default void visit (CountedRepeat parser) { set_result(false); }
    }

    class VisitorNullableTriplet
        extends VisitorNullable implements _VisitorNullableTriplet {}

    class VisitorFirstParsersTriplet
        extends VisitorFirstParsers implements _VisitorFirstParsersTriplet {}

    class VisitorNullableRepetitionTriplet
        extends VisitorNullableRepetition implements _VisitorNullableRepetitionTriplet {}
}
