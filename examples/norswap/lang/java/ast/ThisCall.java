package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class ThisCall implements Expression
{
    public abstract List<Expression> args();

    public static ThisCall mk (List<Expression> args) {
        return new AutoValue_ThisCall(args);
    }
}
