package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class DimExpression
{
    public abstract List<TAnnotation> annotations();
    public abstract Expression expression();

    public static DimExpression mk (List<TAnnotation> annotations, Expression expression) {
        return new AutoValue_DimExpression(annotations, expression);
    }
}
