package norswap.autumn.actions;

import norswap.autumn.parsers.Collect;
import norswap.autumn.parsers.LeftExpression;
import norswap.autumn.parsers.RightExpression;

/**
 * A {@link StackAction} sub-interface setup such that you have to implement a method that returns
 * a boolean. This boolean is intended to control the success status of the parser consuming
 * the action - the parser should fail if the action returns false.
 *
 * <p>Within the standard parsers, this can only be used with {@link Collect} (usually via {@link
 * norswap.autumn.Grammar.rule#filter}, not with {@link LeftExpression} or {@link RightExpression}.
 */
public interface StackPredicate extends StackAction
{
    @Override default boolean apply (ActionContext context) {
        return test(context);
    }

    boolean test (ActionContext context);
}
