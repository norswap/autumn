package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class PackageDeclaration implements Statement
{
    public abstract List<TAnnotation> annotations();
    public abstract List<Identifier> name();

    public static PackageDeclaration mk (List<TAnnotation> annotations, List<Identifier> name) {
        return new AutoValue_PackageDeclaration(annotations, name);
    }
}
