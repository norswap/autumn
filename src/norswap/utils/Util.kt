package norswap.utils
import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.Arrays

// -------------------------------------------------------------------------------------------------

/**
 * Shorthand for [StringBuilder.append].
 */
operator fun StringBuilder.plusAssign(o: Any?) { append(o) }

// -------------------------------------------------------------------------------------------------

/**
 * Use this to enable Kotlin smart casts at no cost: include a value cast as the parameter to
 * this function, and the code that follows will be able to assume that the value is of the
 * type it was casted to.
 *
 * e.g. `proclaim (vehicle as Truck)`
 */
@Suppress("UNUSED_PARAMETER", "NOTHING_TO_INLINE")
inline fun proclaim (cast: Any)
    = Unit

// -------------------------------------------------------------------------------------------------

/**
 * Shorthand for [Arrays.toString].
 */
inline val <T> Array<T>.str: String
    get() = Arrays.toString(this)

// -------------------------------------------------------------------------------------------------

/**
 * Converts this alphanum CamelCase string to snake_case.
 * @author http://stackoverflow.com/questions/10310321
 */
fun String.camel_to_snake(): String
{
    return this
        .replace("([a-z0-9])([A-Z]+)".toRegex(), "$1_$2")
        .toLowerCase()
}

// -------------------------------------------------------------------------------------------------

/**
 * Converts this alphanum snake_case string to CamelCase.
 */
fun String.snake_to_camel(): String
{
    return this
        .split('_')
        .map(String::capitalize)
        .joinToString()
}

// -------------------------------------------------------------------------------------------------

/**
 * Reads a complete file and returns its contents as a string.
 * @throws IOException see [Files.readAllBytes]
 * @throws InvalidPathException see [Paths.get]
 */
fun readFile(file: String)
    = String(Files.readAllBytes(Paths.get(file)))

// -------------------------------------------------------------------------------------------------

/**
 * Returns a list of all the paths that match the given glob pattern within the given directory.
 *
 * The pattern syntax is described in the doc of [FileSystem.getPathMatcher] --
 * the "glob:" part should be omitted.
 */
@Throws(IOException::class)
fun glob(pattern: String, directory: Path): List<Path>
{
    val matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern)

    val result = ArrayList<Path>()

    Files.walkFileTree (directory, object : SimpleFileVisitor<Path>()
    {
        override fun visitFile (file: Path, attrs: BasicFileAttributes): FileVisitResult
        {
            if (matcher.matches(file)) result.add(file)
            return FileVisitResult.CONTINUE
        }

        override fun visitFileFailed (file: Path, exc: IOException?)
            = FileVisitResult.CONTINUE
    })

    return result
}

// -------------------------------------------------------------------------------------------------

/**
 * True iff the receiver is a subtype of (i.e. is assignable to) the parameter.
 */
infix fun Class<*>.extends(other: Class<*>): Boolean
    = other.isAssignableFrom(this)

/**
 * True iff the receiver is a supertype of (i.e. can be assigned from) the parameter.
 */
infix fun Class<*>.supers (other: Class<*>): Boolean
    = this.isAssignableFrom(other)

/**
 * True iff the type parameter is a subtype of (i.e. is assignable to) the parameter.
 */
inline fun <reified T: Any> extends(other: Class<*>): Boolean
    = T::class.java extends other

/**
 * True iff the type parameter is a supertype of (i.e. can be assigned from) the parameter.
 */
inline fun <reified T: Any> supers (other: Class<*>): Boolean
    = T::class.java supers other

// -------------------------------------------------------------------------------------------------

/**
 * Returns an array of the given size, populated with nulls, but casted to an array of non-nullable
 * items. This is unsafe, but handy when an array has to be allocated just to be populated
 * immediately, but the map-style [Array] constructor is not convenient. It also helps construct
 * array of nulls for non-reifiable types.
 */
fun <T> arrayOfSize (size: Int): Array<T>
{
    @Suppress("UNCHECKED_CAST")
    return arrayOfNulls<Any>(size) as Array<T>
}

// -------------------------------------------------------------------------------------------------

/**
 * Helper method to use a `MutableMap<K, ArrayList<V>>` as a `MultiMap<K, V>`.
 * If the key doesn't have a value (list) yet, inserts a list with the value, otherwise appends
 * the value to the list.
 */
fun <K, V> MutableMap<K, ArrayList<V>>.append (k: K, v: V)
{
    var array = this[k]

    if (array == null) {
        array = ArrayList()
        put(k, array)
    }

    array.add(v)
}

// -------------------------------------------------------------------------------------------------