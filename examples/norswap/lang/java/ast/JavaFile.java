package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class JavaFile implements Statement
{
    public abstract @Nullable PackageDeclaration package_declaration();
    public abstract List<ImportDeclaration> imports();
    public abstract List<TypeDeclaration> type_declarations();

    public static JavaFile mk (
        @Nullable PackageDeclaration package_declaration, List<ImportDeclaration> imports,
        List<TypeDeclaration> type_declarations)
    {
        return new AutoValue_JavaFile(package_declaration, imports, type_declarations);
    }
}
