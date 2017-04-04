package norswap.utils

// -------------------------------------------------------------------------------------------------

/**
 * Returns a version of this string where all tabs have been fully expanded, so that each tab
 * brings the column (counted from 0 starting at each newline) to a multiple of [tabSize].
 *
 * If [tabSize] is 0, no tab expansion is performed.
 */
// Courtesy of http://stackoverflow.com/a/34933524/298664
fun CharSequence.expandTabsToBuilder (tabSize: Int): StringBuilder
{
    val b = StringBuilder()

    if (tabSize == 0) {
        b += this
        return b
    }
    else if (tabSize < 0)
        throw IllegalArgumentException("negative tab size")

    var col = 0
    for (c in this) when (c) {
        '\n' -> {
            b += c
            col = 0
        }
        '\t' -> {
            val spaces = tabSize - col % tabSize
            repeat(spaces) { b += " " }
            col += spaces
        }
        else -> {
            b += c
            ++col
        }   }

    return b
}

// -------------------------------------------------------------------------------------------------

/**
 * Returns a version of this string where all tabs have been fully expanded, so that each tab
 * brings the column (counted from 0 starting at each newline) to a multiple of [tabSize].
 *
 * If [tabSize] is 0, no tab expansion is performed.
 */
fun String.expandTabs (tabSize: Int): String
{
    return expandTabsToBuilder(tabSize).toString()
}

// -------------------------------------------------------------------------------------------------