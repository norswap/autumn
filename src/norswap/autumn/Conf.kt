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
 * Index of the first character in a line (used for string representation excusively).
 * Usually 0 or 1. Default is 1.
 */
var LINE_START = 1

// -------------------------------------------------------------------------------------------------

/**
 * Index of the first line (used for string representation exclusively).
 * Usually 1, which is the default.
 */
var COLUMN_START = 1

// -------------------------------------------------------------------------------------------------