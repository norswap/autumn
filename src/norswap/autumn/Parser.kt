package norswap.autumn

// -------------------------------------------------------------------------------------------------

/**
 * Type for parsers, defined as `Grammar.() -> Boolean`.
 *
 * You can subclass this type by inheriting `(Grammar) -> Boolean` because that type can implicitly
 * be converted to `Grammar.() -> Boolean` (the underlying representation is the same), but the
 * later can't be inherited.
 */
typealias Parser = Grammar.() -> Boolean

// -------------------------------------------------------------------------------------------------