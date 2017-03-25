package norswap.uranium

// =================================================================================================

typealias ErrorConstructor = (Reaction<*>, Node) -> ReactorError

// =================================================================================================

/**
 * An error that occurs during the lifetime of a reactor (i.e. during visits, reaction executions,
 * ...).
 *
 * These error are regular events (unlike Java errors) and indicate that some attributes
 * cannot be derived because of problems in the node tree.
 */
abstract class ReactorError
{
    // ---------------------------------------------------------------------------------------------

    /**
     * The rule instance that caused the error (if any).
     */
    abstract val reaction: Reaction<*>?

    // ---------------------------------------------------------------------------------------------

    /**
     * A description of the error.
     */
    abstract val msg: String

    // ---------------------------------------------------------------------------------------------

    /**
     * The attributes whose value cannot be derived because of the error.
     *
     * Defaults to all the attributes provided by [reaction].
     */
    open val affected: List<Attribute>
       get() = reaction?.provided ?: emptyList()

    // ---------------------------------------------------------------------------------------------

    override fun toString() = msg

    // ---------------------------------------------------------------------------------------------
}

// =================================================================================================

/**
 * An error created at the end of a reactor's run, that indicates that a reaction
 * was not triggered during the run.
 */
class ReactionNotTriggered
    internal constructor (override val reaction: Reaction<*>): ReactorError()
{
    override val msg
        = "Reaction not triggered: $reaction"

    /**
     * A list of errors that caused this error.
     */
    lateinit var causes: List<ReactorError>
        internal set
}

// =================================================================================================

/**
 * An error create at the end of a reactor's run, that indicates that there were no rules
 * that could supply [attribute].
 */
class NoSupplier
    internal constructor (val attribute: Attribute): ReactorError()
{
    override val reaction
        = null

    override val msg
        = "No supplier for attribute: $attribute"

    override val affected
        = listOf(attribute)
}

// =================================================================================================

/**
 * An error meant to be carried in a [ReactorException].
 *
 * The reactor will set [reaction] when the error is caught.
 */
abstract class ReactionExceptionError: ReactorError()
{
    override var reaction: Reaction<*>? = null
        internal set
}

// =================================================================================================

/**
 * Indicates an implementation error in a reaction.
 *
 * Using a [ReactorError] rather than an exception for those enables us easily provide
 * more context, and unify error reporting.
 */
abstract class ReactionImplementationError: ReactionExceptionError()

// =================================================================================================

/**
 * Indicates that [reaction] tried to access [attribute] that wasn't defined yet.
 */
class AttributeNotDefined (val attribute: Attribute): ReactionImplementationError()
{
    override val msg = "Attribute not defined yet: $attribute"
}

// =================================================================================================

/**
 * Indicates that [reaction] tried to re-define an existing [attribute].
 */
class AttributeRedefined (val attribute: Attribute): ReactionImplementationError()
{
    override val msg = "Attribute re-defined: $attribute"

}

// =================================================================================================

/**
 * Indicate that a rule instance that should have supplied an attribute didn't do so.
 */
class AttributeNotProvided (val attribute: Attribute): ReactionImplementationError()
{
    override val msg = "Attribute not provided: $attribute"
    override val affected = listOf(attribute)
}

// =================================================================================================