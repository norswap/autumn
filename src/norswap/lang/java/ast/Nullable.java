package norswap.lang.java.ast;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * This is not an AST node, but a nullable annotation for the benefit of AutoValue.
 *
 * <p>cf. https://github.com/google/auto/issues/283
 */
@Target(TYPE_USE)
@Retention(SOURCE)
public @interface Nullable {}