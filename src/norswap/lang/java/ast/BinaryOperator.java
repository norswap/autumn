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
    ASSIGNMENT,
    MULTIPLY_ASSIGNMENT,
    DIVIDE_ASSIGNMENT,
    REMAINDER_ASSIGNMENT,
    LEFT_SHIFT_ASSIGNMENT,
    RIGHT_SHIFT_ASSIGNMENT,
    UNSIGNED_RIGHT_SHIFT_ASSIGNMENT,
    AND_ASSIGNMENT,
    XOR_ASSIGNMENT,
    OR_ASSIGNMENT,

    // Renamed to avoid name conflicts
    ADD,
    SUBTRACT,

    // Renamed for consistency
    ADD_ASSIGNMENT,
    SUBTRACT_ASSIGNMENT
}
