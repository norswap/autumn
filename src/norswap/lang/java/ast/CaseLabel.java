package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class CaseLabel implements SwitchLabel
{
    public abstract Expression expression();

    public static CaseLabel mk (Expression expression) {
        return new AutoValue_CaseLabel(expression);
    }
}
