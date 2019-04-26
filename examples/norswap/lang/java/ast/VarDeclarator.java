package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class VarDeclarator
{
    public abstract VarDeclaratorID id();
    public abstract @Nullable Expression initializer();

    public static VarDeclarator mk (VarDeclaratorID id, @Nullable Expression initializer) {
        return new AutoValue_VarDeclarator(id, initializer);
    }
}
