package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class FormalParameters implements Parameters
{
    public abstract List<FormalParameter> parameters();

    public static FormalParameters mk(List<FormalParameter> parameters) {
        return new AutoValue_FormalParameters(parameters);
    }
}
