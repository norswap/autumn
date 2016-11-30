@file:JvmName("ast_utils")
@file:Suppress("PackageDirectoryMismatch", "UNCHECKED_CAST")
package norswap.whimsy.ast_utils
import norswap.whimsy.Node

// -------------------------------------------------------------------------------------------------

/**
 * Transform the iterable to a sequence and casts the component type to Node.
 * Converts nulls to empty sequences.
 */
inline val Iterable<Any>?.nseq: Sequence<Node>
    get() = if (this == null) emptySequence()
            else (this as Iterable<Node>).asSequence()

// -------------------------------------------------------------------------------------------------

/**
 * Maps the iterable to a sequence and casts the component type to Node.
 */
inline fun <T: Any> Iterable<T>.nmap (crossinline f: (T) -> Any): Sequence<Node>
    = asSequence().map { f(it) } as Sequence<Node>

// -------------------------------------------------------------------------------------------------

/**
 * Filter the iterable to a sequence and casts the component type to Node.
 */
inline fun <T: Any> Iterable<T>.nfilter (crossinline f: (T) -> Boolean): Sequence<Node>
    = asSequence().filter { f(it) } as Sequence<Node>

// -------------------------------------------------------------------------------------------------

/**
 * Casts the component type of the sequence to node.
 * Converts nulls to empty sequences.
 */
inline val Sequence<Any>?.nseq: Sequence<Node>
    get() = if (this == null) emptySequence()
            else this as Sequence<Node>

// -------------------------------------------------------------------------------------------------

/**
 * Creates a sequence from the given elements, casted to Node.
 */
fun nseq (vararg elements: Any): Sequence<Node>
    = elements.asSequence() as Sequence<Node>

// -------------------------------------------------------------------------------------------------

/**
 * Creates a sequence from the given elements, casted to Node, after filtering out null elements.
 */
fun nseqN (vararg elements: Any?): Sequence<Node>
    = (elements.filterNotNull() as Array<Node>).asSequence()

// -------------------------------------------------------------------------------------------------

/**
 * Concatenate the given element to the sequence, after casting it to node. Returns the
 * same sequence if the element is null.
 */
operator fun Sequence<Node>.plus (element: Any?): Sequence<Node>
    =   if (element == null)
            this
        else
            this.plusElement(element as Node)

// -------------------------------------------------------------------------------------------------

/**
 * Concatenate the given sequences.
 */
operator fun Sequence<Node>.plus (other: Sequence<Node>): Sequence<Node>
    = sequenceOf(this, other).flatten()

// -------------------------------------------------------------------------------------------------

/**
 * Concatenates the item to the start of the sequence, after casting it to node.
 */
operator fun Any.plus (other: Sequence<Node>): Sequence<Node>
    = sequenceOf(nseq(this), other).flatten()

// -------------------------------------------------------------------------------------------------

/**
 * Casts the receiver to type [Node].
 */
inline val Any.node: Node
    get() = this as Node

// -------------------------------------------------------------------------------------------------