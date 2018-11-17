package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class Dimension
{
    public abstract List<TAnnotation> annotations();

    public static Dimension mk (List<TAnnotation> annotations) {
        return new AutoValue_Dimension(annotations);
    }
}
