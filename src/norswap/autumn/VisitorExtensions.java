package norswap.autumn;

import norswap.autumn.ParserVisitor.Overloads;
import norswap.utils.data.wrappers.Pair;
import java.util.HashMap;
import java.util.function.BiConsumer;

import static norswap.utils.Util.cast;

/**
 * This is a private implementation class used by {@link ParserVisitor} to manage the
 * {@link Overloads}
 */
final class VisitorExtensions
{
    // ---------------------------------------------------------------------------------------------

    // Types are under-specified, but this is all private.

    /** Maps visitor classes to overloads. */
    HashMap<Class<? extends ParserVisitor>, Overloads> store = new HashMap<>();

    /** Caches the last retrieved set of overloads. */
    Pair<Class<? extends ParserVisitor>, Overloads> cached = new Pair<>(null, null);

    // ---------------------------------------------------------------------------------------------

    /**
     * Implementaiton for {@link ParserVisitor#extend(Class, Class, BiConsumer)}.
     */
    synchronized <V extends ParserVisitor, P extends Parser>
    void extend (Class<V> vclass, Class<P> pclass, BiConsumer<P, V> implem)
    {
        Overloads ov = store.get(vclass);
        if (ov == null) ov = new ParserVisitor.HashOverloads(vclass); // adds itself to the store
        ov.put(pclass, cast(implem));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Retrieve the set of overloads for the given class.
     */
    Overloads overloads (Class<? extends ParserVisitor> vclass)
    {
        // NOTE: The caching logic is thread-safe: no matter which cache entry ends up written, the
        // state never ends up inconsistent.

        Pair<Class<? extends ParserVisitor>, Overloads> cached = this.cached;

        if (cached.a == vclass)
            return cached.b;

        Overloads ov = store.get(vclass);

        if (this.cached == null)
            this.cached = new Pair<>(vclass, ov);

        return ov;
    }

    // ---------------------------------------------------------------------------------------------
}
