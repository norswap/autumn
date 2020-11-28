package norswap.lang.java;

import java.util.HashMap;

public enum TokenKind
{
    // copied from com.sun.tools.javac.parser.Tokens.TokenKind
    // https://github.com/dmlloyd/openjdk/blob/jdk8u/jdk8u/langtools/src/share/classes/com/sun/tools/javac/parser/Tokens.java

    ERROR(),
    IDENTIFIER(Tag.NAMED),
    ABSTRACT("abstract"),
    ASSERT("assert", Tag.NAMED),
    BOOLEAN("boolean", Tag.NAMED),
    BREAK("break"),
    BYTE("byte", Tag.NAMED),
    CASE("case"),
    CATCH("catch"),
    CHAR("char", Tag.NAMED),
    CLASS("class"),
    CONST("const"),
    CONTINUE("continue"),
    DEFAULT("default"),
    DO("do"),
    DOUBLE("double", Tag.NAMED),
    ELSE("else"),
    ENUM("enum", Tag.NAMED),
    EXTENDS("extends"),
    FINAL("final"),
    FINALLY("finally"),
    FLOAT("float", Tag.NAMED),
    FOR("for"),
    GOTO("goto"),
    IF("if"),
    IMPLEMENTS("implements"),
    IMPORT("import"),
    INSTANCEOF("instanceof"),
    INT("int", Tag.NAMED),
    INTERFACE("interface"),
    LONG("long", Tag.NAMED),
    NATIVE("native"),
    NEW("new"),
    PACKAGE("package"),
    PRIVATE("private"),
    PROTECTED("protected"),
    PUBLIC("public"),
    RETURN("return"),
    SHORT("short", Tag.NAMED),
    STATIC("static"),
    STRICTFP("strictfp"),
    SUPER("super", Tag.NAMED),
    SWITCH("switch"),
    SYNCHRONIZED("synchronized"),
    THIS("this", Tag.NAMED),
    THROW("throw"),
    THROWS("throws"),
    TRANSIENT("transient"),
    TRY("try"),
    VOID("void", Tag.NAMED),
    VOLATILE("volatile"),
    WHILE("while"),
    INTLITERAL(Tag.NUMERIC),
    LONGLITERAL(Tag.NUMERIC),
    FLOATLITERAL(Tag.NUMERIC),
    DOUBLELITERAL(Tag.NUMERIC),
    CHARLITERAL(Tag.NUMERIC),
    STRINGLITERAL(Tag.STRING),
    TRUE("true", Tag.NAMED),
    FALSE("false", Tag.NAMED),
    NULL("null", Tag.NAMED),
    UNDERSCORE("_", Tag.NAMED),
    ARROW("->"),
    COLCOL("::"),
    LPAREN("("),
    RPAREN(")"),
    LBRACE("{"),
    RBRACE("}"),
    LBRACKET("["),
    RBRACKET("]"),
    SEMI(";"),
    COMMA(","),
    DOT("."),
    ELLIPSIS("..."),
    EQ("="),
    GT(">"), // there is no GTGT or GTGTGT: ambiguity!
    LT("<"),
    BANG("!"),
    TILDE("~"),
    QUES("?"),
    COLON(":"),
    EQEQ("=="),
    LTEQ("<="),
    GTEQ(">="),
    BANGEQ("!="),
    AMPAMP("&&"),
    BARBAR("||"),
    PLUSPLUS("++"),
    SUBSUB("--"),
    PLUS("+"),
    SUB("-"),
    STAR("*"),
    SLASH("/"),
    AMP("&"),
    BAR("|"),
    CARET("^"),
    PERCENT("%"),
    LTLT("<<"),
    PLUSEQ("+="),
    SUBEQ("-="),
    STAREQ("*="),
    SLASHEQ("/="),
    AMPEQ("&="),
    BAREQ("|="),
    CARETEQ("^="),
    PERCENTEQ("%="),
    LTLTEQ("<<="),
    GTGTEQ(">>="),
    GTGTGTEQ(">>>="),
    MONKEYS_AT("@"),
    CUSTOM;

    enum Tag {
        DEFAULT,
        NAMED,
        STRING,
        NUMERIC
    }

    public final String name;
    public final Tag tag;

    TokenKind() {
        this(null, Tag.DEFAULT);
    }

    TokenKind(String name) {
        this(name, Tag.DEFAULT);
    }

    TokenKind(Tag tag) {
        this(null, tag);
    }

    TokenKind(String name, Tag tag) {
        this.name = name;
        this.tag = tag;
    }

    private static final HashMap<String, TokenKind> table = new HashMap<>();
    static {
        for (TokenKind kind: TokenKind.values())
            if (kind.name != null)
                table.put(kind.name, kind);
    }

    public static TokenKind lookup (String name) {
        return table.getOrDefault(name, IDENTIFIER);
    }
}