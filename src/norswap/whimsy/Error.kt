package norswap.whimsy

// -------------------------------------------------------------------------------------------------

class Error (
    val rule_instance: RuleInstance<*>,
    val msg: String,
    vararg val affected: Attribute
)

// -------------------------------------------------------------------------------------------------