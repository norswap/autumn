package norswap.autumn

// -------------------------------------------------------------------------------------------------

/**
 * Type for parsers, defined as `() -> Boolean`.
 *
 * Typically, parsers will have to access the grammar, if only to read/write the input position
 * or set failure information. A reference to the grammar can be acquired in one of two ways:
 *
 * - Through lambda capture, when defining parsers inside a [Grammar] subclass.
 *
 * - Through lambda capture, via an extension function. This is typically the case
 *   for parser combinators that can be used in many grammars. See the `norswap.autumn.parsers`
 *   package for examples.
 */
typealias Parser = () -> Boolean

// -------------------------------------------------------------------------------------------------