# Matching Characters

These parsers are define in the file [Characters.kt].

[Characters.kt]: /src/norswap/autumn/parsers/Characters.kt

### `char_pred`

    inline fun Grammar.char_pred (pred: (Char) -> Boolean): Boolean

Matches any character that satisfied `pred`.

### `char_any`

    fun Grammar.char_any(): Boolean
    
Matches any character.
Only fails when the end of the input (represented by the null byte) is reached.

### `char_range`

    fun Grammar.char_range (start: Char, end: Char): Boolean

Matches any character in the range between `start` and `end`.

### `char_set (vararg Char)`

    fun Grammar.char_set (vararg chars: Char): Boolean
    
Matches any of the character in `chars`.

### `char_set (String)`

    fun Grammar.char_set (chars: String): Boolean

Matches any of the characters in `chars`.

### `string`

    fun Grammar.string (str: String): Boolean

Matches `str`.

### `word (String)`

    fun Grammar.word (str: String): Boolean

Matches `str`, and any trailing whitespace (as defined by `Grammar.whitespace`).

### `word (Parser)`

    inline fun Grammar.word (p: Parser): Boolean

Matches the same thing as `p`, and any trailing whitespace (as defined by `Grammar.whitespace`).

### `alpha`

    fun Grammar.alpha(): Boolean
    
Matches an alphabetic character (the ranges a-z and A-Z).

### `alphanum`

    fun Grammar.alphanum(): Boolean
    
Matches an alphanumeric character (the ranges a-z, A-Z and 0-9).

### `digit`

    fun Grammar.digit(): Boolean
    
Matches a digit (the range 0-9).

### `hex_digit`

    fun Grammar.hex_digit(): Boolean
    
Matches an hexadecimal digit (the ranges a-f, A-F and 0-9).

### `octal_digit`

    fun Grammar.octal_digit(): Boolean

Matches an octal digit (the range 0-7).

### `space_char`

    fun Grammar.space_char(): Boolean
    
Matches a whitespace character, as defined by [Char.isWhitespace].

[Char.isWhitespace]: https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.text/is-whitespace.html

### `java_iden`

    fun Grammar.java_iden(): Boolean

Matches a java identifier (as defined by [JLS 3.8]).

[JLS 3.8]: https://docs.oracle.com/javase/specs/jls/se8/html/jls-3.html#jls-3.8

### `ascii_java_iden`

    fun Grammar.ascii_java_iden(): Boolean

Matches a java identifier that consists (as defined by [JLS 3.8]) that consists only of
ASCII characters.
    
