package norswap.utils.poly
import norswap.utils.Reducer
import norswap.utils.ReducerAdvice

// -------------------------------------------------------------------------------------------------

/**
 * A polymorphic [Reducer].
 * The polymorphism is predicated on the parameter.
 */
open class PolyReducer <Arg: Any, Out> (inheriting: Boolean = true)
    : Poly2<Arg, Array<Out>, Out>(inheriting)
    , Reducer<Arg, Out>

// -------------------------------------------------------------------------------------------------

/**
 * A polymorphic [ReducerAdvice].
 * The polymorphism is predicated on the parameter.
 */
open class PolyReducerAdvice <Arg: Any, Out> (inheriting: Boolean = true)
    : Poly2<Arg, Array<Out>?, Out>(inheriting)
    , ReducerAdvice<Arg, Out>

// -------------------------------------------------------------------------------------------------
