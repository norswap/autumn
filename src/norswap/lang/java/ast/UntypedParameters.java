package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class UntypedParameters implements Parameters
{
    public abstract List<Identifier> parameters();

    public static UntypedParameters mk(List<Identifier> parameters) {
        return new AutoValue_UntypedParameters(parameters);
    }
}
