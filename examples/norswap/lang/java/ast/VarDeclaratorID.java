package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class VarDeclaratorID
{
    public abstract Identifier name();
    public abstract List<Dimension> dimensions();

    public static VarDeclaratorID mk(Identifier name, List<Dimension> dimensions) {
        return new AutoValue_VarDeclaratorID(name, dimensions);
    }
}
