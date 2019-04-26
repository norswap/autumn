package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class ImportDeclaration implements Statement
{
    public abstract boolean is_static();
    public abstract List<Identifier> name();
    public abstract boolean wildcard();

    public static ImportDeclaration mk (boolean is_static, List<Identifier> name, boolean wildcard) {
        return new AutoValue_ImportDeclaration(is_static, name, wildcard);
    }
}
