package triplet;

import norswap.autumn.*;
import norswap.autumn.visitors.WellFormednessChecker;

/**
 * See /doc/B2-context-sensitive-parsing.md for a full explanation of this example.
 */
public final class TripletGrammar extends DSL
{
    public rule a = CountingRepeat.with(str("a"));
    public rule b = CountedRepeat .with(str("b"));
    public rule c = CountedRepeat .with(str("c"));
    public rule root = seq(a, b, c);

    public static ParseResult parse (String input)
    {
        TripletGrammar grammar = new TripletGrammar();
        return Autumn.parse(grammar.root, input,
            ParseOptions.well_formedness_check(false).get());
    }

    public static ParseResult parse_with_check (String input)
    {
        TripletGrammar grammar = new TripletGrammar();
        WellFormednessChecker checker = new WellFormednessChecker(
            new ParserVisitorTriplet.VisitorFirstParsersTriplet(),
            new ParserVisitorTriplet.VisitorNullableRepetitionTriplet());

        return Autumn.parse(grammar.root, input,
            ParseOptions.well_formedness_checker(() -> checker).get());
    }
}
