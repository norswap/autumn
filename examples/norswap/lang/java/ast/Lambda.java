package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Lambda implements Expression
{
    public abstract Parameters parameters();
    public abstract Statement body();

    public static Lambda mk (Parameters parameters, Statement body) {
        return new AutoValue_Lambda(parameters, body);
    }
}
