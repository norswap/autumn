package norswap.lang.java.ast;

/**
 * When applicable, names are taken from the javac compiler tree API.
 * https://docs.oracle.com/javase/8/docs/jdk/api/javac/tree/com/sun/source/tree/Tree.Kind.html
 */
public enum UnaryOperator
{
    POSTFIX_INCREMENT,
    PREFIX_INCREMENT,
    POSTFIX_DECREMENT,
    PREFIX_DECREMENT,
    UNARY_PLUS,
    UNARY_MINUS,
    BITWISE_COMPLEMENT,
    LOGICAL_COMPLEMENT,

    // Not named explicitly in thre tree API.
    DOT_THIS,   // MemberSelectTree
    DOT_SUPER,  // MemberSelectTree
}
