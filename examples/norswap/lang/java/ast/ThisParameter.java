package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class ThisParameter implements FormalParameter
{
    public abstract List<Modifier> modifiers();
    public abstract TType type();
    public abstract List<Identifier> qualifier();

    public static ThisParameter mk
            (List<Modifier> modifiers, TType type, List<Identifier> qualifier) {
        return new AutoValue_ThisParameter(modifiers, type, qualifier);
    }
}
