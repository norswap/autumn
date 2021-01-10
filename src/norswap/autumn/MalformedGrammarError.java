package norswap.autumn;

import norswap.autumn.visitors.WellFormednessChecker;

/**
 * Thrown by {@link Autumn}'s {@code parse} methods when the {@link
 * ParseOptions#wellFormednessCheck}  options is specified, and the supplied parser fails the {@link
 * WellFormednessChecker} check.
 */
public final class MalformedGrammarError extends Error
{
    // ---------------------------------------------------------------------------------------------

    /**
     * The well-formedness checker that failed and caused this error to be thrown.
     *
     * <p>You can inspect the uncovered well-formedness violations via this field.
     */
    public final WellFormednessChecker checker;

    // ---------------------------------------------------------------------------------------------

    MalformedGrammarError (String message, WellFormednessChecker checker) {
        super(message);
        this.checker = checker;
    }

    // ---------------------------------------------------------------------------------------------
}
