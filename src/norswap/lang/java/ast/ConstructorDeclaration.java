package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class ConstructorDeclaration implements Declaration
{
    public abstract List<Modifier> modifiers();
    public abstract List<TypeParameter> type_params();
    public abstract Identifier name();
    public abstract FormalParameters parameters();
    public abstract List<TType> thrown();
    public abstract Block body();

    public static ConstructorDeclaration mk (
        List<Modifier> modifiers, List<TypeParameter> type_params, Identifier name,
        FormalParameters parameters, List<TType> thrown, Block body)
    {
        return new AutoValue_ConstructorDeclaration(
            modifiers, type_params, name, parameters, thrown, body);
    }
}
