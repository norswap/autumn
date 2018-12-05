package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class VarDeclaration implements Declaration
{
    public abstract List<Modifier> modifiers();
    public abstract TType type();
    public abstract List<VarDeclarator> declarators();

    public static VarDeclaration mk (
        List<Modifier> modifiers, TType type, List<VarDeclarator> declarators)
    {
        return new AutoValue_VarDeclaration(modifiers, type, declarators);
    }
}
