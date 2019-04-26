package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class ArrayConstructorCall implements Expression
{
    public abstract TType type();
    public abstract List<DimExpression> dim_exprs();
    public abstract List<Dimension> dims();
    public abstract @Nullable Expression init();

    public static ArrayConstructorCall mk
        (TType type, List<DimExpression> dim_exprs, List<Dimension> dims,
         @Nullable Expression init)
    {
        return new AutoValue_ArrayConstructorCall(type, dim_exprs, dims, init);
    }
}
