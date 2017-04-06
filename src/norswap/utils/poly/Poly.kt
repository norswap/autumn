package norswap.utils.poly

// =================================================================================================
/*

Polymorphic operations, as implemented in this file, are my own ad-hoc answer to the
Expression Problem.

"The expression problem is a new name for an old problem. The goal is to define a datatype by
cases, where one can add new cases to the datatype and new functions over the datatype, without
recompiling existing code, and while retaining static type safety (e.g., no casts)."

Additionally, it is desirable to be able to use independently-developped extensions
(new datatypes or functions) together, a property known as "independent extensibility".

Functions are defined as instances of the PolyXXX class below. Each of these classes
is polymorphic on one of its type parameters. The user can supply specializations of the
operation to subclasses of this type parameter.

There is one major "unsafety": it is the user's responsability to ensure that each operation
is defined on all subclasses on which it could be used. It is also possible to define
a default implementation.

Another pitfall: all calls must go through a hash table lookup.

*/

// =================================================================================================

/**
 * A polymorphic operation with no parameters.
 * The polymorphism is predicated on the return type.yes
 */
open class Poly0 <Out: Any> (inheriting: Boolean = true)
    : Specialized<Out, () -> Out> (inheriting)
{
    inline fun <reified T: Out> invoke ()
        = for_class(T::class.java)()
}

// =================================================================================================

/**
 * A polymorphic operation with a single parameter.
 * The polymorphism is predicated on the parameter.
 */
open class Poly1 <Arg: Any, Out> (inheriting: Boolean = true)
    : Specialized<Arg, (Arg) -> Out>(inheriting)
    , (Arg) -> Out
{
    // ---------------------------------------------------------------------------------------------

    override fun invoke (arg: Arg)
        = for_instance(arg)(arg)

    // ---------------------------------------------------------------------------------------------

    inline fun <reified Case : Arg> on (noinline value: (Case) -> Out) {
        @Suppress("UNCHECKED_CAST")
        super.bind<Case>(value as (Arg) -> Out)
    }

    // ---------------------------------------------------------------------------------------------

    open fun <Case: Arg> on (klass: Class<Case>, value: (Case) -> Out) {
        @Suppress("UNCHECKED_CAST")
        super.bind(klass, value as (Arg) -> Out)
    }

    // ---------------------------------------------------------------------------------------------

    fun default (value: (Arg) -> Out) {
        default = value
    }
}

// =================================================================================================

/**
 * A polymorphic operation with two parameters.
 * The polymorphism is predicated on the first parameter.
 */
open class Poly2 <Arg1: Any, Arg2, Out> (inheriting: Boolean = true)
    : Specialized<Arg1, (Arg1, Arg2) -> Out>(inheriting)
    , (Arg1, Arg2) -> Out
{
    // ---------------------------------------------------------------------------------------------

    override fun invoke (arg1: Arg1, arg2: Arg2)
        = for_instance(arg1)(arg1, arg2)

    // ---------------------------------------------------------------------------------------------

    inline fun <reified Case : Arg1> on (noinline value: (Case, Arg2) -> Out) {
        @Suppress("UNCHECKED_CAST")
        super.bind<Case>(value as (Arg1, Arg2) -> Out)
    }

    // ---------------------------------------------------------------------------------------------

    open fun <Case: Arg1> on (klass: Class<Case>, value: (Case, Arg2) -> Out) {
        @Suppress("UNCHECKED_CAST")
        super.bind(klass, value as (Arg1, Arg2) -> Out)
    }
}

// =================================================================================================

/**
 * A polymorphic operation with three parameters.
 * The polymorphism is predicated on the first parameter.
 */
open class Poly3 <Arg1: Any, Arg2, Arg3, Out> (inheriting: Boolean = true)
    : Specialized<Arg1, (Arg1, Arg2, Arg3) -> Out>(inheriting)
    , (Arg1, Arg2, Arg3) -> Out
{
    // ---------------------------------------------------------------------------------------------

    override fun invoke (arg1: Arg1, arg2: Arg2, arg3: Arg3)
        = for_instance(arg1)(arg1, arg2, arg3)

    // ---------------------------------------------------------------------------------------------

    inline fun <reified Case : Arg1> on (noinline value: (Case, Arg2, Arg3) -> Out) {
        @Suppress("UNCHECKED_CAST")
        super.bind<Case>(value as (Arg1, Arg2, Arg3) -> Out)
    }

    // ---------------------------------------------------------------------------------------------

    open fun <Case: Arg1> on (klass: Class<Case>, value: (Case, Arg2, Arg3) -> Out) {
        @Suppress("UNCHECKED_CAST")
        super.bind(klass, value as (Arg1, Arg2, Arg3) -> Out)
    }
}

// =================================================================================================