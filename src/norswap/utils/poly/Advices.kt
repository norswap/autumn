package norswap.utils.poly
import norswap.utils.Advice1

// -------------------------------------------------------------------------------------------------

/**
 * A polymorphic [Advice1].
 * The polymorphism is predicated on the parameter.
 */
open class PolyAdvice <Arg: Any, Out> (inheriting: Boolean = true)
    : Poly2<Arg, Boolean, Out>(inheriting)
    , Advice1<Arg, Out>

// -------------------------------------------------------------------------------------------------

/**
 * Like [PolyAdvice] with an `Out` type of `Unit`, and a default advice of doing nothing.
 */
open class PolyAdviceUnit <Arg: Any> (inheriting: Boolean = true)
    : PolyAdvice<Arg, Unit>(inheriting)
{
    // default advice: do nothing
    init { default = { _,_ -> } }
}

// -------------------------------------------------------------------------------------------------