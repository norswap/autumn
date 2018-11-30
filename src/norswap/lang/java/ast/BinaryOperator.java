package norswap.lang.java.ast;

/**
 * When applicable, names are taken from the javac compiler tree API.
 * https://docs.oracle.com/javase/8/docs/jdk/api/javac/tree/com/sun/source/tree/Tree.Kind.html
 *
 * <p>Sometimes, we choose another name to avoid conflicts with token names.
 */
public enum BinaryOperator
{
    MULTIPLY,
    DIVIDE,
    REMAINDER,
    LEFT_SHIFT,
    RIGHT_SHIFT,
    UNSIGNED_RIGHT_SHIFT,
    LESS_THAN,
    LESS_THAN_EQUAL,
    GREATER_THAN,
    GREATER_THAN_EQUAL,
    EQUAL_TO,
    NOT_EQUAL_TO,
    AND,
    XOR,
    OR,
    CONDITIONAL_AND,
    CONDITIONAL_OR,

    // Renamed to avoid name conflicts
    ADD,
    SUBTRACT,
}
