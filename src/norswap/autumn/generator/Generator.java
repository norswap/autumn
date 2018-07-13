package norswap.autumn.generator;

import norswap.autumn.Parser;

public final class Generator
{
    // ---------------------------------------------------------------------------------------------

    public static String generate (String name, Parser parser)
    {
        StringBuilder b = new StringBuilder();
        b.append("public final class ").append(name).append("\n{");
        b.append("}");
        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------
}
