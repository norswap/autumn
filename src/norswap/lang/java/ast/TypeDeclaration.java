package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class TypeDeclaration implements Declaration
{
    public enum Kind {
        ANNOTATION,
        CLASS,
        ENUM,
        INTERFACE
    }

    public abstract Kind kind();
    public abstract List<Modifier> modifiers();
    public abstract Identifier name();
    public abstract List<TypeParameter> type_params();
    public abstract List<TType> extended();
    public abstract List<TType> implemented();
    public abstract List<Declaration> declarations();

    public static TypeDeclaration mk
        (Kind kind, List<Modifier> modifiers, Identifier name, List<TypeParameter> type_params,
         List<TType> extended, List<TType> implemented, List<Declaration> declarations)
    {
        return new AutoValue_TypeDeclaration
            (kind, modifiers, name, type_params, extended, implemented, declarations);
    }
}
