package norswap.autumn.naive

// -------------------------------------------------------------------------------------------------
/*

This file contains parsers that match at the character level.

 */
// -------------------------------------------------------------------------------------------------

/**
 * Matches any character that satisfied [pred].
 */
class Char_pred (pred: (Char) -> Boolean): Parser()
{
    override fun invoke(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches any character.
 * Only fails when the end of the input (represented by the null byte) is reached.
 */
class Char_any(): Parser()
{
    override fun invoke(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches any character in the range between [start] and [end].
 */
class Char_range (start: Char, end: Char): Parser()
{
    override fun invoke(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches any of the character in [chars].
 */
class Char_set (vararg chars: Char): Parser()
{
    override fun invoke(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches any of the characters in [chars].
 */
/*class Char_set (chars: String): Parser()
{
    override fun invoke(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}*/

// -------------------------------------------------------------------------------------------------

/**
 * Matches [str].
 */
class String_ (str: String): Parser()
{
    override fun invoke(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
// -------------------------------------------------------------------------------------------------

/**
 * Matches [str], and any trailing whitespace (as defined by [Grammar.whitespace]).
 */
class Word (str: String): Parser()
{
    override fun invoke(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches the same thing as [p], and any trailing whitespace (as defined by [Grammar.whitespace]).
 */
/*
class Word (p: Parser): Parser()
{
    override fun invoke(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
*/

// -------------------------------------------------------------------------------------------------

/**
 * Matches an alphabetic character (the ranges a-z and A-Z).
 */
class Alpha(): Parser()
{
    override fun invoke(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches an alphanumeric character (the ranges a-z, A-Z and 0-9).
 */
class Alphanum(): Parser()
{
    override fun invoke(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches a digit (the range 0-9).
 */
class Digit(): Parser()
{
    override fun invoke(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches an hexadecimal digit (the ranges a-f, A-F and 0-9).
 */
class Hex_digit(): Parser()
{
    override fun invoke(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches an octal digit (the range 0-7).
 */
class Octal_digit(): Parser()
{
    override fun invoke(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches a whitespace character, as defined by [Char.isWhitespace].
 */
class Space_char(): Parser()
{
    override fun invoke(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches a java identifier (as defined by JLS 3.8).
 */
class Java_iden(): Parser()
{
    override fun invoke(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches a java identifier that consists (as defined by JLS 3.8) that consists only of
 * ASCII characters.
 */
class Ascii_java_iden(): Parser()
{
    override fun invoke(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

// -------------------------------------------------------------------------------------------------
