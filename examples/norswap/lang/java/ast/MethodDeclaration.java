package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class MethodDeclaration implements Declaration
{
    public abstract List<Modifier> modifiers();
    public abstract List<TypeParameter> type_params();
    public abstract TType return_type();
    public abstract Identifier name();
    public abstract FormalParameters parameters();
    public abstract List<Dimension> dimensions();
    public abstract List<TType> thrown();
    public abstract @Nullable Block body();

    public static MethodDeclaration mk (
        List<Modifier> modifiers, List<TypeParameter> type_params, TType return_type, Identifier name,
        FormalParameters parameters, List<Dimension> dimensions, List<TType> thrown,
        @Nullable Block body)
    {
        return new AutoValue_MethodDeclaration(
            modifiers, type_params, return_type, name, parameters, dimensions, thrown, body);
    }
}
