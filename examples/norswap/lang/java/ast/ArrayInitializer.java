package norswap.lang.java.ast;

import com.google.auto.value.AutoValue;
import java.util.List;

@AutoValue
public abstract class ArrayInitializer implements Expression
{
    public abstract List<Expression> expressions();

    public static ArrayInitializer mk (List<Expression> expressions) {
        return new AutoValue_ArrayInitializer(expressions);
    }
}
