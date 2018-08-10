package norswap.lang.java.ast;

import java.util.Objects;

public class Identifier implements Expression
{
    public final String name;

    public Identifier (String name) {
        this.name = name;
    }

    @Override public String toString() {
        return "id(" + name + ")";
    }

    @Override public int hashCode() {
        return name.hashCode();
    }

    @Override public boolean equals (Object obj) {
        return obj instanceof Identifier
            && Objects.equals(name, ((Identifier) obj).name);
    }
}
