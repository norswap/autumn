@file:Suppress("PackageDirectoryMismatch")
package norswap.autumn.conf

// -------------------------------------------------------------------------------------------------

/*

Customize for your editor / coding style of choice.

Currently, all these values are defaults for [ParseInput]'s constructor, and so
can be overriden for a specific parse by constructing a ParseInput manually.

 */

// -------------------------------------------------------------------------------------------------

/**
 * Size of tabs, used to display text positions.
 *
 * If 0, specifies that tab should not be expanded.
 * Otherwise, all tab characters will be replaced by space characters so that the tab brings
 * the line position to the next multiple of TAB_SIZE.
 */
var TAB_SIZE = 4

// -------------------------------------------------------------------------------------------------

/**
 * Index of the first line (only impacts string representations).
 * Usually 1, which is the default.
 */
var LINE_START = 1

// -------------------------------------------------------------------------------------------------

/**
 * Index of the first character in a line (only impacts string representations).
 * Usually 0 (e.g. Emacs) or 1 (e.g. IntelliJ IDEA). The default is 1.
 */
var COLUMN_START = 1

// -------------------------------------------------------------------------------------------------