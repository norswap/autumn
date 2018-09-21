package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class SuperCall implements Expression
{
    public abstract List<Expression> args();

    public static SuperCall mk (List<Expression> args) {
        return new AutoValue_SuperCall(args);
    }
}